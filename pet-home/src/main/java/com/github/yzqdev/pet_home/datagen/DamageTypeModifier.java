package com.github.yzqdev.pet_home.datagen;

import com.github.yzqdev.pet_home.server.misc.PHDamageTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;

public class DamageTypeModifier {
public static void bootstrap(BootstrapContext<DamageType> bootstrap){


        bootstrap.register(PHDamageTypes.SIPHON, new DamageType("pet_home.siphon",
                DamageScaling.NEVER,
                0,
                DamageEffects.HURT,
                DeathMessageType.DEFAULT));


}
}