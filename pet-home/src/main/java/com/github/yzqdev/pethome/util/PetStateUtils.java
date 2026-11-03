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
 * 宠物状态与计时器：护盾/光环/音波/墙/威吓/无敌/冻结等计时器、暗影挥拳状态、宠物床坐标与维度
 * （原 {@code TameableUtils} 三、五区）。
 */
final class PetStateUtils {

    private PetStateUtils() {}
    /** 烈焰保护的剩余护盾格数（0..2×等级），由 EntityTickHandler 充能、EntityHurtHandler 消耗。 */
    static int getBlazingProtectionBars(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getIntOr(PetDataKeys.BLAZING_PROTECTION_BARS, 0);
    }

    /** 烈焰保护下一格护盾的充能倒计时（tick）。 */
    static int getBlazingProtectionCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getIntOr(PetDataKeys.BLAZING_PROTECTION_COOLDOWN, 0);
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

    /** 治愈光环相位计时器：&gt;0 施法中、0 待机、&lt;0 冷却（EntityTickHandler 里 0→200→-600..-1200→0 循环）。存独立 int 附件而非 citadel tag。 */
    static int getHealingAuraTime(LivingEntity enchanted) {
        Integer current = enchanted.getExistingDataOrNull(PHAttachments.HEALING_AURA_TIME);
        return current == null ? 0 : current;
    }

    /** 写治愈光环计时器。附件 setData 无内容判等，值不变时直接返回避免每 tick 空广播。 */
    static void setHealingAuraTime(LivingEntity enchanted, int time) {
        // 独立 int 附件（PHAttachments.HEALING_AURA_TIME）：每 tick 只广播小整数；
        // 附件 setData 无判等，值没变就不写，避免空广播
        Integer current = enchanted.getExistingDataOrNull(PHAttachments.HEALING_AURA_TIME);
        if (current != null && current == time) {
            return;
        }
        enchanted.setData(PHAttachments.HEALING_AURA_TIME, time);
    }

    /** 音波轰击的下次可施放时间戳（level game time，非倒计时）。 */
    static long getSonicboomAuraTime(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getLongOr(PetDataKeys.Sonic_boom_TIME, 0L);
    }

    static void setSonicboomAuraTime(LivingEntity enchanted, long time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putLong(PetDataKeys.Sonic_boom_TIME, time);
        PetDataSync.sync(enchanted, tag);
    }

    /** 治愈光环的「受击冲能」标记（宠物或主人附近受伤时置 true，下一 tick 提前进入施法相位）。 */
    static boolean getHealingAuraImpulse(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getBooleanOr(PetDataKeys.HEALING_AURA_IMPULSE, false);
    }

