package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.FrogAttackablesSensor;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 青蛙攻击目标过滤：驯服个体不把主人 / 同主人单位列为可吞食目标。 */
@Mixin(FrogAttackablesSensor.class)
public class FrogAttackablesSensorMixin {

    /** 注入 {@code FrogAttackablesSensor#isMatchingEntity} 头部（可取消）：驯服青蛙的猎物判定改走 {@code ModifiedToBeTameable#isValidAttackTarget}（非停留状态下由宠物规则裁决；美西螈实例命中此处时一律不可吞食）。 */
    @Inject(
            method = {"Lnet/minecraft/world/entity/ai/sensing/FrogAttackablesSensor;isMatchingEntity(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void di_isHuntTarget(LivingEntity frog, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if(frog instanceof ModifiedToBeTameable tamed && tamed.getTameOwner() != null && !tamed.isStayingStill()){
            if(tamed.isValidAttackTarget(livingEntity)){
                cir.setReturnValue(true);
            }else if(tamed instanceof Axolotl){
                cir.setReturnValue(false);
            }
        }
    }
}
