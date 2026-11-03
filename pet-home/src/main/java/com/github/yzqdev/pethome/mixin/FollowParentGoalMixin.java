package com.github.yzqdev.pethome.mixin;


import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.util.IComandableMob;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code FollowParentGoal} 适配三态指令：处于停留（1）/跟随（2）状态的
 * 幼年宠物不再追随父母（"游走"0 保持原版行为）。
 */
@Mixin(FollowParentGoal.class)
public class FollowParentGoalMixin {

    @Shadow
    @Final
    private Animal animal;

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"canUse()Z"},
            cancellable = true
    )
    private void di_canUse(CallbackInfoReturnable<Boolean> cir){
        if(animal instanceof IComandableMob commandableMob && commandableMob.getCommand() != 0 && PetHomeConfig.trinaryCommandSystem){
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
        if(animal instanceof IComandableMob commandableMob && commandableMob.getCommand() != 0 && PetHomeConfig.trinaryCommandSystem){
            cir.setReturnValue(false);
        }
    }
}
