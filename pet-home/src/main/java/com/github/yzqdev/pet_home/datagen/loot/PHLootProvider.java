package com.github.yzqdev.pet_home.datagen.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PHLootProvider extends LootTableProvider {


    public PHLootProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(LootTableGen.PET_LOOT_TABLE), List.of(
                new LootTableProvider.SubProviderEntry(LootTableGen.ChestLootTables::new, LootContextParamSets.CHEST),
                new LootTableProvider.SubProviderEntry(LootTableGen.BlockLootTables::new, LootContextParamSets.BLOCK)

        ), registries);
    }
}
