package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = {"changeDimension"}, at = {@At("TAIL")})
    public void onChangeDimension(DimensionTransition transition, CallbackInfoReturnable<Entity> cir) {
        ServerPlayer player = (ServerPlayer) ((Object) this);
        if (transition != null && transition.newLevel() != null) {
            ServerEvent.onEntityTravelToDimension(player, transition.newLevel());
        }
    }
}
