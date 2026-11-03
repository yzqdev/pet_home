package com.github.yzqdev.pethome.datagen;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;

/** 生成 data-driven 动态注册表条目（附魔、伤害类型）。 条目本体在 PetHomeDataGenerator.buildRegistry 中引导。 */
public class PHDatapackProvider extends FabricDynamicRegistryProvider {

  public PHDatapackProvider(
      FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, registries);
  }

  @Override
  protected void configure(HolderLookup.Provider registries, Entries entries) {
    entries.addAll(registries.lookupOrThrow(Registries.ENCHANTMENT));
    entries.addAll(registries.lookupOrThrow(Registries.DAMAGE_TYPE));
  }

  @Override
  public String getName() {
    return "mod datapack entries";
  }
}
