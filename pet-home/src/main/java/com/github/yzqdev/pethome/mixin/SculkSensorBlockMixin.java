package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * muffled（安静，自 1.20 移植）：带 muffled 的宠物踩上幽匿感测体不触发振动（{@code gameEvent} 已被 EntityMixin 拦截， 此为双保险，直接取消
 * stepOn）。
 */
@Mixin(SculkSensorBlock.class)
public class SculkSensorBlockMixin {

  @Inject(
      cancellable = true,
      at = @At("HEAD"),
      method =
          "stepOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/Entity;)V")
  private void di_onStepOn(
      Level level, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
    if (entity instanceof LivingEntity living
        && TameableUtils.isTamed(living)
        && TameableUtils.hasEnchant(living, PHEnchantments.MUFFLED)) {
      ci.cancel();
    }
  }
}
