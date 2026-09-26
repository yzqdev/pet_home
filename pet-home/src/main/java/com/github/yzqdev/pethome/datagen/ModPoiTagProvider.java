package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.misc.PHPOIRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static com.github.yzqdev.pethome.server.misc.PHPOIRegistry.getBeds;

public class ModPoiTagProvider extends PoiTypeTagsProvider {
    public ModPoiTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(PHPOIRegistry.PET_BED.key());
    }

    public static void bootstrap(BootstrapContext<PoiType> bootstrap) {
        bootstrap.register(PHPOIRegistry.PET_BED.key(), new PoiType(getBeds(), 1, 1));
    }
}
