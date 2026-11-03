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

/** 恐慌 Goal 调整：Mixin 扩展物种驯服后不再恐慌逃跑。 */
@Mixin(PanicGoal.class)
public class PanicGoalMixin {

    @Shadow
    @Final
    private PathfinderMob mob;

    /** 注入 {@code PanicGoal#canUse} 头部（可取消）：Mixin 扩展物种（狐狸/兔/美西螈/青蛙）驯服后不再触发恐慌逃跑。 */
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

    /** 注入 {@code PanicGoal#canContinueToUse} 头部（可取消）：驯服后立即中断进行中的恐慌逃跑。 */
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
