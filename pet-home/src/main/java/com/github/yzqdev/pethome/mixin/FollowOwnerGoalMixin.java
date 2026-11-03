package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 注入：{@code FollowOwnerGoal#canUse} / {@code #canContinueToUse}（可取消）与 {@code #tick}。
 *
 * <ul>
 *   <li>三态指令系统开启时，仅“跟随”（指令=2）的宠物才允许启用/维持原版跟随目标；
 *   <li>带两栖（AMPHIBIOUS）附魔的宠物在水中且距主人 12 格内时，绕过原版 tick 寻路改为直接导航游泳—— 原版 {@code FollowOwnerGoal}
 *       的水路跟随体验很差（会卡在岸上），这里换成直线导航。
 * </ul>
 */
@Mixin(FollowOwnerGoal.class)
public abstract class FollowOwnerGoalMixin extends Goal {

  @Shadow @Final private TamableAnimal tamable;
  @Shadow private LivingEntity owner;
  @Shadow @Final private float stopDistance;

  @Shadow @Final private double speedModifier;

  @Inject(
      at = {@At("HEAD")},
      remap = true,
      method = {"canUse()Z"},
      cancellable = true)
  private void di_canUse(CallbackInfoReturnable<Boolean> cir) {
    if (tamable instanceof IComandableMob commandableMob
        && commandableMob.getCommand() != 2
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
    if (tamable instanceof IComandableMob commandableMob
        && commandableMob.getCommand() != 2
        && PetHomeConfig.trinaryCommandSystem) {
      cir.setReturnValue(false);
    }
  }

  @Inject(
      at = {@At("HEAD")},
      remap = true,
      method = {"tick()V"},
      cancellable = true)
  private void di_tick(CallbackInfo ci) {
    if (TameableUtils.hasEnchant(tamable, PHEnchantments.AMPHIBIOUS)
        && tamable.isInWaterOrBubble()
        && this.tamable.distanceToSqr(this.owner) < 144.0D) {
      tamable.getNavigation().moveTo(owner, speedModifier);
      ci.cancel();
    }
  }
}
