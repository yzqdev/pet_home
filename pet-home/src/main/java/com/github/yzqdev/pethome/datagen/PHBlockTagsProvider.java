package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;

import java.util.concurrent.CompletableFuture;


/**
 * 方块标签数据生成：16 色宠物床归入斧头可挖掘、往生灯笼归入镐子可挖掘。
 */
public class PHBlockTagsProvider extends FabricTagsProvider.BlockTagsProvider {

    public PHBlockTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        var axe = this.valueLookupBuilder(BlockTags.MINEABLE_WITH_AXE);
        PHBlockRegistry.PET_BED_BLOCKS.values().forEach(block -> axe.add(block));
        this.valueLookupBuilder(BlockTags.MINEABLE_WITH_PICKAXE).add(PHBlockRegistry.WAYWARD_LANTERN);
    }

    @Override
    public String getName() {
        return "mod block tags";
    }
}
