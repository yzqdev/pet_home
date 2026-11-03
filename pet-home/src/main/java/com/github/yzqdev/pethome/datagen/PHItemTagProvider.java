package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;

/**
 * 物品 tag datagen：项圈标签物品集、全部 16 色宠物床物品集，并把捕网发射器挂进 {@code
 * minecraft:durability_enchantable}（否则它无法在铁砧/附魔台吃耐久附魔）。
 */
public class PHItemTagProvider extends FabricTagProvider.ItemTagProvider {

  public PHItemTagProvider(
      FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, registries);
  }

  @Override
  protected void addTags(HolderLookup.Provider provider) {
    getOrCreateTagBuilder(PHTags.COLLAR_TAG_tagkey).add(PHItemRegistry.COLLAR_TAG);
    var petBed = getOrCreateTagBuilder(PHTags.PetBedKey);
    PHBlockRegistry.PetBedItems.values().forEach(item -> petBed.add(item));
    getOrCreateTagBuilder(ItemTags.DURABILITY_ENCHANTABLE).add(PHItemRegistry.NET_LAUNCHER_ITEM);
  }

  @Override
  public String getName() {
    return "mod item tags";
  }
}
