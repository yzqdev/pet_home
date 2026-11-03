package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入：{@code Projectile#onHit} 的 HEAD（可取消）。投射物命中前转发 {@link
 * ServerEvent#onProjectileImpactEvent} 判定——偏折护盾类附魔借此拦截射向宠物的箭/三叉戟等投射物。
 */
@Mixin(Projectile.class)
public abstract class ProjectileImpactMixin {
  @Inject(
      method = {"onHit"},
      at = {@At("HEAD")},
      cancellable = true)
  public void onOnHit(HitResult result, CallbackInfo ci) {
    Projectile projectile = (Projectile) ((Object) this);
    if (ServerEvent.onProjectileImpactEvent(projectile, result)) {
      ci.cancel();
    }
  }
}
