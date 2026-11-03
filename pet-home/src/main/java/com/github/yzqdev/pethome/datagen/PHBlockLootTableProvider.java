package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.concurrent.CompletableFuture;


/**
 * 方块掉落表数据生成：指令鼓、往生灯笼、16 色宠物床全部自身掉落
 * （显式指定随机序列，保证掉落可复现）。
 */
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
