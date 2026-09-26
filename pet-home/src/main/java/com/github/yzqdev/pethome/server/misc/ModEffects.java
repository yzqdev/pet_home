package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, PetHomeMod.MODID);
    public static final DeferredHolder<MobEffect, MobEffect> DRUNK = EFFECTS.register("drunk", () -> {
        return new DrunkEffect(MobEffectCategory.HARMFUL, 6684723, false);
    });

}