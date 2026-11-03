package com.github.yzqdev.pethome.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.phys.AABB;

/** 自 1.21 移植：混乱之脑的怪物目标工具 */
public class LivingUtils {
    /** 让一只怪物把另一只怪物设为攻击目标：猪灵走 Brain 记忆，中性生物走 {@code NeutralMob}，其余直接 setTarget。 */
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

    /** 以实体为中心取半径 radius 的正方体检测盒（混乱之脑索敌范围）。 */
    public static AABB getBoundingBoxAroundEntity(Entity entity, double radius) {
        return new AABB(entity.getX() - radius, entity.getY() - radius, entity.getZ() - radius, entity.getX() + radius, entity.getY() + radius, entity.getZ() + radius);
    }

}
