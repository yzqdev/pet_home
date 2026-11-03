package com.github.yzqdev.pethome.server.entity;



import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.network.Networking;
import com.github.yzqdev.pethome.server.NbtKeys;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 宠物附魔读写与属性结算：citadel tag 里的附魔列表、附魔缓存构建与读取、写入后的属性修饰符重算
 * （原 {@code TameableUtils} 四区）。
 */
final class PetEnchantmentUtils {

    private PetEnchantmentUtils() {}
    /**
     * 附魔列表写入的总收口：写回 citadel tag，同时置 20 tick 项圈防抖（COLLAR_SWAP_COOLDOWN）
     * 并标记已佩戴项圈，防止紧随其后的项圈交互把旧附魔当作掉落处理；随后 sync 并按附魔增删重算属性修饰符。
     */
    private static void setEnchantmentTag(LivingEntity enchanted, ListTag enchants) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        Map<ResourceLocation, Integer> prevEnchants = getEnchants(enchanted);
        tag.put(PetDataKeys.ENCHANTMENT_TAG, enchants);
        tag.putInt(PetDataKeys.COLLAR_SWAP_COOLDOWN, 20);
        tag.putBoolean(PetDataKeys.COLLAR_TAG, true);
        PetDataSync.sync(enchanted, tag);
        onUpdateEnchants(prevEnchants, enchanted);
    }

    /**
     * 附魔变化后的属性同步：按当前附魔等级重挂/摘除 MAX_HEALTH(+10/级)、MOVEMENT_SPEED(+7.5%/级)、
     * 两栖陆行增速、护甲与击退抗性(TOUGH +3/级)的永久修饰符；immaturity_curse 新增或被移除时
     * 将实体切幼年并 refreshDimensions 让客户端立即刷新姿态。prevEnchants 为写前快照，用于感知诅咒移除。
     */
    private static void onUpdateEnchants(@Nullable Map<ResourceLocation, Integer> prevEnchants, LivingEntity enchanted) {
        int healthExtra = getEnchantLevel(enchanted, PHEnchantmentRegistry.HEALTH_BOOST);
        int speedExtra = getEnchantLevel(enchanted, PHEnchantmentRegistry.SPEEDSTER);
        boolean amphib = hasEnchant(enchanted, PHEnchantmentRegistry.AMPHIBIOUS) && !enchanted.isInWaterOrBubble() && isWaterCreature(enchanted);
        AttributeInstance health = enchanted.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speed = enchanted.getAttribute(Attributes.MOVEMENT_SPEED);
        if (hasEnchant(enchanted, PHEnchantmentRegistry.IMMATURITY_CURSE) || prevEnchants != null && prevEnchants.keySet().contains(ForgeRegistries.ENCHANTMENTS.getKey(PHEnchantmentRegistry.IMMATURITY_CURSE))) {
            // 切换 Pose 以触发维度同步，让客户端立即看到幼年形态
            enchanted.setPose(Pose.FALL_FLYING);
            AgeableMob ageable = (AgeableMob) enchanted;
            ageable.setBaby(true); // 直接设为幼年
            enchanted.refreshDimensions();
        }
        if (health != null) {
            if (healthExtra > 0) {
                AttributeModifier attributemodifier = new AttributeModifier(PetDataKeys.HEALTH_BOOST_UUID, "health boost pet upgrade", healthExtra * 10, AttributeModifier.Operation.ADDITION);
                if (health.hasModifier(attributemodifier)) {
                    health.removeModifier(attributemodifier);
                    health.addPermanentModifier(attributemodifier);
                } else {
                    health.addPermanentModifier(attributemodifier);
                }
            } else {
                health.removePermanentModifier(PetDataKeys.HEALTH_BOOST_UUID);
            }
        }
        if (speed != null) {
            if (speedExtra > 0) {
                AttributeModifier attributemodifier = new AttributeModifier(PetDataKeys.SPEED_BOOST_UUID, "speedster pet upgrade", speedExtra * 0.075F, AttributeModifier.Operation.ADDITION);
                if (speed.hasModifier(attributemodifier)) {
                    speed.removeModifier(attributemodifier);
                    speed.addPermanentModifier(attributemodifier);
                } else {
                    speed.addPermanentModifier(attributemodifier);
                }
            } else {
                speed.removePermanentModifier(PetDataKeys.SPEED_BOOST_UUID);
            }
            if (amphib) {
                AttributeModifier attributemodifier = new AttributeModifier(PetDataKeys.SPEED_BOOST_AQUATIC_LAND_UUID, "amphibious pet upgrade", 0.13F, AttributeModifier.Operation.ADDITION);
                if (speed.hasModifier(attributemodifier)) {
                    speed.removeModifier(attributemodifier);
                    speed.addPermanentModifier(attributemodifier);
                } else {
                    speed.addPermanentModifier(attributemodifier);
                }
            } else {
                speed.removePermanentModifier(PetDataKeys.SPEED_BOOST_AQUATIC_LAND_UUID);
            }
        }
        // TOUGH（稳固）：每级 +3 护甲与 +3 击退抗性（自 1.21 移植）
        int toughExtra = getEnchantLevel(enchanted, PHEnchantmentRegistry.TOUGH);
        AttributeInstance armor = enchanted.getAttribute(Attributes.ARMOR);
        AttributeInstance knockbackResistance = enchanted.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (armor != null) {
            if (toughExtra > 0) {
                AttributeModifier armorBoost = new AttributeModifier(PetDataKeys.TOUGH_ARMOR_UUID, "tough pet armor upgrade", toughExtra * 3, AttributeModifier.Operation.ADDITION);
                if (armor.hasModifier(armorBoost)) {
                    armor.removeModifier(armorBoost);
                }
                armor.addPermanentModifier(armorBoost);
            } else {
                armor.removePermanentModifier(PetDataKeys.TOUGH_ARMOR_UUID);
            }
        }
        if (knockbackResistance != null) {
            if (toughExtra > 0) {
                AttributeModifier resBoost = new AttributeModifier(PetDataKeys.TOUGH_KNOCKBACK_UUID, "tough pet knockback resistance upgrade", toughExtra * 3, AttributeModifier.Operation.ADDITION);
                if (knockbackResistance.hasModifier(resBoost)) {
                    knockbackResistance.removeModifier(resBoost);
                }
                knockbackResistance.addPermanentModifier(resBoost);
            } else {
                knockbackResistance.removePermanentModifier(PetDataKeys.TOUGH_KNOCKBACK_UUID);
            }
        }
    }

    private static boolean isWaterCreature(LivingEntity enchanted) {
        return enchanted.getType().getCategory() == MobCategory.WATER_CREATURE || enchanted.getType().getCategory() == MobCategory.UNDERGROUND_WATER_CREATURE || enchanted.getType().getCategory() == MobCategory.WATER_AMBIENT;
    }

    @Nullable
    private static ListTag getEnchantmentList(LivingEntity entity) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(entity);
        if (tag.contains(PetDataKeys.ENCHANTMENT_TAG)) {
            return tag.getList(PetDataKeys.ENCHANTMENT_TAG, 10);
        }
        return null;
    }

    /**
     * 查宠物某附魔的等级，未拥有/被配置禁用时返回 0。实体实现了 {@link PetSyncDataEntity} 时走
     * 实体级缓存（tag 变化才重新解析 NBT 列表），供 mixin/渲染/tick 热路径频繁调用。
     * 1.20 侧的键是 {@link Enchantment} 注册表实例本身（26.1 为 ResourceLocation/ResourceKey）。
     */
    static int getEnchantLevel(LivingEntity entity, Enchantment enchantment) {
        if (!PetHomeMod.CONFIG.isEnchantEnabled(enchantment)) {
            return 0;
        }
        if (entity instanceof PetSyncDataEntity holder) {
            // 实体级缓存：附魔集合只在 tag 变化时解析一次，热路径（mixin/渲染/tick）不再反复扫描 NBT 列表
            Integer level = holder.ph_getEnchantCache(CitadelEntityData.getCitadelTag(entity)).get(enchantment);
            return level == null ? 0 : level;
        }
        ListTag listtag = getEnchantmentList(entity);
        if (listtag != null) {
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                ResourceLocation res = EnchantmentHelper.getEnchantmentId(compoundtag);
                if (res != null && res.equals(ForgeRegistries.ENCHANTMENTS.getKey(enchantment))) {
                    return EnchantmentHelper.getEnchantmentLevel(compoundtag);
                }
            }
        }
        return 0;
    }

    /** 把 citadel tag 里的附魔列表解析为 Enchantment → level 映射（供实体缓存重建用） */
    static Map<Enchantment, Integer> buildEnchantCache(CompoundTag tag) {
        Map<Enchantment, Integer> cache = new HashMap<>();
        if (tag.contains(PetDataKeys.ENCHANTMENT_TAG)) {
            ListTag listtag = tag.getList(PetDataKeys.ENCHANTMENT_TAG, 10);
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                ResourceLocation res = EnchantmentHelper.getEnchantmentId(compoundtag);
                if (res != null) {
                    Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(res);
                    if (enchantment != null) {
                        cache.put(enchantment, EnchantmentHelper.getEnchantmentLevel(compoundtag));
                    }
                }
            }
        }
        return cache;
    }

    /** 是否拥有该附魔（等级 > 0），底层走 {@link #getEnchantLevel} 的缓存路径。 */
    static boolean hasEnchant(LivingEntity entity, Enchantment enchantment) {
        return getEnchantLevel(entity, enchantment) > 0;
    }

    /**
     * 解析实体全部宠物附魔为 ResourceLocation → 等级映射（已按配置过滤被禁用的附魔）。
     * 实体从未有过附魔（citadel tag 无 {@code StoredPetEnchantments} 键）时返回 null，调用方需判空。
     */
    @Nullable
    static Map<ResourceLocation, Integer> getEnchants(LivingEntity entity) {
        ListTag listtag = getEnchantmentList(entity);
        if (listtag == null) {
            return null;
        }
        Map<ResourceLocation, Integer> enchants = new HashMap<>();
        for (int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            ResourceLocation res = EnchantmentHelper.getEnchantmentId(compoundtag);
            if (PetHomeMod.CONFIG.isEnchantEnabled(res.getPath())) {
                enchants.put(res, EnchantmentHelper.getEnchantmentLevel(compoundtag));
            }
        }
        return enchants;
    }

    /** 生成附魔列表的展示文本（金色标题行 + 每条「附魔名 等级」，诅咒附魔显示红色），用于 tooltip/Jade 等界面。 */
    static List<Component> getEnchantDescriptions(LivingEntity entity) {
        List<Component> list = new ArrayList<>();
        list.add(Component.literal("   ").append(Component.translatable("message.pet_home.enchantments").withStyle(ChatFormatting.GOLD)));
        Map<ResourceLocation, Integer> map = getEnchants(entity);
        if (map != null) {
            for (Map.Entry<ResourceLocation, Integer> entry : map.entrySet()) {
                if (PetHomeMod.CONFIG.isEnchantEnabled(entry.getKey().getPath())) {
                    boolean isCurse = entry.getKey().getPath().contains("curse");
                    list.add(Component.translatable("enchantment." + entry.getKey().getNamespace() + "." + entry.getKey().getPath()).append(Component.literal(" ")).append(Component.translatable("enchantment.level." + entry.getValue())).withStyle(isCurse ? ChatFormatting.RED : ChatFormatting.AQUA));
                }
            }
        }
        return list;
    }

    /** 实体缓存路径下判定是否拥有任意宠物附魔；无缓存的实体直接看 NBT 列表是否非空。 */
    static boolean hasAnyEnchants(LivingEntity entity) {
        if (entity instanceof PetSyncDataEntity holder) {
            return !holder.ph_getEnchantCache(CitadelEntityData.getCitadelTag(entity)).isEmpty();
        }
        ListTag listtag = getEnchantmentList(entity);
        return listtag != null && !listtag.isEmpty();
    }

    /** 给宠物加一条附魔：已有同名附魔时只允许升级（现等级更高则维持不变），写入后触发防抖与属性重算。 */
    static void addEnchant(LivingEntity entity, EnchantmentInstance enchantment) {
        ListTag listtag = getEnchantmentList(entity);
        addEnchant(entity, enchantment, listtag);
    }

    /** addEnchant 的列表版本：在调用方提供的 {@link ListTag} 上原地修改后统一经 {@link #setEnchantmentTag} 写回（附魔被配置禁用时静默忽略）。 */
    static void addEnchant(LivingEntity entity, EnchantmentInstance enchantment, ListTag listtag) {
        if (listtag != null && PetHomeMod.CONFIG.isEnchantEnabled(enchantment.enchantment)) {
            ResourceLocation resourcelocation = EnchantmentHelper.getEnchantmentId(enchantment.enchantment);
            boolean flag = true;
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                ResourceLocation resourcelocation1 = EnchantmentHelper.getEnchantmentId(compoundtag);
                if (resourcelocation1 != null && resourcelocation1.equals(resourcelocation)) {
                    if (EnchantmentHelper.getEnchantmentLevel(compoundtag) < enchantment.level) {
                        EnchantmentHelper.setEnchantmentLevel(compoundtag, enchantment.level);
                    }

                    flag = false;
                    break;
                }
            }
            if (flag) {
                listtag.add(EnchantmentHelper.storeEnchantment(resourcelocation, enchantment.level));
            }
            setEnchantmentTag(entity, listtag);
        }
    }

    /** 清空全部宠物附魔（写入空列表，走统一收口：触发防抖 + 属性重算，卸下所有属性修饰符）。契约解除驯服/换项圈时使用。 */
    static void clearEnchants(LivingEntity entity) {
        setEnchantmentTag(entity, new ListTag());
    }
}
