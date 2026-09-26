package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;


public class ModEffects {

    public static final Holder.Reference<MobEffect> DRUNK = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,
            ResourceKey.create(BuiltInRegistries.MOB_EFFECT.key(), Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "drunk")),
            new DrunkEffect(MobEffectCategory.HARMFUL, 6684723, false));

    public static void init() {
    }
}
