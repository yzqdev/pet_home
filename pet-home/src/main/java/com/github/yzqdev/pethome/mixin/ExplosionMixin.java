package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入：{@code Explosion#explode} 的 HEAD（可取消）。爆炸发生前转发给 {@link ServerEvent#onExplosionStart}
 * 判定——宠物附魔（如防爆类效果）可借此拦截爆炸，返回 {@code true} 时直接取消整个爆炸。
 */
@Mixin(Explosion.class)
public abstract class ExplosionMixin {
  @Inject(
      method = {"explode"},
      at = {@At("HEAD")},
      cancellable = true)
  public void onExplode(CallbackInfo ci) {
    Explosion explosion = (Explosion) ((Object) this);
    if (ServerEvent.onExplosionStart(explosion)) {
      ci.cancel();
    }
  }
}
