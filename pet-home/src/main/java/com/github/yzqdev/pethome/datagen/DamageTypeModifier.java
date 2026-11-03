package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.misc.PHDamageTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;

/**
 * 伤害类型数据生成：注册虹吸附魔专用的 {@code pet_home:siphon} 伤害类型
 * （不受伤害缩放、无死亡消息附加）。
 */
public class DamageTypeModifier {
    public static void bootstrap(BootstrapContext<DamageType> bootstrap) {


        bootstrap.register(PHDamageTypes.SIPHON, new DamageType("pet_home.siphon",
                DamageScaling.NEVER,
                0,
                DamageEffects.HURT,
                DeathMessageType.DEFAULT));


    }
}