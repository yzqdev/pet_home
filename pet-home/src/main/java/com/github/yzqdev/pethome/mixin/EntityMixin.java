package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.entity.PsychicWallEntity;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 注入：{@code Entity} 上的五个“查询型”方法（均可取消），把宠物附魔/友军关系接入原版基础行为：
 *
 * <ul>
 *   <li>{@code fireImmune}：烈焰保护（FIREPROOF）附魔的宠物整体免疫火焰；
 *   <li>{@code isPushedByFluid}：两栖附魔的宠物不受水流推动，便于水下自主游动；
 *   <li>{@code isAlliedTo}：同一主人的两只宠物互判为友军（目标选择/范围伤害不再误伤自家宠物）；
 *   <li>{@code canCollideWith}：念力墙不与本队实体碰撞，只拦敌人；
 *   <li>{@code dismountsUnderwater}：两栖附魔的宠物被骑乘进入水中不再自动下坐骑。
 * </ul>
 */
@Mixin(Entity.class)
public class EntityMixin {

  @Inject(
      method = {"fireImmune()Z"},
      at = @At(value = "HEAD"),
      cancellable = true)
  private void di_isFireImmune(CallbackInfoReturnable<Boolean> cir) {
    Entity us = (Entity) ((Object) this);
    if (TameableUtils.isTamed(us)
        && TameableUtils.hasEnchant((LivingEntity) us, PHEnchantments.FIREPROOF)) {
      cir.setReturnValue(true);
    }
  }

  @Inject(
      method = {"isPushedByFluid()Z"},
      at = @At(value = "HEAD"),
      cancellable = true)
  protected void di_pushedByWater(CallbackInfoReturnable<Boolean> cir) {
    if ((Object) this instanceof LivingEntity
        && TameableUtils.isTamed((LivingEntity) (Object) this)
        && TameableUtils.hasEnchant((LivingEntity) (Object) this, PHEnchantments.AMPHIBIOUS)) {
      cir.setReturnValue(false);
    }
  }

  @Inject(
      method = {"isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z"},
      at = @At(value = "HEAD"),
      cancellable = true)
  protected void di_isAlliedTo(Entity other, CallbackInfoReturnable<Boolean> cir) {
    if (TameableUtils.isTamed(other)
        && TameableUtils.isTamed((Entity) (Object) this)
        && TameableUtils.hasSameOwnerAs((LivingEntity) other, (Entity) (Object) this)) {
      cir.setReturnValue(true);
    }
  }

  @Inject(
      method = {"canCollideWith(Lnet/minecraft/world/entity/Entity;)Z"},
      at = @At(value = "HEAD"),
      cancellable = true)
  protected void di_canCollideWith(Entity other, CallbackInfoReturnable<Boolean> cir) {
    if (other instanceof PsychicWallEntity
        && ((PsychicWallEntity) other).isSameTeam((Entity) (Object) this)) {
      cir.setReturnValue(false);
    }
  }

  @Inject(
      method = {"dismountsUnderwater()Z"},
      at = @At(value = "HEAD"),
      cancellable = true)
  protected void di_rideableInWater(CallbackInfoReturnable<Boolean> cir) {
    if ((Object) this instanceof LivingEntity
        && TameableUtils.isTamed((LivingEntity) (Object) this)
        && TameableUtils.hasEnchant((LivingEntity) (Object) this, PHEnchantments.AMPHIBIOUS)) {
      cir.setReturnValue(true);
    }
  }
}
