package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 替代 Forge ProjectileImpactEvent（弹射物命中宠物时反弹/跳过）。
 */
@Mixin(Projectile.class)
public class ProjectileMixin {

    @Inject(method = "onHit(Lnet/minecraft/world/phys/HitResult;)V", at = @At("HEAD"), cancellable = true)
    private void ph_onProjectileImpact(HitResult result, CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        if (ServerEvent.onProjectileImpactEvent(self, result)) {
            ci.cancel();
        }
    }
}
