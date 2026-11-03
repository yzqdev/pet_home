package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 注入：{@code AgeableMob#isBaby} 的 HEAD（可取消）。带不成熟诅咒（IMMATURITY_CURSE）项圈附魔的宠物
 * 强制保持幼年形态——永远长不大，是纯外观/定位类“诅咒”附魔。
 */
@Mixin(AgeableMob.class)
public class AgeableMobMixin extends PathfinderMob {

  protected AgeableMobMixin(EntityType<? extends PathfinderMob> type, Level level) {
    super(type, level);
  }

  @Inject(
      at = {@At("HEAD")},
      remap = true,
      method = {"isBaby()Z"},
      cancellable = true)
  private void di_isBaby(CallbackInfoReturnable<Boolean> cir) {
    if (TameableUtils.isTamed(this)
        && TameableUtils.hasEnchant(this, PHEnchantments.IMMATURITY_CURSE)) {
      cir.setReturnValue(true);
    }
  }
}
