package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

public class PHItemTagsProvider extends FabricTagsProvider.ItemTagsProvider {
    public PHItemTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.valueLookupBuilder(ModTags.COLLAR_TAG_tagkey).add(PHItemRegistry.COLLAR_TAG );
        this.valueLookupBuilder(ModTags.PetBedKey).add(PHBlockRegistry.PetBedItems.values().stream()
                .toArray(Item[]::new));
        valueLookupBuilder(ItemTags.DURABILITY_ENCHANTABLE).add(PHItemRegistry.NET_LAUNCHER_ITEM);
    }
}
