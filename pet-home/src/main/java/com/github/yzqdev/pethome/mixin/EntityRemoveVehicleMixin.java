package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.GiantBubbleEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入：{@code Entity#removeVehicle} 的 HEAD（可取消）。巨型泡泡（{@code pet_home:giant_bubble}）承载生物期间
 * 阻止原版卸载载具逻辑，否则乘客会被立即弹出、泡泡包裹效果失效。
 */
@Mixin(Entity.class)
public abstract class EntityRemoveVehicleMixin {
  @Inject(
      method = {"removeVehicle"},
      at = {@At("HEAD")},
      cancellable = true)
  public void onRemoveVehicle(CallbackInfo ci) {
    Entity entity = (Entity) ((Object) this);
    if (entity.getVehicle() instanceof GiantBubbleEntity bubble && bubble.isAlive()) {
      ci.cancel();
    }
  }
}
