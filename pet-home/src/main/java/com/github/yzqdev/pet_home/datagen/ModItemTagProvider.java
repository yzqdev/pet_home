package com.github.yzqdev.pet_home.datagen;

import com.github.yzqdev.pet_home.server.block.PHBlockRegistry;
import com.github.yzqdev.pet_home.server.item.PHItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * @author yzqde
 * @date time 2024/12/17 1:44
 * @modified By:
 */
public class ModItemTagProvider extends ItemTagsProvider {

    public ModItemTagProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(ModTags.COLLAR_TAG_tagkey).add(PHItemRegistry.COLLAR_TAG.getKey());
        this.tag(ModTags.PetBedKey).add(PHBlockRegistry.PetBedItems.values().stream().map(i->i.get()).toArray(Item[]::new));

    }

    @Override
    public String getName() {
        return  "mod item tags";
    }
}
