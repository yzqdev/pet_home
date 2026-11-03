package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;


/** 方块标签 datagen：往生灯笼归入镐子可挖掘（MINEABLE_WITH_PICKAXE）。 */
public class PHBlockTagsProvider extends FabricTagProvider<Block> {

    public PHBlockTagsProvider(FabricDataOutput output, CompletableFuture<net.minecraft.core.HolderLookup.Provider> registriesFuture) {
        super(output, Registries.BLOCK, registriesFuture);
    }

    @Override
    protected void addTags(net.minecraft.core.HolderLookup.Provider provider) {
        getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_PICKAXE).add(PHBlockRegistry.WAYWARD_LANTERN);
    }

    @Override
    public String getName() {
        return "mod block tags";
    }
}
