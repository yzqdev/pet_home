package com.github.yzqdev.pet_home.datagen;

import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.server.misc.PHPOIRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static com.github.yzqdev.pet_home.server.misc.PHPOIRegistry.getBeds;

public class ModPoiTagProvider extends PoiTypeTagsProvider {
    public ModPoiTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider,   @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, PetHomeMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(PHPOIRegistry.PET_BED.getKey());
    }

    public static void bootstrap(BootstrapContext<PoiType> bootstrap){
        bootstrap.register(PHPOIRegistry.PET_BED.getKey(),new PoiType(getBeds(), 1, 1));
    }
}
