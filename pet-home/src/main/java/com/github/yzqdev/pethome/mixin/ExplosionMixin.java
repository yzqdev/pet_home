package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 替代 Forge ExplosionEvent.Start（偏移结界附魔拆除爆炸）。
 */
@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow
    private Level level;

    /** 注入 {@code Explosion#explode} 头部（可取消）：爆炸源处在宠物的偏移结界（DEFLECTION_SHIELD）范围内时直接取消爆炸。 */
    @Inject(method = "explode()V", at = @At("HEAD"), cancellable = true)
    private void ph_onExplosionStart(CallbackInfo ci) {
        Explosion self = (Explosion) (Object) this;
        if (ServerEvent.onExplosionStart(this.level, self)) {
            ci.cancel();
        }
    }
}
