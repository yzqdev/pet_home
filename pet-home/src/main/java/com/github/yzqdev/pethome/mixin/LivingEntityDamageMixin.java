package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.EntityHurtHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 伤害预处理入口：{@code actuallyHurt} 的 amount 需改写而源无直接挂点，先用 {@code @Inject} 把 {@code DamageSource}
 * 暂存到 @Unique 静态字段，再用 {@code @ModifyVariable}(ordinal 0, argsOnly) 把 amount 改写为 {@code
 * EntityHurtHandler.onDamagePre} 的返回值（防御附魔减免 / 友军保护等）。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {
  @Unique private static DamageSource pethome$currentSource;

  @Inject(
      method = {"actuallyHurt"},
      at = {@At("HEAD")})
  public void pethome$captureSource(DamageSource source, float amount, CallbackInfo ci) {
    pethome$currentSource = source;
  }

  @ModifyVariable(
      method = {"actuallyHurt"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true)
  public float pethome$violent(float value) {
    DamageSource source = pethome$currentSource;
    if (source == null) {
      return value;
    }
    LivingEntity entity = (LivingEntity) ((Object) this);
    return EntityHurtHandler.onDamagePre(entity, source, value);
  }
}
