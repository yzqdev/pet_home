package com.github.yzqdev.pethome.datagen.loot;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;


public class PHLootProvider extends SimpleFabricLootTableProvider {

    private final CompletableFuture<HolderLookup.Provider> registryLookup;

    public PHLootProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, LootContextParamSets.CHEST);
        this.registryLookup = registries;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        new LootTableGen.ChestLootTables(registryLookup.join()).generate(consumer);
    }

    @Override
    public String getName() {
        return "ph loot tables";
    }
}
