package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.EntityHurtHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {
    @Unique
    private static DamageSource pethome$currentSource;

    @Inject(method = {"actuallyHurt"}, at = {@At("HEAD")})
    public void pethome$captureSource(DamageSource source, float amount, CallbackInfo ci) {
        pethome$currentSource = source;
    }

    @ModifyVariable(method = {"actuallyHurt"}, at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public float pethome$violent(float value) {
        DamageSource source = pethome$currentSource;
        if (source == null) {
            return value;
        }
        LivingEntity entity = (LivingEntity) ((Object) this);
        return EntityHurtHandler.onDamagePre(entity, source, value);
    }
}
