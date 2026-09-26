package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.loot.PHLootProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageType;


public class PetHomeDataGenerator implements DataGeneratorEntrypoint {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.ENCHANTMENT, ModEnchantments::bootstrap)
            .add(Registries.DAMAGE_TYPE, DamageTypeModifier::bootstrap);

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(ModDatapackProvider::new);
        pack.addProvider(ModBlockTagsProvider::new);
        pack.addProvider(ModRecipeProvider::new);
        pack.addProvider(ModItemProvider::new);
        pack.addProvider(ModPoiTagProvider::new);
        pack.addProvider(ModEntityTagsProvider::new);
        pack.addProvider(ModEnchantTagProvider::new);
        pack.addProvider(ModItemTagProvider::new);
        pack.addProvider(ModEnLangProvider::new);
        pack.addProvider(ModZhLangProvider::new);
        pack.addProvider(PHLootProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.ENCHANTMENT, ModEnchantments::bootstrap);
        registryBuilder.add(Registries.DAMAGE_TYPE, DamageTypeModifier::bootstrap);
    }

    @Override
    public String getEffectiveModId() {
        return PetHomeMod.MODID;
    }
}
