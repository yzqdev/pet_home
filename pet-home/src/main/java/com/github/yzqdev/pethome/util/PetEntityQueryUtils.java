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
 * 传送与近距离查询：范围内实体筛选、治疗光环目标、范围内的治疗者、传送合法性判定
 * （原 {@code TameableUtils} 七区）。
 */
final class PetEntityQueryUtils {

    private PetEntityQueryUtils() {}
    /** {@code range} 格内的非玩家活体（不含自身），吸血平摊/音波群攻等范围效果的基础查询。 */
    static List<LivingEntity> getNearbyMobs(LivingEntity entity, double range) {
        // 计算范围边界框
        AABB area = new AABB(
                entity.getX() - range, entity.getY() - range, entity.getZ() - range,
                entity.getX() + range, entity.getY() + range, entity.getZ() + range
        );

        // 获取范围内的活体生物（排除自身）
        return entity.level().getEntitiesOfClass(LivingEntity.class, area, e ->
                e != entity && !(e instanceof Player)
        );
    }

    /** 光环可治疗的目标：4 格内同主人（双向）且未满血的活体，含主人本人。 */
    static List<LivingEntity> getAuraHealables(LivingEntity pet) {
        Predicate<Entity> hurtAndOnTeam = (animal) -> PetOwnershipUtils.hasSameOwnerAs((LivingEntity) animal, pet) && animal.distanceTo(pet) < 4 && ((LivingEntity) animal).getHealth() < ((LivingEntity) animal).getMaxHealth();
        return pet.level().getEntitiesOfClass(LivingEntity.class, pet.getBoundingBox().inflate(4, 4, 4), EntitySelector.NO_SPECTATORS.and(hurtAndOnTeam));
    }

    /** 16 格内「有 healing_aura 且当前空闲（healingAuraTime==0）」的同主人宠物，用于受击冲能（EntityHurtHandler）。 */
    static List<LivingEntity> getNearbyHealers(LivingEntity hurtOwner) {
        Predicate<Entity> healer = (animal) -> PetOwnershipUtils.hasSameOwnerAs((LivingEntity) animal, hurtOwner) && PetEnchantmentUtils.hasEnchant((LivingEntity) animal, PHEnchantments.HEALING_AURA) && PetStateUtils.getHealingAuraTime((LivingEntity) animal) == 0;
        return hurtOwner.level().getEntitiesOfClass(LivingEntity.class, hurtOwner.getBoundingBox().inflate(16, 4, 16), EntitySelector.NO_SPECTATORS.and(healer));
    }

    /**
     * tethered_teleport 附魔是否允许这只宠物跟着 owner 一起传送（ServerEvent.teleportNearbyPets 的筛选条件）：
     * 三态宠物需处于跟随态（command==2）；原版可驯服动物需未坐下且在 10 格内。
     */
    static boolean isValidTeleporter(LivingEntity owner, Mob animal) {
        if (PetEnchantmentUtils.hasEnchant(animal, PHEnchantments.TETHERED_TELEPORT)) {
            if (animal instanceof IComandableMob commandableMob) {
                return commandableMob.getCommand() == 2;
            } else if (animal instanceof TamableAnimal tame) {
                return !tame.isOrderedToSit() && animal.distanceTo(owner) < 10;
            }
        }
        return false;
    }
}
