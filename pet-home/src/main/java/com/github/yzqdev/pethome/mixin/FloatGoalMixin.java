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
 * Mixin 注入：{@code FloatGoal#canUse} 的 HEAD（可取消）。带两栖（AMPHIBIOUS）项圈附魔的宠物禁用原版“漂浮在水上” 目标，让它能完整潜入水中活动，由
 * {@code AmphibiousPathNavigation} 接管水下寻路。
 */
@Mixin(FloatGoal.class)
public abstract class FloatGoalMixin extends Goal {

  @Shadow @Final private Mob mob;

  @Inject(
      at = {@At("HEAD")},
      remap = true,
      method = {"canUse()Z"},
      cancellable = true)
  private void di_canUse(CallbackInfoReturnable<Boolean> cir) {
    if (TameableUtils.isTamed(mob) && TameableUtils.hasEnchant(mob, PHEnchantments.AMPHIBIOUS)) {
      cir.setReturnValue(false);
    }
  }
}
