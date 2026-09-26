package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Inject(method = {"explode"}, at = {@At("HEAD")}, cancellable = true)
    public void onExplode(CallbackInfo ci) {
        Explosion explosion = (Explosion) ((Object) this);
        if (ServerEvent.onExplosionStart(explosion)) {
            ci.cancel();
        }
    }
}
