package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.AxolotlAttackablesSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 注入：{@code AxolotlAttackablesSensor#isMatchingEntity} 的 HEAD（可取消）。已驯服美西螈的猎物判定 改由 {@link
 * ModifiedToBeTameable#isValidAttackTarget} 决定——主人在线且非“停留”状态时才主动捕猎， 捕猎目标范围也从原版“鱼类/热带鱼”扩展为宠物攻击目标规则。
 */
@Mixin(AxolotlAttackablesSensor.class)
public class AxolotlAttackablesSensorMixin {

  @Inject(
      method = {
        "Lnet/minecraft/world/entity/ai/sensing/AxolotlAttackablesSensor;isMatchingEntity(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"
      },
      remap = true,
      at = @At(value = "HEAD"),
      cancellable = true)
  private void di_isHuntTarget(
      LivingEntity axolotl, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
    if (axolotl instanceof ModifiedToBeTameable tamed
        && tamed.getTameOwner() != null
        && !tamed.isStayingStill()) {
      if (tamed.isValidAttackTarget(livingEntity)) {
        cir.setReturnValue(true);
      } else {
        cir.setReturnValue(false);
      }
    }
  }
}
