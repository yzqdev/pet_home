package com.github.yzqdev.pethome.util;


import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.NbtKeys;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.entity.HighlightedBlockEntity;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 宠物附魔读写与属性结算：citadel tag 里的附魔列表、实体级解析缓存构建与读取、写入后的属性修饰符重算
 * （原 {@code TameableUtils} 四区）。
 */
final class PetEnchantmentUtils {

    private PetEnchantmentUtils() {}
    /** 清空宠物全部附魔（项圈替换/契约解除时调用），并触发属性修饰符重算 */
    static void clearEnchants(LivingEntity entity) {
        setEnchantmentTag(entity, new ListTag());
    }

    /**
     * 用给定列表整体替换附魔集（非增量）：同时视为「已装备项圈」，写入 20 tick 项圈交换冷却
     * （CollarTickTracker 防抖），最后按新旧差集重算属性修饰符。
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

    private static boolean isWaterCreature(LivingEntity enchanted) {
        return enchanted.getType().getCategory() == MobCategory.WATER_CREATURE || enchanted.getType().getCategory() == MobCategory.UNDERGROUND_WATER_CREATURE || enchanted.getType().getCategory() == MobCategory.WATER_AMBIENT;
    }

    /**
     * 附魔变化后的属性收口：health_boost(+10/级最大生命)、speedster(+0.075/级速度)、
     * tough(+3/级护甲与击退抗性)、两栖水中生物的陆地加速；
     * 幼体诅咒（新增或刚移除）时切幼年姿态并 refreshDimensions 通知客户端。
     * 修饰符按固定 ResourceLocation 增删，等级 0 即移除。
     */
    private static void onUpdateEnchants(@Nullable Map<ResourceLocation, Integer> prevEnchants, LivingEntity enchanted) {
        int healthExtra = getEnchantLevel(enchanted, PHEnchantments.HEALTH_BOOST);
        int speedExtra = getEnchantLevel(enchanted, PHEnchantments.SPEEDSTER);
        int toughExtra = getEnchantLevel(enchanted, PHEnchantments.TOUGH);
        boolean amphib = hasEnchant(enchanted, PHEnchantments.AMPHIBIOUS) && !enchanted.isInWaterOrBubble() && isWaterCreature(enchanted);
        AttributeInstance health = enchanted.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speed = enchanted.getAttribute(Attributes.MOVEMENT_SPEED);
        var armor = enchanted.getAttribute(Attributes.ARMOR);
        var resistance = enchanted.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        var enchantReg = enchanted.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        if (hasEnchant(enchanted, PHEnchantments.IMMATURITY_CURSE) || prevEnchants != null && prevEnchants.containsKey(PHEnchantments.IMMATURITY_CURSE.registry())) {
            // 切换 Pose 以触发维度同步，让客户端立即看到幼年形态
            enchanted.setPose(Pose.FALL_FLYING);
            AgeableMob ageable = (AgeableMob) enchanted;
            ageable.setBaby(true); // 直接设为幼年
            enchanted.refreshDimensions();

        }
        if (armor != null && resistance != null) {
            AttributeModifier armorBoost = new AttributeModifier(PetDataKeys.ARMOR_BOOST_UUID, toughExtra * 3, AttributeModifier.Operation.ADD_VALUE);
            AttributeModifier resBoost = new AttributeModifier(PetDataKeys.RESISTANCE_BOOST_UUID, toughExtra * 3, AttributeModifier.Operation.ADD_VALUE);
            if (toughExtra > 0) {
                if (armor.hasModifier(armorBoost.id())) {
                    armor.removeModifier(armorBoost);
                    armor.addPermanentModifier(armorBoost);
                } else {
                    armor.addPermanentModifier(armorBoost);
                }
                if (resistance.hasModifier(resBoost.id())) {
                    resistance.removeModifier(resBoost);
                    resistance.addPermanentModifier(resBoost);
                } else {
                    resistance.addPermanentModifier(resBoost);
                }
            } else {
                armor.removeModifier(PetDataKeys.ARMOR_BOOST_UUID);
                resistance.removeModifier(PetDataKeys.RESISTANCE_BOOST_UUID);
            }
        }
        if (health != null) {
            AttributeModifier healthBoostPetUpgrade = new AttributeModifier(PetDataKeys.HEALTH_BOOST_UUID, healthExtra * 10, AttributeModifier.Operation.ADD_VALUE);

            if (healthExtra > 0) {
                if (health.hasModifier(healthBoostPetUpgrade.id())) {
                    health.removeModifier(healthBoostPetUpgrade);
                    health.addPermanentModifier(healthBoostPetUpgrade);
                } else {
                    health.addPermanentModifier(healthBoostPetUpgrade);
                }
            } else {

                health.removeModifier(healthBoostPetUpgrade);
            }
        }
        if (speed != null) {
            AttributeModifier speedsterPetUpgrade = new AttributeModifier(PetDataKeys.SPEED_BOOST_UUID, speedExtra * 0.075F, AttributeModifier.Operation.ADD_VALUE);

            if (speedExtra > 0) {
                if (speed.hasModifier(speedsterPetUpgrade.id())) {
                    speed.removeModifier(speedsterPetUpgrade);
                    speed.addPermanentModifier(speedsterPetUpgrade);
                } else {
                    speed.addPermanentModifier(speedsterPetUpgrade);
                }
            } else {
                speed.removeModifier(speedsterPetUpgrade);
            }
            AttributeModifier speed_aqua = new AttributeModifier(PetDataKeys.SPEED_BOOST_AQUATIC_LAND_UUID, 0.13F, AttributeModifier.Operation.ADD_VALUE);

            if (amphib) {
                if (speed.hasModifier(speed_aqua.id())) {
                    speed.removeModifier(speed_aqua);
                    speed.addPermanentModifier(speed_aqua);
                } else {
                    speed.addPermanentModifier(speed_aqua);
                }
            } else {
                speed.removeModifier(speed_aqua);
            }
        }
    }

