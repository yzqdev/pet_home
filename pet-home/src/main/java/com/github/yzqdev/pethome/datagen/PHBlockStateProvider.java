package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.DrumBlock;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.Block;

/**
 * 方块状态与方块模型 datagen：指令鼓 COMMAND 三态、16 色宠物床色座模型（母模 pet_bed 静态
 * 手写，bed 槽 putForced 覆写）+ FACING 四向旋转、往生灯笼单变体（12 元素模型静态手写）。
 */
public class PHBlockStateProvider extends FabricModelProvider {

  public PHBlockStateProvider(FabricDataOutput output) {
    super(output);
  }

  @Override
  public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
    for (String mode : List.of("wander", "stay", "follow")) {
      TextureMapping mapping =
          new TextureMapping()
              .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(PHBlockRegistry.DRUM, "_side"))
              .put(
                  TextureSlot.BOTTOM,
                  TextureMapping.getBlockTexture(PHBlockRegistry.DRUM, "_bottom"))
              .put(TextureSlot.TOP, id("block/drum_" + mode));
      ModelTemplates.CUBE_BOTTOM_TOP.create(
          id("block/drum_" + mode), mapping, blockStateModelGenerator.modelOutput);
    }
    blockStateModelGenerator.blockStateOutput.accept(
        MultiVariantGenerator.multiVariant(PHBlockRegistry.DRUM)
            .with(
                PropertyDispatch.property(DrumBlock.COMMAND)
                    .select(0, Variant.variant().with(VariantProperties.MODEL, id("block/drum_wander")))
                    .select(1, Variant.variant().with(VariantProperties.MODEL, id("block/drum_stay")))
                    .select(2, Variant.variant().with(VariantProperties.MODEL, id("block/drum_follow")))));

    Map<String, Block> petBeds = PHBlockRegistry.petBedsByColor();
    for (var entry : petBeds.entrySet()) {
      String name = "pet_bed_" + entry.getKey();
      Block bed = entry.getValue();
      var modelId = id("block/" + name);
      // 色座模型：继承静态母模 pet_bed，只覆写 bed 槽贴图（bed 槽不进 requiredSlots）
      new ModelTemplate(Optional.of(id("block/pet_bed")), Optional.empty())
          .create(
              modelId,
              new TextureMapping()
                  .putForced(BED_SLOT, id("block/pet_bed_seat_" + entry.getKey())),
              blockStateModelGenerator.modelOutput);
      blockStateModelGenerator.blockStateOutput.accept(
          MultiVariantGenerator.multiVariant(bed)
              .with(
                  PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
                      .select(
                          Direction.NORTH,
                          Variant.variant().with(VariantProperties.MODEL, modelId))
                      .select(
                          Direction.EAST,
                          Variant.variant()
                              .with(VariantProperties.MODEL, modelId)
                              .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                      .select(
                          Direction.SOUTH,
                          Variant.variant()
                              .with(VariantProperties.MODEL, modelId)
                              .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                      .select(
                          Direction.WEST,
                          Variant.variant()
                              .with(VariantProperties.MODEL, modelId)
                              .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))));
    }

    blockStateModelGenerator.blockStateOutput.accept(
        BlockModelGenerators.createSimpleBlock(
            PHBlockRegistry.WAYWARD_LANTERN, id("block/wayward_lantern")));
  }

  @Override
  public void generateItemModels(ItemModelGenerators itemModelGenerator) {
    // 物品模型由 PHItemModelProvider 负责；本 provider 只生成方块状态
  }

  /** 宠物床色座贴图槽（自定义槽必须复用同一实例：TextureSlot.create 每次返回新对象）。 */
  private static final TextureSlot BED_SLOT = TextureSlot.create("bed");

  private static ResourceLocation id(String path) {
    return new ResourceLocation(PetHomeMod.MODID, path);
  }

  @Override
  public String getName() {
    return "mod block states";
  }
}
