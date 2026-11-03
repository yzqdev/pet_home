package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;

/** 方块 tag datagen：16 色宠物床归入斧类挖掘（{@code minecraft:mineable/axe}），往生灯笼归入镐类挖掘。 */
public class PHBlockTagsProvider extends FabricTagProvider.BlockTagProvider {

  public PHBlockTagsProvider(
      FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
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
