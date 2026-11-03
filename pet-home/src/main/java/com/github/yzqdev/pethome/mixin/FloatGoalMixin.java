package com.github.yzqdev.pethome.mixin;


import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code FloatGoal.canUse} 注入：携带"两栖"附魔的宠物禁用漂浮目标
 * （会主动下潜，不需要浮水）。
 */
@Mixin(FloatGoal.class)
public abstract class FloatGoalMixin extends Goal {

    @Shadow
    @Final
    private Mob mob;


    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"canUse()Z"},
            cancellable = true
    )
    private void di_canUse(CallbackInfoReturnable<Boolean> cir){
        if(TameableUtils.isTamed(mob) && TameableUtils.hasEnchant(mob, PHEnchantments.AMPHIBIOUS)){
            cir.setReturnValue(false);
        }
    }

}
