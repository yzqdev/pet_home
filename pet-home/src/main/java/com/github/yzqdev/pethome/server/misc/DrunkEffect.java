package com.github.yzqdev.pethome.server.misc;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 从 1.21 移植（chaos/violent 附魔依赖的"混乱"效果）。
 * 1.20 的 tick 判定方法为 isDurationEffectTick（对应 1.21 的 shouldApplyEffectTickThisTick）。
 */
public class DrunkEffect extends MobEffect {

    public DrunkEffect(MobEffectCategory type, int color) {
        super(type, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 5 == 0;
    }

    @Override
    public boolean isBeneficial() {
        return false;
    }
}
