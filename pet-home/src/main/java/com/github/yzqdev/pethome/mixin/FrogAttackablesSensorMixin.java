package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.FrogAttackablesSensor;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 注入：{@code FrogAttackablesSensor#isMatchingEntity} 的 HEAD（可取消）。已驯服青蛙（或同样走该 sensor
 * 的美西螈）的猎物判定改由 {@link ModifiedToBeTameable#isValidAttackTarget} 决定；非有效目标时 强制返回
 * false，避免驯服青蛙把主人的其它宠物当猎物。
 */
@Mixin(FrogAttackablesSensor.class)
public class FrogAttackablesSensorMixin {

  @Inject(
      method = {
        "Lnet/minecraft/world/entity/ai/sensing/FrogAttackablesSensor;isMatchingEntity(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"
      },
      remap = true,
      at = @At(value = "HEAD"),
      cancellable = true)
  private void di_isHuntTarget(
      LivingEntity frog, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
    if (frog instanceof ModifiedToBeTameable tamed
        && tamed.getTameOwner() != null
        && !tamed.isStayingStill()) {
      if (tamed.isValidAttackTarget(livingEntity)) {
        cir.setReturnValue(true);
      } else if (tamed instanceof Axolotl) {
        cir.setReturnValue(false);
      }
    }
  }
}
