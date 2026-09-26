package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.misc.PHPOIRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import java.util.concurrent.CompletableFuture;

public class ModPoiTagProvider extends FabricTagProvider<PoiType> {

    public ModPoiTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Registries.POINT_OF_INTEREST_TYPE, registries);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        getOrCreateTagBuilder(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(PHPOIRegistry.PET_BED.key());
    }

    @Override
    public String getName() {
        return "mod poi tags";
    }
}
