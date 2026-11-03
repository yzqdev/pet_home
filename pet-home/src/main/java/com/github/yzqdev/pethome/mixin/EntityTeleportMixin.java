package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入：{@code ServerPlayer#teleportTo(ServerLevel,double,double,double,float,float)} 的 HEAD。
 * 1.21 的 /tp（{@code TeleportCommand.performTeleport}）对玩家最终汇入此方法：同维度分支走
 * {@code connection.teleport}，跨维度分支走 {@code changeDimension}（由 {@link ServerPlayerMixin} 挂 TAIL 处理）。
 * 旧入口 {@code Entity#teleportTo(DDD)V} 在 1.21 已不被 /tp 经过（known-issues 2026-11-03），故拦截点迁移至此。
 * HEAD 时玩家仍在原维度，同维度直传携宠语义不变。
 */
@Mixin(ServerPlayer.class)
public abstract class EntityTeleportMixin {
  @Inject(
      method = {
        "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V"
      },
      at = {@At("HEAD")})
  public void onTeleportTo(
      ServerLevel level, double x, double y, double z, float yRot, float xRot, CallbackInfo ci) {
    ServerPlayer player = (ServerPlayer) ((Object) this);
    if (!player.level().isClientSide()) {
      ServerEvent.onEntityTeleport(player, player.position());
    }
  }
}
