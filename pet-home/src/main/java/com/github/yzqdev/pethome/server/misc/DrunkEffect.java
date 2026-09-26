package com.github.yzqdev.pethome.server.misc;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 「混乱」效果（自 1.21 移植）：供 Chaos（混乱之脑）/ Violent（暴力）附魔使用。
 *
 * <p>本身不产生任何属性变化，仅作为标记：被标记的怪物会被
 * {@code EntityTickHandler#tickChaos} 每 10 tick 转向攻击附近的其他怪物。</p>
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
