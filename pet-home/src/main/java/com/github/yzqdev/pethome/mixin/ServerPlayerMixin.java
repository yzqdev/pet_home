package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 注入：{@code ServerPlayer#changeDimension} 的 TAIL。玩家跨维度成功后回调 {@link
 * ServerEvent#onEntityTravelToDimension}（仅服务端），供往生灯笼等跨维度宠物数据处理使用。
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
  @Inject(
      method = {"changeDimension"},
      at = {@At("TAIL")})
  public void onChangeDimension(
      DimensionTransition transition, CallbackInfoReturnable<Entity> cir) {
    ServerPlayer player = (ServerPlayer) ((Object) this);
    if (transition != null && transition.newLevel() != null) {
      ServerEvent.onEntityTravelToDimension(player, transition.newLevel());
    }
  }
}
