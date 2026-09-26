package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.EntityHurtHandler;
import com.github.yzqdev.pethome.server.event.IncomingDamage;
import com.github.yzqdev.pethome.server.event.LivingDamageContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 受击注入（Fabric 侧替代 NeoForge 的 {@code LivingIncomingDamageEvent} /
 * {@code LivingDamageEvent.Pre/Post}）。
 *
 * <p>26.1 的伤害入口是 {@code LivingEntity#hurtServer(ServerLevel, DamageSource, float)}：</p>
 * <ul>
 *   <li>HEAD：先跑「友军保护 + 防御/攻击附魔」的受击处理，再跑 Pre 阶段（暴力附魔改伤害）；</li>
 *   <li>若处理器取消本次受击 → 直接返回 false；</li>
 *   <li>若伤害值被改写 → 用改写后的数值递归调用一次 {@code hurtServer} 应用（静态标志防止再次进入本注入）；</li>
 *   <li>RETURN：跑 Post 阶段（混乱 / 平摊 / 麻痹等命中后效果），并给出实际扣除的生命值。</li>
 * </ul>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityHurtMixin {

    @Unique
    private float pethome$healthBefore;

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void pethome$onIncomingDamage(ServerLevel level, DamageSource source, float amount,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (EntityHurtHandler.isApplyingAdjustedDamage()) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        this.pethome$healthBefore = self.getHealth();

        IncomingDamage incoming = new IncomingDamage(self, source, amount);
        EntityHurtHandler.onLivingDamage(incoming);

        if (incoming.isCanceled()) {
            cir.setReturnValue(false);
            return;
        }

        LivingDamageContext pre = new LivingDamageContext(self, source, incoming.getAmount(), incoming.getAmount(), 0.0F);
        EntityHurtHandler.onEntityHurtPre(pre);

        float finalAmount = pre.getNewDamage();
        if (finalAmount != amount) {
            if (!EntityHurtHandler.beginAdjustedDamage()) {
                cir.setReturnValue(false);
                return;
            }
            try {
                cir.setReturnValue(self.hurtServer(level, source, finalAmount));
            } finally {
                EntityHurtHandler.endAdjustedDamage();
            }
        }
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void pethome$afterDamage(ServerLevel level, DamageSource source, float amount,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (EntityHurtHandler.isApplyingAdjustedDamage() || !cir.getReturnValueZ()) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        float healthDamage = Math.max(0.0F, this.pethome$healthBefore - self.getHealth());
        EntityHurtHandler.onEntityHurt(new LivingDamageContext(self, source, amount, amount, healthDamage));
    }
}
