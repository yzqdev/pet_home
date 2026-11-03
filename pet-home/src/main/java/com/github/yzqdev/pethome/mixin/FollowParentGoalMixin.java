package com.github.yzqdev.pethome.mixin;


import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.entity.IComandableMob;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 跟随亲代 Goal 调整：处于停留/跟随指令（非游走）的宠物不再跟随亲代。 */
@Mixin(FollowParentGoal.class)
public class FollowParentGoalMixin {

    @Shadow
    @Final
    private Animal animal;

    /** 注入 {@code FollowParentGoal#canUse} 头部（可取消）：三态指令开启且宠物非游走态(0)时不跟随亲代。 */
    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"canUse()Z"},
            cancellable = true
    )
    private void di_canUse(CallbackInfoReturnable<Boolean> cir){
        if(animal instanceof IComandableMob commandableMob && commandableMob.getCommand() != 0 && PetHomeMod.CONFIG.trinaryCommandSystem.get()){
            cir.setReturnValue(false);
        }
    }

    /** 注入 {@code FollowParentGoal#canContinueToUse} 头部（可取消）：宠物切到停留/跟随态时中断跟随亲代。 */
    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"canContinueToUse()Z"},
            cancellable = true
    )
    private void di_canContinueToUse(CallbackInfoReturnable<Boolean> cir){
        if(animal instanceof IComandableMob commandableMob && commandableMob.getCommand() != 0 && PetHomeMod.CONFIG.trinaryCommandSystem.get()){
            cir.setReturnValue(false);
        }
    }
}
