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
 * 宠物状态与计时器：护盾/光环/音波/墙/威吓/无敌/冻结等计时器、暗影挥拳状态、宠物床坐标与维度
 * （原 {@code TameableUtils} 三、五区）。
 */
final class PetStateUtils {

    private PetStateUtils() {}
    static int getBlazingProtectionBars(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(PetDataKeys.BLAZING_PROTECTION_BARS);
    }

    static int getBlazingProtectionCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(PetDataKeys.BLAZING_PROTECTION_COOLDOWN);
    }

    static void setBlazingProtectionCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PetDataKeys.BLAZING_PROTECTION_COOLDOWN, time);
        PetDataSync.sync(enchanted, tag);
    }

    static void setBlazingProtectionBars(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PetDataKeys.BLAZING_PROTECTION_BARS, time);
        PetDataSync.sync(enchanted, tag);
    }

    /** 治疗光环相位计时器：走 PH_HEALING_AURA_TIME 独立 int 数据槽（每 tick 增减只同步小整数），非数据槽实现者恒 0 */
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

    static long getSonicboomAuraTime(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getLong(PetDataKeys.Sonic_boom_TIME);
    }

    static void setSonicboomAuraTime(LivingEntity enchanted, long time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putLong(PetDataKeys.Sonic_boom_TIME, time);
        PetDataSync.sync(enchanted, tag);
    }

    static boolean getHealingAuraImpulse(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getBoolean(PetDataKeys.HEALING_AURA_IMPULSE);
    }

    static void setHealingAuraImpulse(LivingEntity enchanted, boolean impulse) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(PetDataKeys.HEALING_AURA_IMPULSE, impulse);
        PetDataSync.sync(enchanted, tag);
    }

    static int getPsychicWallCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(PetDataKeys.PSYCHIC_WALL_COOLDOWN);
    }

    static void setPsychicWallCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PetDataKeys.PSYCHIC_WALL_COOLDOWN, time);
        PetDataSync.sync(enchanted, tag);
    }

    static int getPetAttackTargetID(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return !tag.contains(PetDataKeys.ATTACK_TARGET_ENTITY) ? -1 : tag.getInt(PetDataKeys.ATTACK_TARGET_ENTITY);
    }

    @Nullable
    static Entity getPetAttackTarget(LivingEntity enchanted) {
        int i = getPetAttackTargetID(enchanted);
        return i == -1 ? null : enchanted.level().getEntity(i);
    }

    static void setPetAttackTarget(LivingEntity enchanted, int id) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PetDataKeys.ATTACK_TARGET_ENTITY, id);
        PetDataSync.sync(enchanted, tag);
    }

    static int getIntimidationCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(PetDataKeys.INTIMIDATION_COOLDOWN);
    }

    static void setIntimidationCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PetDataKeys.INTIMIDATION_COOLDOWN, time);
        PetDataSync.sync(enchanted, tag);
    }

    /** 免疫帧剩余时间：仅免疫帧附魔存在时有效（无附魔读写都被跳过），由 EntityTickHandler 每 tick 递减 */
    static int getImmuneTime(LivingEntity enchanted) {
        if (PetEnchantmentUtils.hasEnchant(enchanted, PHEnchantments.IMMUNITY_FRAME)) {
            CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
            return tag.getInt(PetDataKeys.IMMUNITY_TIME_TAG);
        }
        return 0;
    }

    static void setImmuneTime(LivingEntity enchanted, int time) {
        if (PetEnchantmentUtils.hasEnchant(enchanted, PHEnchantments.IMMUNITY_FRAME)) {
            CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
            tag.putInt(PetDataKeys.IMMUNITY_TIME_TAG, time);
            PetDataSync.sync(enchanted, tag);
        }
    }

    /** 冰冻倒计时：走 PH_FROZEN_TIME 独立 int 数据槽（frost_fang 命中时由伤害管线写入），非数据槽实现者恒 0 */
    static int getFrozenTime(LivingEntity enchanted) {
        if (enchanted instanceof PetSyncDataEntity holder) {
            return holder.ph_getFrozenTime();
        }
        return 0;
    }

    static void setFrozenTimeTag(LivingEntity enchanted, int time) {
        if (enchanted instanceof PetSyncDataEntity holder) {
            holder.ph_setFrozenTime(time);
        }
    }

    static float getFallDistance(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getFloat(PetDataKeys.FALL_DISTANCE_SYNC);
    }

    static void setFallDistance(LivingEntity enchanted, float dist) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putFloat(PetDataKeys.FALL_DISTANCE_SYNC, dist);
        PetDataSync.sync(enchanted, tag);

    }

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

    /** 绑定的宠物床坐标；未绑定（HasPetBed=false）返回 {@code null}。死亡复活/罗盘传送都以此为准 */
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

    static void removePetBedPos(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(PetDataKeys.HAS_PET_BED, false);
        PetDataSync.sync(enchanted, tag);
    }

    /** 灯笼/复活需要维度信息：未记录时默认主世界。写入路径 setPetBedDimension 经 sync 同步 */
    static String getPetBedDimension(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return !tag.contains(PetDataKeys.PET_BED_DIMENSION) ? "minecraft:overworld" : tag.getString(PetDataKeys.PET_BED_DIMENSION);
    }

    static void setPetBedDimension(LivingEntity enchanted, String dimension) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putString(PetDataKeys.PET_BED_DIMENSION, dimension);
        PetDataSync.sync(enchanted, tag);
    }
}
