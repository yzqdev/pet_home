package com.github.yzqdev.pet_home.datagen;

import com.github.yzqdev.pet_home.server.block.PHBlockRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * @author yzqde
 * @date time 2024/12/17 2:13
 * @modified By:
 */
public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        var axe= this.tag(BlockTags.MINEABLE_WITH_AXE);
        for (var item : PHBlockRegistry.PET_BED_BLOCKS.values()) {
           axe.add(item.get());
        }
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(PHBlockRegistry.WAYWARD_LANTERN.value());
    }
    @Override
    public String getName() {
        return "mod  block tags";
    }
}
