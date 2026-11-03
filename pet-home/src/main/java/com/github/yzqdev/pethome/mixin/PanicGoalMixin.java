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
 * Mixin 注入：{@code PanicGoal#canUse} / {@code #canContinueToUse} 的 HEAD（可取消）。已驯服的 {@link
 * ModifiedToBeTameable} 生物不再受惊逃跑——驯服后行为完全由三态指令接管，避免被打时乱窜。
 */
@Mixin(PanicGoal.class)
public class PanicGoalMixin {

  @Shadow @Final private PathfinderMob mob;

  @Inject(
      at = {@At("HEAD")},
      remap = true,
      method = {"canUse()Z"},
      cancellable = true)
  private void di_canUse(CallbackInfoReturnable<Boolean> cir) {
    if (mob instanceof ModifiedToBeTameable mob && mob.isTame()) {
      cir.setReturnValue(false);
    }
  }

  @Inject(
      at = {@At("HEAD")},
      remap = true,
      method = {"canContinueToUse()Z"},
      cancellable = true)
  private void di_canContinueToUse(CallbackInfoReturnable<Boolean> cir) {
    if (mob instanceof ModifiedToBeTameable mob && mob.isTame()) {
      cir.setReturnValue(false);
    }
  }
}
