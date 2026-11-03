package com.github.yzqdev.pethome.util;


import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.entity.HighlightedBlockEntity;
import com.github.yzqdev.pethome.server.NbtKeys;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
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
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 宠物附魔读写与属性结算：citadel tag 里的附魔列表、实体级解析缓存读取、写入后的属性修饰符重算
 * （原 {@code TameableUtils} 四区）。
 */
final class PetEnchantmentUtils {

    private PetEnchantmentUtils() {}
    /** 清空宠物全部附魔（走 {@link #setEnchantmentTag}，会顺带把项圈置为已戴上并触发属性重算）。 */
    static void clearEnchants(LivingEntity entity) {
        setEnchantmentTag(entity, new ListTag());
    }

    /** 整表覆写附魔列表（addEnchant/clearEnchants 的共同出口）。副作用：置位项圈、写 20 tick 换项圈防抖、随后按前后差量重算属性。 */
    private static void setEnchantmentTag(LivingEntity enchanted, ListTag enchants) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        Map<Identifier, Integer> prevEnchants = getEnchants(enchanted);
        tag.put(PetDataKeys.ENCHANTMENT_TAG, enchants);
        tag.putInt(PetDataKeys.COLLAR_SWAP_COOLDOWN, 20);
        tag.putBoolean(PetDataKeys.COLLAR_TAG, true);
        PetDataSync.sync(enchanted, tag);
        onUpdateEnchants(prevEnchants, enchanted);
    }

    /** 实体是否为水生生物类别（WATER_CREATURE / UNDERGROUND_WATER_CREATURE / WATER_AMBIENT），onUpdateEnchants 用它决定两栖附魔的陆地加速。 */
    private static boolean isWaterCreature(LivingEntity enchanted) {
        return enchanted.getType().getCategory() == MobCategory.WATER_CREATURE || enchanted.getType().getCategory() == MobCategory.UNDERGROUND_WATER_CREATURE || enchanted.getType().getCategory() == MobCategory.WATER_AMBIENT;
    }

    /** 附魔变化后的属性同步：按当前等级重挂 health_boost(+10/级)/speedster/tough(+3 护甲与击退抗性) 永久修饰符，两栖水生生物额外加陆地速度；immaturity_curse 新增或刚移除时把实体设为幼年并刷新碰撞箱。 */
    private static void onUpdateEnchants(@Nullable Map<Identifier, Integer> prevEnchants, LivingEntity enchanted) {
        int healthExtra = getEnchantLevel(enchanted, PHEnchantments.HEALTH_BOOST);
        int speedExtra = getEnchantLevel(enchanted, PHEnchantments.SPEEDSTER);
        int toughExtra = getEnchantLevel(enchanted, PHEnchantments.TOUGH);
        boolean amphib = hasEnchant(enchanted, PHEnchantments.AMPHIBIOUS) && !enchanted.isInWater() && isWaterCreature(enchanted);
        AttributeInstance health = enchanted.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speed = enchanted.getAttribute(Attributes.MOVEMENT_SPEED);
        var armor = enchanted.getAttribute(Attributes.ARMOR);
        var resistance = enchanted.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        var enchantReg = enchanted.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
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

    /** {@link #getEnchantLevel} 的布尔形态。 */
    static boolean hasEnchant(LivingEntity entity, ResourceKey<Enchantment> enchantment) {
        return getEnchantLevel(entity, enchantment) > 0;
    }

    /**
     * 查宠物单个附魔等级（mixin/渲染/tick 热路径）。
     * 走 {@link PetEnchantCache}：只读当前附件实例并按引用比对，附件没换实例就不重扫 NBT；
     * 未挂附件或无该附魔返回 0。
     */
    static int getEnchantLevel(LivingEntity entity, ResourceKey<Enchantment> enchantment) {
        // 实体级缓存：附魔集合只在 citadel 附件实例被替换时解析一次（setData/网络同步换引用）；
        // 原先每次查询都经 getOrCreateCitadelTag 整 tag 拷贝再扫描（mixin/渲染/tick 热路径）
        CompoundTag live = CitadelEntityData.getExistingCitadelTagOrNull(entity);
        Integer level = entity.getData(PHAttachments.ENCHANT_CACHE).rebuildIfStale(live).get(enchantment.identifier());
        return level == null ? 0 : level;
    }

    /**
     * 组装宠物附魔列表的展示行（标题 + 每条「附魔名 等级」，诅咒显示红色）。
     * 语言键为 {@code enchantment.<namespace>.<path>} + {@code enchantment.level.<n>}，Jade tooltip 与宠物信息面板共用。
     */
    static List<Component> getEnchantDescriptions(LivingEntity entity) {
        List<Component> list = new ArrayList<>();
        list.add(Component.literal("   ").append(Component.translatable("message.pet_home.enchantments").withStyle(ChatFormatting.GOLD)));
        Map<Identifier, Integer> map = getEnchants(entity);
        if (map != null) {
            for (Map.Entry<Identifier, Integer> entry : map.entrySet()) {

                boolean isCurse = entry.getKey().getPath().contains("curse");
                list.add(Component.translatable("enchantment." + entry.getKey().getNamespace() + "." + entry.getKey().getPath()).append(Component.literal(" ")).append(Component.translatable("enchantment.level." + entry.getValue())).withStyle(isCurse ? ChatFormatting.RED : ChatFormatting.AQUA));

            }
        }
        return list;
    }

    /**
     * 取宠物全部附魔（{@code id → level}）；实体未挂 citadel 附件或无附魔键时返回 {@code null}（调用方需判空）。
     * 结果来自 {@link PetEnchantCache}（引用比对缓存），不得修改返回的 Map。
     */
    @Nullable
    static Map<Identifier, Integer> getEnchants(LivingEntity entity) {
        CompoundTag live = CitadelEntityData.getExistingCitadelTagOrNull(entity);
        if (live == null || !live.contains(PetDataKeys.ENCHANTMENT_TAG)) {
            return null;
        }
        // 附魔 tooltip（Jade 每帧触发）同样走实体缓存，避免每次整 tag 拷贝+重建映射
        return entity.getData(PHAttachments.ENCHANT_CACHE).rebuildIfStale(live);
    }

    /** 构造附魔列表的单个条目 tag：{@code id} 存 {@link Identifier} 字符串、{@code lvl} 存 short（三侧同格式）。 */
    static CompoundTag storeEnchantment(@Nullable Identifier pId, int pLevel) {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putString("id", String.valueOf((Object) pId));
        compoundtag.putShort("lvl", (short) pLevel);
        return compoundtag;
    }

    /** 把 {@link ItemEnchantments}（附魔书内容）整表写入宠物附魔列表——覆盖旧列表而非追加（对齐 DI 原版附魔书行为）。 */
    static void addEnchant(LivingEntity entity, ItemEnchantments itemEnchantments) {
        ListTag listTag = new ListTag();


        for (var entry : itemEnchantments.entrySet()) {
            var en = entry.getKey().getKey().identifier();
            var compound = storeEnchantment(en, entry.getIntValue());
            listTag.add(compound);
        }
        setEnchantmentTag(entity, listTag);

    }
}