    static void setHealingAuraImpulse(LivingEntity enchanted, boolean impulse) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(PetDataKeys.HEALING_AURA_IMPULSE, impulse);
        PetDataSync.sync(enchanted, tag);
    }

    /** 念力墙冷却倒计时（tick），EntityTickHandler 控制组递减。 */
    static int getPsychicWallCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getIntOr(PetDataKeys.PSYCHIC_WALL_COOLDOWN, 0);
    }

    static void setPsychicWallCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PetDataKeys.PSYCHIC_WALL_COOLDOWN, time);
        PetDataSync.sync(enchanted, tag);
    }

    /** 暗影手/引力共用目标槽：存目标实体 id（-1 无），读写都经 citadel tag 同步给客户端渲染。 */
    static int getPetAttackTargetID(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return !tag.contains(PetDataKeys.ATTACK_TARGET_ENTITY) ? -1 : tag.getIntOr(PetDataKeys.ATTACK_TARGET_ENTITY, 0);
    }

    /** 把 id 解析为目标实体；id 无效或实体已卸载返回 {@code null}。 */
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

    /** intimidation（威吓）剩余威吓 tick 数；&gt;0 期间只驱赶不重触发（scareRandomMonsters 里递减）。 */
    static int getIntimidationCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getIntOr(PetDataKeys.INTIMIDATION_COOLDOWN, 0);
    }

    static void setIntimidationCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PetDataKeys.INTIMIDATION_COOLDOWN, time);
        PetDataSync.sync(enchanted, tag);
    }

    /** immunity_frame 剩余无敌 tick（EntityInvulnerabilityCheck 消费）；未戴该附魔恒 0。 */
    static int getImmuneTime(LivingEntity enchanted) {
        if (PetEnchantmentUtils.hasEnchant(enchanted, PHEnchantments.IMMUNITY_FRAME)) {
            CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
            return tag.getIntOr(PetDataKeys.IMMUNITY_TIME_TAG, 0);
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

    /** frost_fang 打在受害者身上的剩余冻结时间（独立 int 附件 {@link PHAttachments#FROZEN_TIME}，渲染冰冻覆盖层用）。 */
    static int getFrozenTime(LivingEntity enchanted) {
        Integer current = enchanted.getExistingDataOrNull(PHAttachments.FROZEN_TIME);
        return current == null ? 0 : current;
    }

    /** 写冻结计时（EntityHurtHandler 在 frost_fang 命中时置 60）；值相等直接返回，避免每 tick 空广播。 */
    static void setFrozenTimeTag(LivingEntity enchanted, int time) {
        Integer current = enchanted.getExistingDataOrNull(PHAttachments.FROZEN_TIME);
        if (current != null && current == time) {
            return;
        }
        enchanted.setData(PHAttachments.FROZEN_TIME, time);
    }

    /** void_cloud 缓降期间把下落距离同步进 citadel tag（服务端写、客户端读，供粒子/渲染层使用）。 */
    static float getFallDistance(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getFloatOr(PetDataKeys.FALL_DISTANCE_SYNC, 0.0F);
    }

    static void setFallDistance(LivingEntity enchanted, float dist) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putFloat(PetDataKeys.FALL_DISTANCE_SYNC, dist);
        PetDataSync.sync(enchanted, tag);

    }

    /** 暗影手出拳间隔倒计时（tick，出拳置 5 递减）。 */
    static int getShadowPunchCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getIntOr(PetDataKeys.SHADOW_PUNCH_COOLDOWN, 0);
    }

    static void setShadowPunchCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PetDataKeys.SHADOW_PUNCH_COOLDOWN, time);
        PetDataSync.sync(enchanted, tag);
    }

    /** 每只暗影手的出拳进度（0..10，数组按附魔等级对应各只手）；同步到客户端供 {@code ClientGameEvents.shadowPunchRenderData} 插值。 */
    static int[] getShadowPunchTimes(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getIntArray(PetDataKeys.SHADOW_PUNCH_TIMES).orElse(new int[0]);
    }

    static void setShadowPunchTimes(LivingEntity enchanted, int[] times) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putIntArray(PetDataKeys.SHADOW_PUNCH_TIMES, times);
        PetDataSync.sync(enchanted, tag);
    }

    /** 各只暗影手是否处于出拳中（0=收回，1=打出），与 {@link #getShadowPunchTimes} 一起构成状态机。 */
    static void setShadowPunchStriking(LivingEntity enchanted, int[] times) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putIntArray(PetDataKeys.SHADOW_PUNCH_STRIKING, times);
        PetDataSync.sync(enchanted, tag);
    }

    static int[] getShadowPunchStriking(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getIntArray(PetDataKeys.SHADOW_PUNCH_STRIKING).orElse(new int[0]);
    }

    /** 宠物绑定的宠物床坐标（存 citadel tag，{@code HasPetBed=false} 或坐标缺失时返回 {@code null}）。 */
    @Nullable
    static BlockPos getPetBedPos(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        if (tag.getBooleanOr(PetDataKeys.HAS_PET_BED, false) && tag.contains(PetDataKeys.PET_BED_X) && tag.contains(PetDataKeys.PET_BED_Y) && tag.contains(PetDataKeys.PET_BED_Z)) {
            return new BlockPos(tag.getIntOr(PetDataKeys.PET_BED_X, 0), tag.getIntOr(PetDataKeys.PET_BED_Y, 0), tag.getIntOr(PetDataKeys.PET_BED_Z, 0));
        }
        return null;
    }

    /** 绑定宠物床（站上宠物床时调用）；只写坐标，维度由 {@link #setPetBedDimension} 单独写入。 */
    static void setPetBedPos(LivingEntity enchanted, BlockPos petBed) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(PetDataKeys.HAS_PET_BED, true);
        tag.putInt(PetDataKeys.PET_BED_X, petBed.getX());
        tag.putInt(PetDataKeys.PET_BED_Y, petBed.getY());
        tag.putInt(PetDataKeys.PET_BED_Z, petBed.getZ());
        PetDataSync.sync(enchanted, tag);
    }

    /** 解绑宠物床（床被拆/放生时调用）；只置 {@code HAS_PET_BED=false}，坐标键残留但 {@link #getPetBedPos} 会返回 null。 */
    static void removePetBedPos(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(PetDataKeys.HAS_PET_BED, false);
        PetDataSync.sync(enchanted, tag);
    }

    /** 宠物床所在维度的字符串形式（如 {@code minecraft:overworld}）；未写入时按主世界处理。 */
    static String getPetBedDimension(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return !tag.contains(PetDataKeys.PET_BED_DIMENSION) ? "minecraft:overworld" : tag.getStringOr(PetDataKeys.PET_BED_DIMENSION, "");
    }

    static void setPetBedDimension(LivingEntity enchanted, String dimension) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putString(PetDataKeys.PET_BED_DIMENSION, dimension);
        PetDataSync.sync(enchanted, tag);
    }
}
