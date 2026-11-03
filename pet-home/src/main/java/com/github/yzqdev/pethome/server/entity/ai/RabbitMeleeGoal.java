package com.github.yzqdev.pethome.server.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.animal.Animal;

/** 宠物兔的近战攻击 Goal：原版兔子没有攻击 AI，此处给驯服后的兔子 补上近战能力，攻击距离按兔子体型微调（基础 4 格 + 宽度），比原版近战更远。 */
public class RabbitMeleeGoal extends MeleeAttackGoal {
  public RabbitMeleeGoal(Animal animal) {
    super(animal, 1.4D, true);
  }

  protected double getAttackReachSqr(LivingEntity animal) {
    return (double) (4.0F + animal.getBbWidth());
  }
}
