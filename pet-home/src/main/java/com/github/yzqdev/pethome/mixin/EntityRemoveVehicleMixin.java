package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.GiantBubbleEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public abstract class EntityRemoveVehicleMixin {
    @Inject(method = {"removeVehicle"}, at = {@At("HEAD")}, cancellable = true)
    public void onRemoveVehicle(CallbackInfo ci) {
        Entity entity = (Entity) ((Object) this);
        if (entity.getVehicle() instanceof GiantBubbleEntity bubble && bubble.isAlive()) {
            ci.cancel();
        }
    }
}
