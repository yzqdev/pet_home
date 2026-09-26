package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.event.ServerEventContexts;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 投掷物命中（替代 NeoForge 的 {@code ProjectileImpactEvent}）。
 *
 * <p>语义：宠物身上的「偏转护盾」附魔会把射向宠物的箭矢反弹给攻击者。</p>
 *
 * <p>{@code require = 0}：签名变化时只静默失效。</p>
 */
@Mixin(Projectile.class)
public abstract class ProjectileImpactMixin {

    @Inject(method = "onHit(Lnet/minecraft/world/phys/HitResult;)V", at = @At("HEAD"), require = 0)
    private void pethome$onProjectileImpact(HitResult hitResult, CallbackInfo ci) {
        ServerEventContexts.ProjectileImpactEvent event =
                new ServerEventContexts.ProjectileImpactEvent((Projectile) (Object) this, hitResult);
        ServerEvent.onProjectileImpactEvent(event);
    }
}
