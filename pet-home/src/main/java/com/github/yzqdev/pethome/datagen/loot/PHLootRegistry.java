package com.github.yzqdev.pethome.datagen.loot;


import com.github.yzqdev.pethome.PetHomeMod;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class PHLootRegistry {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> DEF_REG = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, PetHomeMod.MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<PHLootModifier>> LOOT_FRAGMENT = DEF_REG.register("ph_loot_modifier", () -> PHLootModifier.CODEC);
}
