package com.github.yzqdev.pethome.server.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;


/**
 * 对应 NeoForge {@code LivingIncomingDamageEvent}：{@code LivingEntity#hurtServer} HEAD
 * 阶段的伤害上下文，可改写 {@code amount} 或取消（{@code LivingEntityHurtMixin} 构造，
 * {@code EntityHurtHandler.onLivingDamage} 消费）。
 */
public final class IncomingDamage {

    private final LivingEntity entity;
    private final DamageSource source;
    private float amount;
    private boolean canceled;

    public IncomingDamage(LivingEntity entity, DamageSource source, float amount) {
        this.entity = entity;
        this.source = source;
        this.amount = amount;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public DamageSource getSource() {
        return source;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
