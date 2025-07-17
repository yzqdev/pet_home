package com.github.yzqdev.pet_home.datagen;

import com.github.yzqdev.pet_home.PetHomeMod;


import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModDatapackProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.ENCHANTMENT, ModEnchantments::bootstrap)

            .add(Registries.DAMAGE_TYPE,DamageTypeModifier::bootstrap) ;


    public ModDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries ) {
        super(output, registries,BUILDER, Set.of(PetHomeMod.MODID));

    }
}
