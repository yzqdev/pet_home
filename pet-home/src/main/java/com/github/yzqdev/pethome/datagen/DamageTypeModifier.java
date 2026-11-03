package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.misc.PHDamageTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;

/**
 * datagen 引导自定义 {@link DamageType}（{@code data/pet_home/damage_type/*.json}）。 目前只有 {@code
 * pet_home:siphon}——生命虹吸（HEALTH_SIPHON）实现里的占位伤害源 （宠物实际吃 0 点、伤害转嫁给主人，{@code EntityHurtHandler}
 * 用它阻断虹吸递归）： 不随难度缩放（NEVER）、无附加效果与特殊死亡消息。
 */
public class DamageTypeModifier {
  public static void bootstrap(BootstrapContext<DamageType> bootstrap) {

    bootstrap.register(
        PHDamageTypes.SIPHON,
        new DamageType(
            "pet_home.siphon",
            DamageScaling.NEVER,
            0,
            DamageEffects.HURT,
            DeathMessageType.DEFAULT));
  }
}
