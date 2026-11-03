package com.github.yzqdev.pethome.server.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.animal.Animal;

/**
 * 兔子的近战攻击目标：驯服后的杀手兔攻击主人目标用。攻击距离按目标宽度加成（兔身位小、
 * 需要贴得更近才判定命中），速度 1.4。
 */
public class RabbitMeleeGoal extends MeleeAttackGoal {
    /** @param animal 驯服后的兔子本体 */
    public RabbitMeleeGoal(Animal animal) {
        super(animal, 1.4D, true);
    }

    protected double getAttackReachSqr(LivingEntity animal) {
        return (double)(4.0F + animal.getBbWidth());
    }
}