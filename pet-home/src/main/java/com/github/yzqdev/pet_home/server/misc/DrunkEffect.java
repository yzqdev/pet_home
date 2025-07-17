package com.github.yzqdev.pet_home.server.misc;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class DrunkEffect extends MobEffect {
    public DrunkEffect(MobEffectCategory type, int color, boolean isInstant) {
        super(type, color );
    }


    @Override
    public boolean shouldApplyEffectTickThisTick(int tick, int amp) {
        if (tick % 5 == 0) {
            return true;
        }else{
            return false;
        }
    }




    public boolean isBeneficial() {
        return false;
    }
}