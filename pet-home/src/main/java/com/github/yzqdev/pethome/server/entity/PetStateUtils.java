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
 * 宠物状态与计时器：护盾/光环/墙/威吓/无敌/冻结等计时器、暗影挥拳状态、僵尸化、
 * 宠物床坐标与维度（原 {@code TameableUtils} 三、五区）。
 */
final class PetStateUtils {

    private PetStateUtils() {}
    /** immune_frame 附魔的免疫帧计时（tick）；实体没有该附魔时恒为 0，不写 tag。 */
    static int getImmuneTime(LivingEntity enchanted) {
        if (PetEnchantmentUtils.hasEnchant(enchanted, PHEnchantmentRegistry.IMMUNITY_FRAME)) {
            CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
            return tag.getInt(PetDataKeys.IMMUNITY_TIME_TAG);
        }
        return 0;
    }

    static void setImmuneTime(LivingEntity enchanted, int time) {
        if (PetEnchantmentUtils.hasEnchant(enchanted, PHEnchantmentRegistry.IMMUNITY_FRAME)) {
            CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
            tag.putInt(PetDataKeys.IMMUNITY_TIME_TAG, time);
            PetDataSync.sync(enchanted, tag);
        }
    }

    /** 冷冻（frozen）剩余 tick 数。运行时存于 {@link PetSyncDataEntity} 独立 int 数据槽（每 tick 高频读写，避免整 tag 同步）；存档经 LivingEntityMixin 桥接回 {@code PetFrozenTime} NBT 键保证旧档兼容。 */
    static int getFrozenTime(LivingEntity enchanted) {
        if (enchanted instanceof PetSyncDataEntity holder) {
            return holder.ph_getFrozenTime();
        }
        return 0;
    }

    /** 写冷冻计时（同上走独立数据槽，setter 需在 mixin 侧自行判等避免 0→0 空同步）。 */
    static void setFrozenTimeTag(LivingEntity enchanted, int time) {
        if (enchanted instanceof PetSyncDataEntity holder) {
            holder.ph_setFrozenTime(time);
        }
    }

    /** 暗影手当前攻击目标实体 ID（citadel tag 存储，无目标为 -1）。 */
    static int getPetAttackTargetID(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return !tag.contains(PetDataKeys.ATTACK_TARGET_ENTITY) ? -1 : tag.getInt(PetDataKeys.ATTACK_TARGET_ENTITY);
    }

    /** 按存储的实体 ID 解析暗影手攻击目标；目标已不在世界中时返回 null。 */
    @Nullable
    static Entity getPetAttackTarget(LivingEntity enchanted) {
        int i = getPetAttackTargetID(enchanted);
        return i == -1 ? null : enchanted.level().getEntity(i);
    }

