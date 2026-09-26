package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.EntityTickHandler;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public abstract class EntityTickMixin {
    @Inject(method = {"tick"}, at = {@At("HEAD")})
    public void onTick(CallbackInfo ci) {
        Entity entity = (Entity) ((Object) this);
        EntityTickHandler.onLivingUpdate(entity);
    }
}
