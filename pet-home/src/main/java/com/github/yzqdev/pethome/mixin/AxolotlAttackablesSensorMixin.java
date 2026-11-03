package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.AxolotlAttackablesSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 美西螈攻击目标过滤：驯服个体不把主人 / 同主人单位 / 玩家列为攻击对象。 */
@Mixin(AxolotlAttackablesSensor.class)
public class AxolotlAttackablesSensorMixin {

    /** 注入 {@code AxolotlAttackablesSensor#isMatchingEntity} 头部（可取消）：驯服美西螈的猎物判定改走 {@code ModifiedToBeTameable#isValidAttackTarget}（非停留状态下由宠物规则裁决）。 */
    @Inject(
            method = {"Lnet/minecraft/world/entity/ai/sensing/AxolotlAttackablesSensor;isMatchingEntity(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void di_isHuntTarget(LivingEntity axolotl, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if(axolotl instanceof ModifiedToBeTameable tamed && tamed.getTameOwner() != null && !tamed.isStayingStill()){
            if(tamed.isValidAttackTarget(livingEntity)){
                cir.setReturnValue(true);
            }else{
                cir.setReturnValue(false);
            }
        }
    }
}
