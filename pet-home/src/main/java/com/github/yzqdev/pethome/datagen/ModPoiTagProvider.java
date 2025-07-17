package com.github.yzqdev.pethome.datagen;


import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.misc.DIPOIRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PoiTypeTagsProvider;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static com.github.yzqdev.pethome.server.misc.DIPOIRegistry.getBeds;


public class ModPoiTagProvider extends PoiTypeTagsProvider {
    public ModPoiTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, PetHomeMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(DIPOIRegistry.PET_BED.getKey());
    }

    public static void bootstrap(BootstapContext<PoiType> bootstrap){
        bootstrap.register(DIPOIRegistry.PET_BED.getKey(),new PoiType(getBeds(), 1, 1));
    }
}
