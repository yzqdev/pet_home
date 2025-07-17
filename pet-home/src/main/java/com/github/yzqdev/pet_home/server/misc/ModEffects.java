package com.github.yzqdev.pet_home.server.misc;

import com.github.yzqdev.pet_home.PetHomeMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, PetHomeMod.MODID);
    public static DeferredHolder<MobEffect,MobEffect> DRUNK = EFFECTS.register("drunk", () -> {
        return new DrunkEffect(MobEffectCategory.HARMFUL, 6684723, false);
    });

}