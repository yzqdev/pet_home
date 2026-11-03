package com.github.yzqdev.pethome.server.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.animal.Animal;

/**
 * 驯服兔子的近战攻击目标：速度 1.4，即使目标不可见也持续追击；
 * 攻击距离放宽为"4 + 目标碰撞宽度"（兔子体型小，按碰撞箱算出的原版距离够不到）。
 */
public class RabbitMeleeGoal extends MeleeAttackGoal {
    public RabbitMeleeGoal(Animal animal) {
        super(animal, 1.4D, true);
    }

    /** 攻击距离平方：固定 4 + 目标碰撞宽度（覆盖原版按自身宽度平方的计算）。 */
    protected double getAttackReachSqr(LivingEntity animal) {
        return (double)(4.0F + animal.getBbWidth());
    }
}