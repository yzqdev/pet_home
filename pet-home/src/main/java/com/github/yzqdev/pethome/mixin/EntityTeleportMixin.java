package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.event.ServerEventContexts;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 传送带宠（替代 NeoForge 的 {@code EntityTeleportEvent}）。
 *
 * <p>玩家传送时把附近的宠物一起带过去（见 {@code ServerEvent.teleportNearbyPets}）。</p>
 *
 * <p>{@code require = 0}：{@code teleportTo(DDD)V} 是 26.1 上玩家 /tp 走的入口，
 * 若签名变化只会静默失效，不会让模组启动崩溃。</p>
 */
@Mixin(Entity.class)
public abstract class EntityTeleportMixin {

    @Inject(method = "teleportTo(DDD)V", at = @At("HEAD"), require = 0)
    private void pethome$beforeTeleport(double x, double y, double z, CallbackInfo ci) {
        if (!((Object) this instanceof Player player)) {
            return;
        }
        ServerEventContexts.EntityTeleportEvent event =
                new ServerEventContexts.EntityTeleportEvent(player, player.position(), new Vec3(x, y, z));
        ServerEvent.onEntityTeleport(event);
    }
}