    /** 设置暗影手攻击目标 ID（随 sync 同步客户端供渲染层使用）。 */
    static void setPetAttackTarget(LivingEntity enchanted, int id) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PetDataKeys.ATTACK_TARGET_ENTITY, id);
        PetDataSync.sync(enchanted, tag);
    }

    /**
     * 暗影手（shadow_hands 附魔）状态机数据：cooldown 为出拳冷却 tick；
     * times 是各拳的已出次数数组；striking 标记进行中的拳索引数组。均存 citadel tag 并随 sync 同步
     * （渲染层读取 striking 做挥拳动画）。
     */
    static int getShadowPunchCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(PetDataKeys.SHADOW_PUNCH_COOLDOWN);
    }

    static void setShadowPunchCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PetDataKeys.SHADOW_PUNCH_COOLDOWN, time);
        PetDataSync.sync(enchanted, tag);
    }

    static int[] getShadowPunchTimes(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getIntArray(PetDataKeys.SHADOW_PUNCH_TIMES);
    }

    static void setShadowPunchTimes(LivingEntity enchanted, int[] times) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putIntArray(PetDataKeys.SHADOW_PUNCH_TIMES, times);
        PetDataSync.sync(enchanted, tag);
    }

    static void setShadowPunchStriking(LivingEntity enchanted, int[] times) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putIntArray(PetDataKeys.SHADOW_PUNCH_STRIKING, times);
        PetDataSync.sync(enchanted, tag);
    }

    static int[] getShadowPunchStriking(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getIntArray(PetDataKeys.SHADOW_PUNCH_STRIKING);
    }

    /** psychic_wall（念力墙）附魔的施放冷却 tick，citadel tag 存储。 */
    static int getPsychicWallCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(PetDataKeys.PSYCHIC_WALL_COOLDOWN);
    }

    static void setPsychicWallCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PetDataKeys.PSYCHIC_WALL_COOLDOWN, time);
        PetDataSync.sync(enchanted, tag);
    }

    /** intimidation（威吓）附魔的冷却 tick：归零后才能再次触发威吓，被吓退的怪物数量按剩余冷却递减。 */
    static int getIntimidationCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(PetDataKeys.INTIMIDATION_COOLDOWN);
    }

    static void setIntimidationCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PetDataKeys.INTIMIDATION_COOLDOWN, time);
        PetDataSync.sync(enchanted, tag);
    }

    /** blazing_protection（炽焰守护）的吸收护盾充能值与重充冷却 tick，citadel tag 存储。 */
    static int getBlazingProtectionCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(PetDataKeys.BLAZING_PROTECTION_COOLDOWN);
    }

    static void setBlazingProtectionCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PetDataKeys.BLAZING_PROTECTION_COOLDOWN, time);
        PetDataSync.sync(enchanted, tag);
    }

    /** healing_aura（治疗光环）的相位计时 tick。运行时存 {@link PetSyncDataEntity} 独立 int 数据槽（高频每 tick 读写），存档经 LivingEntityMixin 桥接回 {@code PetHealingAuraTime} NBT 键。 */
    static int getHealingAuraTime(LivingEntity enchanted) {
        if (enchanted instanceof PetSyncDataEntity holder) {
            return holder.ph_getHealingAuraTime();
        }
        return 0;
    }

    static void setHealingAuraTime(LivingEntity enchanted, int time) {
        if (enchanted instanceof PetSyncDataEntity holder) {
            holder.ph_setHealingAuraTime(time);
        }
    }

    /** healing_aura 的脉冲标记（当前 tick 是否该发治疗脉冲），citadel tag 布尔存储。 */
    static boolean getHealingAuraImpulse(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getBoolean(PetDataKeys.HEALING_AURA_IMPULSE);
    }

    static void setHealingAuraImpulse(LivingEntity enchanted, boolean impulse) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(PetDataKeys.HEALING_AURA_IMPULSE, impulse);
        PetDataSync.sync(enchanted, tag);
    }

    /** blazing_protection 已充能的护盾条数（受击时逐条抵消伤害），citadel tag 存储。 */
    static int getBlazingProtectionBars(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(PetDataKeys.BLAZING_PROTECTION_BARS);
    }

    static void setBlazingProtectionBars(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PetDataKeys.BLAZING_PROTECTION_BARS, time);
        PetDataSync.sync(enchanted, tag);
    }

    /** 宠物绑定的宠物床坐标（citadel tag 存储）；未绑定时返回 null。宠物床复活/拆除时读写。 */
    @Nullable
    static BlockPos getPetBedPos(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        if (tag.getBoolean(PetDataKeys.HAS_PET_BED) && tag.contains(PetDataKeys.PET_BED_X) && tag.contains(PetDataKeys.PET_BED_Y) && tag.contains(PetDataKeys.PET_BED_Z)) {
            return new BlockPos(tag.getInt(PetDataKeys.PET_BED_X), tag.getInt(PetDataKeys.PET_BED_Y), tag.getInt(PetDataKeys.PET_BED_Z));
        }
        return null;
    }

    static void setPetBedPos(LivingEntity enchanted, BlockPos petBed) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(PetDataKeys.HAS_PET_BED, true);
        tag.putInt(PetDataKeys.PET_BED_X, petBed.getX());
        tag.putInt(PetDataKeys.PET_BED_Y, petBed.getY());
        tag.putInt(PetDataKeys.PET_BED_Z, petBed.getZ());
        PetDataSync.sync(enchanted, tag);
    }

    /** 解除宠物床绑定（只置 HAS_PET_BED=false，坐标键保留），床被拆/放生时调用。 */
    static void removePetBedPos(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(PetDataKeys.HAS_PET_BED, false);
        PetDataSync.sync(enchanted, tag);
    }

    /** 宠物床所在维度（字符串形式如 "minecraft:overworld"）；旧数据缺该键时按主世界处理。 */
    static String getPetBedDimension(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return !tag.contains(PetDataKeys.PET_BED_DIMENSION) ? "minecraft:overworld" : tag.getString(PetDataKeys.PET_BED_DIMENSION);
    }

    static void setPetBedDimension(LivingEntity enchanted, String dimension) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putString(PetDataKeys.PET_BED_DIMENSION, dimension);
        PetDataSync.sync(enchanted, tag);
    }

    /** 跨端同步的下落距离（citadel tag 存储）：void_cloud 缓降等附魔据此在客户端与服务端保持一致判定。 */
    static float getFallDistance(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getFloat(PetDataKeys.FALL_DISTANCE_SYNC);
    }

    static void setFallDistance(LivingEntity enchanted, float dist) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putFloat(PetDataKeys.FALL_DISTANCE_SYNC, dist);
        PetDataSync.sync(enchanted, tag);

    }

    /** 僵尸化宠物标记（ZombiePet）：感染类效果把宠物转为敌对僵尸形态时置位，供渲染与行为判定。 */
    static void setZombiePet(LivingEntity enchanted, boolean zombiefied) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(PetDataKeys.ZOMBIE_PET, zombiefied);
        PetDataSync.sync(enchanted, tag);

    }

    static boolean isZombiePet(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getBoolean(PetDataKeys.ZOMBIE_PET);
    }
}
