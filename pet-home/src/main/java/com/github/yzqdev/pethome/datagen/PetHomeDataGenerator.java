package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class PetHomeDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(ModBlockTagsProvider::new);
        pack.addProvider(ModEntityTagsProvider::new);
        pack.addProvider(ModPoiTagProvider::new);
        pack.addProvider(ModRecipeProvider::new);
        pack.addProvider(ModItemModelProvider::new);
        pack.addProvider(ModEnLangProvider::new);
        pack.addProvider(ModZhLangProvider::new);
    }

    @Override
    public String getEffectiveModId() {
        return PetHomeMod.MODID;
    }
}
