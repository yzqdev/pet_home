package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 替代 Forge EntityTeleportEvent / EntityTravelToDimensionEvent：
 * 玩家传送时把有"拴魂"类附魔的宠物一起带走。
 */
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;", at = @At("HEAD"))
    private void ph_onChangeDimension(ServerLevel targetLevel, CallbackInfoReturnable<Entity> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        ServerEvent.onEntityTravelToDimension(self, targetLevel);
    }

    @Inject(method = "teleportTo(DDD)V", at = @At("HEAD"))
    private void ph_onTeleportTo(double x, double y, double z, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        ServerEvent.onEntityTeleport(self, new net.minecraft.world.phys.Vec3(x, y, z));
    }
}
