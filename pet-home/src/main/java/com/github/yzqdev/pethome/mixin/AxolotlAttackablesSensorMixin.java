package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.AxolotlAttackablesSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxolotlAttackablesSensor.class)
public class AxolotlAttackablesSensorMixin {

    // 26.1：isMatchingEntity 首参新增 ServerLevel
    @Inject(
            method = {"Lnet/minecraft/world/entity/ai/sensing/AxolotlAttackablesSensor;isMatchingEntity(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void di_isHuntTarget(ServerLevel level, LivingEntity axolotl, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if(axolotl instanceof ModifiedToBeTameable tamed && tamed.getTameOwner() != null && !tamed.isStayingStill()){
            if(tamed.isValidAttackTarget(livingEntity)){
                cir.setReturnValue(true);
            }else{
                cir.setReturnValue(false);
            }
        }
    }
}
