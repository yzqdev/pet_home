package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public abstract class EntityTeleportMixin {
    @Inject(method = {"teleportTo(DDD)V"}, at = {@At("TAIL")})
    public void onTeleportTo(double x, double y, double z, CallbackInfo ci) {
        Entity entity = (Entity) ((Object) this);
        if (entity instanceof Player && !entity.level().isClientSide()) {
            ServerEvent.onEntityTeleport((Player) entity, entity.position());
        }
    }
}
