package com.github.yzqdev.pethome.datagen.loot;

import com.github.yzqdev.pethome.server.block.DyeColors;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;

/** 方块战利品表 datagen：指令鼓、往生灯笼与 16 色宠物床均"掉落自身"（对应原静态 18 张表）。 */
public class PHBlockLootTableProvider extends FabricBlockLootTableProvider {

  public PHBlockLootTableProvider(
      FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, registries);
  }

  @Override
  public void generate() {
    this.add(PHBlockRegistry.DRUM, this.createSingleItemTable(PHBlockRegistry.DRUM));
    for (var color : DyeColors.COLORS.keySet()) {
      Block bed = PHBlockRegistry.PET_BED_BLOCKS.get(color);
      this.add(bed, this.createSingleItemTable(bed));
    }
    this.add(PHBlockRegistry.WAYWARD_LANTERN, this.createSingleItemTable(PHBlockRegistry.WAYWARD_LANTERN));
  }

}
