package com.github.yzqdev.pethome.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.phys.AABB;

/**
 * 生物目标与范围小工具。
 */
public class LivingUtils {
    /**
     * 让 {@code monster} 把 {@code otherMonster} 设为攻击目标，按类型走不同通道：
     * 猪灵（脑驱动）写 {@code ATTACK_TARGET} 记忆 + target/lastHurt；
     * {@code NeutralMob} 只 {@code setTarget}（不记仇）；其余设置 target + lastHurtMob。
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

    public static AABB getBoundingBoxAroundEntity(Entity entity, double radius) {
        return new AABB(entity.getX() - radius, entity.getY() - radius, entity.getZ() - radius, entity.getX() + radius, entity.getY() + radius, entity.getZ() + radius);
    }

}
