package com.github.yzqdev.pethome.server.misc;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * {@code drunk} 药水效果（非受益）：本身无状态效果，行为侧由
 * {@code EntityTickHandler.mobTick} 消费——喝醉的怪每 5 tick 一次效果 tick 周期，
 * 10 格内随机换攻击目标并冒问号粒子。
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