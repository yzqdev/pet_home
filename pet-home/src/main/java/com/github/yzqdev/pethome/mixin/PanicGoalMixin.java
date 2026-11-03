package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code PanicGoal} 注入：已驯服宠物不再因受伤恐慌逃窜
 * （受惊乱跑会破坏"停留"状态与主人间的位置关系）。
 */
@Mixin(PanicGoal.class)
public class PanicGoalMixin {

    @Shadow
    @Final
    private PathfinderMob mob;

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"canUse()Z"},
            cancellable = true
    )
    private void di_canUse(CallbackInfoReturnable<Boolean> cir){
        if(mob instanceof ModifiedToBeTameable mob && mob.isTame()){
            cir.setReturnValue(false);
        }
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"canContinueToUse()Z"},
            cancellable = true
    )
    private void di_canContinueToUse(CallbackInfoReturnable<Boolean> cir){
        if(mob instanceof ModifiedToBeTameable mob && mob.isTame()){
            cir.setReturnValue(false);
        }

    }
}
