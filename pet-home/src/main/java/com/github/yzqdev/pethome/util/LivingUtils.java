package com.github.yzqdev.pethome.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.phys.AABB;

/** 生物目标/包围盒小工具。 */
public class LivingUtils {
  /**
   * 让 {@code monster} 把 {@code otherMonster} 设为攻击目标，按类型走不同 API： 猪灵系走 Brain 的 ATTACK_TARGET
   * 记忆；中立生物（{@code NeutralMob}）走 {@code setTarget}；其余再补 {@code setLastHurtMob} 触发仇恨。威吓/臭名昭著等附魔逻辑使用。
   */
  public static void setAttackTarget(Monster monster, Monster otherMonster) {
    if (monster != null && otherMonster != null && monster != otherMonster) {
      if (monster instanceof AbstractPiglin) {
        (monster).getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, otherMonster);
        monster.setTarget(otherMonster);
        monster.setLastHurtMob(otherMonster);
      } else if (monster instanceof NeutralMob) {
        ((NeutralMob) monster).setTarget(otherMonster);
      } else {
        monster.setTarget(otherMonster);
        monster.setLastHurtMob(otherMonster);
      }
    }
  }

  /** 以实体为中心、边长 {@code 2*radius} 的立方体包围盒（范围附魔索敌用）。 */
  public static AABB getBoundingBoxAroundEntity(Entity entity, double radius) {
    return new AABB(
        entity.getX() - radius,
        entity.getY() - radius,
        entity.getZ() - radius,
        entity.getX() + radius,
        entity.getY() + radius,
        entity.getZ() + radius);
  }
}
