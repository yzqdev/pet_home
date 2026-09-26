package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.concurrent.CompletableFuture;


public class PHBlockLootTableProvider extends FabricBlockLootSubProvider {

    protected PHBlockLootTableProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(packOutput, registriesFuture);
    }

    @Override
    public void generate() {
        this.add(PHBlockRegistry.DRUM, selfDrop(PHBlockRegistry.DRUM));
        this.add(PHBlockRegistry.WAYWARD_LANTERN, selfDrop(PHBlockRegistry.WAYWARD_LANTERN));
        PHBlockRegistry.PET_BED_BLOCKS.values().forEach(block -> this.add(block, selfDrop(block)));
    }

    private LootTable.Builder selfDrop(Block block) {
        return this.createSingleItemTable(block)
                .setRandomSequence(block.getLootTable().orElseThrow().identifier());
    }
}
