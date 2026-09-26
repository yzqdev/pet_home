package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LivingEntity.class)
public abstract class LivingEntityDropMixin {
    @Inject(method = {"dropAllDeathLoot"}, at = {@At("HEAD")}, cancellable = true)
    public void onDropAllDeathLoot(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) ((Object) this);
        if (TameableUtils.isTamed(entity) && TameableUtils.getPetBedPos(entity) != null) {
            ci.cancel();
        }
    }
}
