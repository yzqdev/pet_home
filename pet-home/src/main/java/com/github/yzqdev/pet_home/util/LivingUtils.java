package com.github.yzqdev.pet_home.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.phys.AABB;

public class LivingUtils {
    public static void setAttackTarget(Monster monster, Monster otherMonster) {
        if (monster != null && otherMonster != null && monster != otherMonster) {
            if (monster instanceof AbstractPiglin) {
                ((AbstractPiglin) monster).getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, otherMonster);
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
