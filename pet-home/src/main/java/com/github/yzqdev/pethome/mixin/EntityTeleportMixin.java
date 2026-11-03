package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.event.ServerEventContexts;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 传送带宠（替代 NeoForge 的 {@code EntityTeleportEvent}）。
 *
 * <p>玩家传送时把附近的宠物一起带过去（见 {@code ServerEvent.teleportNearbyPets}）。</p>
 *
 * <p>26.1 的 /tp（{@code TeleportCommand.performTeleport}）走多参 {@code teleportTo(ServerLevel,...)}
 * → {@code teleport(TeleportTransition)}，传送门 {@code changeDimension} 亦汇入该点；
 * 旧入口 {@code teleportTo(DDD)V} 已不被 /tp 经过（known-issues 2026-11-03），故拦截点迁移至此。
 * {@code require = 0}：签名再变时静默失效，不会让模组启动崩溃。</p>
 */
@Mixin(ServerPlayer.class)
public abstract class EntityTeleportMixin {

    @Inject(
            method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At("HEAD"),
            require = 0)
    private void pethome$beforeTeleport(TeleportTransition transition, CallbackInfoReturnable<ServerLevel> cir) {
        if (transition == null) {
            return;
        }
        ServerPlayer player = (ServerPlayer) ((Object) this);
        if (player.level().isClientSide()) {
            return;
        }
        ServerEventContexts.EntityTeleportEvent event =
                new ServerEventContexts.EntityTeleportEvent(player, player.position(), transition.position());
        ServerEvent.onEntityTeleport(event, transition.newLevel());
    }
}
