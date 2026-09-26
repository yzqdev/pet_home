package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;

import java.util.concurrent.CompletableFuture;

/**
 * @author yzqde
 * @date time 2024/12/17 1:44
 * @modified By:
 */
public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {

    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        getOrCreateTagBuilder(ModTags.COLLAR_TAG_tagkey).add(PHItemRegistry.COLLAR_TAG);
        var petBed = getOrCreateTagBuilder(ModTags.PetBedKey);
        PHBlockRegistry.PetBedItems.values().forEach(item -> petBed.add(item));
        getOrCreateTagBuilder(ItemTags.DURABILITY_ENCHANTABLE).add(PHItemRegistry.NET_LAUNCHER_ITEM);
    }

    @Override
    public String getName() {
        return "mod item tags";
    }
}
