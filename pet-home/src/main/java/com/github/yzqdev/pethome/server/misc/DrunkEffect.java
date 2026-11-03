package com.github.yzqdev.pethome.server.misc;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * {@code pet_home:drunk} 醉酒效果（非受益）：效果本体无行为，怪物的"醉酒乱打"由 {@code EntityTickHandler.mobTick}
 * 按该效果存在与否驱动；每 5 tick 才应用一次效果 tick。
 */
public class DrunkEffect extends MobEffect {
  public DrunkEffect(MobEffectCategory type, int color, boolean isInstant) {
    super(type, color);
  }

  @Override
  public boolean shouldApplyEffectTickThisTick(int tick, int amp) {
    if (tick % 5 == 0) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean isBeneficial() {
    return false;
  }
}
