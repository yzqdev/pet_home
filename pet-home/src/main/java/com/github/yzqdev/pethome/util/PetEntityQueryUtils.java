package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.datagen.PHEnchantments;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * 实体查询与传送筛选（package-private）：附近实体查询、治疗目标筛选、传送条件判定。
 *
 * <p>本类只做「按条件挑出实体」，不做任何游戏行为（伤害/治疗/移动由 {@link PetEnchantmentBehaviorUtils} 与事件处理器负责），
 * 也不做归属判定本身（委托 {@link PetOwnershipUtils#hasSameOwnerAs}），保证判定逻辑只有一份。
 */
final class PetEntityQueryUtils {

    private PetEntityQueryUtils() {
    }

    /** {@code range} 格内的非玩家活体（不含自身），吸血平摊/音波群攻等范围效果的基础查询。 */
    static List<LivingEntity> getNearbyMobs(LivingEntity entity, double range) {
        // 计算范围边界框
        AABB area = new AABB(
                entity.getX() - range,
                entity.getY() - range,
                entity.getZ() - range,
                entity.getX() + range,
                entity.getY() + range,
                entity.getZ() + range);

        // 获取范围内的活体生物（排除自身）
        return entity.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != entity && !(e instanceof Player));
    }

    /** 光环可治疗的目标：4 格内同主人（双向）且未满血的活体，含主人本人。 */
    static List<LivingEntity> getAuraHealables(LivingEntity pet) {
        Predicate<Entity> hurtAndOnTeam = (animal) -> PetOwnershipUtils.hasSameOwnerAs((LivingEntity) animal, pet)
                && animal.distanceTo(pet) < 4
                && ((LivingEntity) animal).getHealth() < ((LivingEntity) animal).getMaxHealth();
        return pet.level()
                .getEntitiesOfClass(
                        LivingEntity.class,
                        pet.getBoundingBox().inflate(4, 4, 4),
                        EntitySelector.NO_SPECTATORS.and(hurtAndOnTeam));
    }

    /** 16 格内「有 healing_aura 且当前空闲（healingAuraTime==0）」的同主人宠物，用于受击冲能（EntityHurtHandler）。 */
    static List<LivingEntity> getNearbyHealers(LivingEntity hurtOwner) {
        Predicate<Entity> healer = (animal) -> PetOwnershipUtils.hasSameOwnerAs((LivingEntity) animal, hurtOwner)
                && PetEnchantmentUtils.hasEnchant((LivingEntity) animal, PHEnchantments.HEALING_AURA)
                && PetStateUtils.getHealingAuraTime((LivingEntity) animal) == 0;
        return hurtOwner
                .level()
                .getEntitiesOfClass(
                        LivingEntity.class,
                        hurtOwner.getBoundingBox().inflate(16, 4, 16),
                        EntitySelector.NO_SPECTATORS.and(healer));
    }

    /**
     * tethered_teleport 附魔是否允许这只宠物跟着 owner 一起传送（ServerEvent.teleportNearbyPets 的筛选条件）：
     * 三态宠物需处于跟随态（command==2）；原版可驯服动物需未坐下且在 10 格内。
     */
    static boolean isValidTeleporter(LivingEntity owner, Mob animal) {
        if (PetEnchantmentUtils.hasEnchant(animal, PHEnchantments.TETHERED_TELEPORT)) {
            if (animal instanceof IComandableMob commandableMob) {
                return commandableMob.getCommand() == PetDataKeys.COMMAND_FOLLOW;
            } else if (animal instanceof TamableAnimal tame) {
                return !tame.isOrderedToSit() && animal.distanceTo(owner) < 10;
            }
        }
        return false;
    }
}
