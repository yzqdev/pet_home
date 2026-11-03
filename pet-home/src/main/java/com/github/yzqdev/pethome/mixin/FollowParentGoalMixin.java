package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeConfig;
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
 * Mixin 注入：{@code FollowParentGoal#canUse} / {@code #canContinueToUse} 的 HEAD（可取消）。三态指令系统 （{@code
 * PetHomeConfig.trinaryCommandSystem}）开启时，指令非“游走”（≠0）的幼年动物不再跟随父母， 保证“停留/跟随”指令的优先级高于原版育幼行为。
 */
@Mixin(FollowParentGoal.class)
public class FollowParentGoalMixin {

  @Shadow @Final private Animal animal;

  @Inject(
      at = {@At("HEAD")},
      remap = true,
      method = {"canUse()Z"},
      cancellable = true)
  private void di_canUse(CallbackInfoReturnable<Boolean> cir) {
    if (animal instanceof IComandableMob commandableMob
        && commandableMob.getCommand() != 0
        && PetHomeConfig.trinaryCommandSystem) {
      cir.setReturnValue(false);
    }
  }

  @Inject(
      at = {@At("HEAD")},
      remap = true,
      method = {"canContinueToUse()Z"},
      cancellable = true)
  private void di_canContinueToUse(CallbackInfoReturnable<Boolean> cir) {
    if (animal instanceof IComandableMob commandableMob
        && commandableMob.getCommand() != 0
        && PetHomeConfig.trinaryCommandSystem) {
      cir.setReturnValue(false);
    }
  }
}
