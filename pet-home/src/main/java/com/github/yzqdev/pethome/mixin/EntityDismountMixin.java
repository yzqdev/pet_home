package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 替代 Forge EntityMountEvent：阻止从存活的巨型泡泡上主动下来。
 */
@Mixin(Entity.class)
public class EntityDismountMixin {

    @Inject(method = "stopRiding()V", at = @At("HEAD"), cancellable = true)
    private void ph_onDismount(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (ServerEvent.onEntityMount(self)) {
            ci.cancel();
        }
    }
}
