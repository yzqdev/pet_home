package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.datagen.PHEnchantments;
import java.util.*;
import java.util.function.Predicate;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** 传送与近距离查询：范围内实体筛选、治疗光环目标、范围内的治疗者、传送合法性判定 （原 {@code TameableUtils} 七区）。 */
final class PetEntityQueryUtils {

  private PetEntityQueryUtils() {}

  static List<LivingEntity> getNearbyMobs(LivingEntity entity, double range) {
    // 计算范围边界框
    AABB area =
        new AABB(
            entity.getX() - range,
            entity.getY() - range,
            entity.getZ() - range,
            entity.getX() + range,
            entity.getY() + range,
            entity.getZ() + range);

    // 获取范围内的活体生物（排除自身）
    return entity
        .level()
        .getEntitiesOfClass(LivingEntity.class, area, e -> e != entity && !(e instanceof Player));
  }

  static List<LivingEntity> getAuraHealables(LivingEntity pet) {
    Predicate<Entity> hurtAndOnTeam =
        (animal) ->
            PetOwnershipUtils.hasSameOwnerAs((LivingEntity) animal, pet)
                && animal.distanceTo(pet) < 4
                && ((LivingEntity) animal).getHealth() < ((LivingEntity) animal).getMaxHealth();
    return pet.level()
        .getEntitiesOfClass(
            LivingEntity.class,
            pet.getBoundingBox().inflate(4, 4, 4),
            EntitySelector.NO_SPECTATORS.and(hurtAndOnTeam));
  }

  static List<LivingEntity> getNearbyHealers(LivingEntity hurtOwner) {
    Predicate<Entity> healer =
        (animal) ->
            PetOwnershipUtils.hasSameOwnerAs((LivingEntity) animal, hurtOwner)
                && PetEnchantmentUtils.hasEnchant(
                    (LivingEntity) animal, PHEnchantments.HEALING_AURA)
                && PetStateUtils.getHealingAuraTime((LivingEntity) animal) == 0;
    return hurtOwner
        .level()
        .getEntitiesOfClass(
            LivingEntity.class,
            hurtOwner.getBoundingBox().inflate(16, 4, 16),
            EntitySelector.NO_SPECTATORS.and(healer));
  }

  /**
   * tethered_teleport 的随行者判定（ServerEvent.teleportNearbyPets 的过滤条件）：
   * 需有附魔，且三态跟随（command==2）或原版未坐下且距主人 10 格内。
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
