package com.github.yzqdev.pethome.server.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;


public final class LivingDamageContext {

    private final LivingEntity entity;
    private final DamageSource source;
    private float newDamage;
    private final float originalDamage;
    private final float healthDamage;

    public LivingDamageContext(LivingEntity entity, DamageSource source, float newDamage,
                              float originalDamage, float healthDamage) {
        this.entity = entity;
        this.source = source;
        this.newDamage = newDamage;
        this.originalDamage = originalDamage;
        this.healthDamage = healthDamage;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public DamageSource getSource() {
        return source;
    }

    public float getNewDamage() {
        return newDamage;
    }

    public void setNewDamage(float newDamage) {
        this.newDamage = newDamage;
    }

    /** 修改前的原始伤害值 */
    public float getOriginalDamage() {
        return originalDamage;
    }

    /** 实际扣掉的生命值（仅 Post 阶段有意义） */
    public float getHealthDamage() {
        return healthDamage;
    }
}
