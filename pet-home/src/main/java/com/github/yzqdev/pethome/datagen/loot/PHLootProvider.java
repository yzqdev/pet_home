package com.github.yzqdev.pethome.datagen.loot;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

/**
 * datagen provider：生成 {@link LootTableGen} 定义的 chest 类型战利品表 JSON （宠物商店箱 {@code petshop_chest} 与
 * {@code pet_protection_loot}），前者由 {@code PetshopStructurePoolElement.handleDataMarker}
 * 在商店生成时挂到箱子方块上； 与 {@link PHLootRegistry} 的运行时注入互不相干。
 */
public class PHLootProvider extends SimpleFabricLootTableProvider {

  private final CompletableFuture<HolderLookup.Provider> registryLookup;

  public PHLootProvider(
      FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
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
