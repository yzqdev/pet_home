package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;

import java.util.concurrent.CompletableFuture;

/**
 * @author yzqde
 * @date time 2024/12/17 2:13
 * @modified By:
 */
public class ModBlockTagsProvider extends FabricTagProvider.BlockTagProvider {

    public ModBlockTagsProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var axe = getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_AXE);
        for (var block : PHBlockRegistry.PET_BED_BLOCKS.values()) {
            axe.add(block);
        }
        getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_PICKAXE).add(PHBlockRegistry.WAYWARD_LANTERN);
    }

    @Override
    public String getName() {
        return "mod block tags";
    }
}