    /** 有该宠物附魔即等级 &gt; 0 */
    static boolean hasEnchant(LivingEntity entity, ResourceKey<Enchantment> enchantment) {
        return getEnchantLevel(entity, enchantment) > 0;
    }

    /**
     * 附魔等级（mixin/渲染/tick 热路径入口）：PetSyncDataEntity 实现者走实体级缓存
     * （{@code ph_getEnchantCache}，tag 引用比对失效），否则全量扫描 NBT 列表。无该附魔返回 0。
     */
    static int getEnchantLevel(LivingEntity entity, ResourceKey<Enchantment> enchantment) {
        if (entity instanceof PetSyncDataEntity holder) {
            // 实体级缓存：附魔集合只在 tag 变化时解析一次，热路径（mixin/渲染/tick）不再反复扫描 NBT 列表
            Integer level = holder.ph_getEnchantCache(CitadelEntityData.getCitadelTag(entity)).get(enchantment.location());
            return level == null ? 0 : level;
        }
        ListTag listtag = getEnchantmentList(entity);

        if (listtag != null) {
            for (int i = 0; i < listtag.size(); ++i) {

                CompoundTag compoundtag = listtag.getCompound(i);
                var location = ResourceLocation.parse(compoundtag.getString("id"));

                if (enchantment.location().equals(location)) {
                    return compoundtag.getInt("lvl");
                }

            }

        }
        return 0;
    }

    /** 把 citadel tag 里的附魔列表解析为附魔 ID → level 映射（供实体缓存重建用） */
    static Map<ResourceLocation, Integer> buildEnchantCache(CompoundTag tag) {
        Map<ResourceLocation, Integer> cache = new HashMap<>();
        if (tag.contains(PetDataKeys.ENCHANTMENT_TAG)) {
            ListTag listtag = tag.getList(PetDataKeys.ENCHANTMENT_TAG, 10);
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                String id = compoundtag.getString("id");
                if (!id.isEmpty()) {
                    cache.put(ResourceLocation.parse(id), compoundtag.getInt("lvl"));
                }
            }
        }
        return cache;
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
     * 宠物附魔描述行（金色标题 + 每条附魔名与等级；含 "curse" 的显示为红色）。
     * 供 Jade/tooltip 等展示宠物附魔概览。
     */
    static List<Component> getEnchantDescriptions(LivingEntity entity) {
        List<Component> list = new ArrayList<>();
        list.add(Component.literal("   ").append(Component.translatable("message.pet_home.enchantments").withStyle(ChatFormatting.GOLD)));
        Map<ResourceLocation, Integer> map = getEnchants(entity);
        if (map != null) {
            for (Map.Entry<ResourceLocation, Integer> entry : map.entrySet()) {

                boolean isCurse = entry.getKey().getPath().contains("curse");
                list.add(Component.translatable("enchantment." + entry.getKey().getNamespace() + "." + entry.getKey().getPath()).append(Component.literal(" ")).append(Component.translatable("enchantment.level." + entry.getValue())).withStyle(isCurse ? ChatFormatting.RED : ChatFormatting.AQUA));

            }
        }
        return list;
    }

    /** 附魔列表 → {附魔 id → 等级}；无 StoredPetEnchantments 键时返回 {@code null}（与空列表区分） */
    static Map<ResourceLocation, Integer> getEnchants(LivingEntity entity) {
        ListTag listtag = getEnchantmentList(entity);
        if (listtag == null) {
            return null;
        }
        Map<ResourceLocation, Integer> enchants = new HashMap<>();
        for (int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            ResourceLocation res = ResourceLocation.parse(compoundtag.getString("id"));

            enchants.put(res, compoundtag.getInt("lvl"));

        }
        return enchants;
    }

    static CompoundTag storeEnchantment(@Nullable ResourceLocation pId, int pLevel) {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putString("id", String.valueOf((Object) pId));
        compoundtag.putShort("lvl", (short) pLevel);
        return compoundtag;
    }

    /**
     * 把物品附魔（{@code ItemEnchantments}，如项圈物品上记录的）整体写入宠物：
     * 是替换而非追加——换项圈时旧附魔已先经 clearEnchants 落回旧项圈物品。
     */
    static void addEnchant(LivingEntity entity, ItemEnchantments itemEnchantments) {
        ListTag listTag = new ListTag();


        for (var entry : itemEnchantments.entrySet()) {
            var en = entry.getKey().getKey().location();
            var compound = storeEnchantment(en, entry.getIntValue());
            listTag.add(compound);
        }
        setEnchantmentTag(entity, listTag);

    }
}
