package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.DrumBlock;
import com.github.yzqdev.pethome.server.block.DyeColors;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.block.PetBedBlock;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
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
import net.minecraft.world.level.block.Block;

/** 物品模型 datagen：7 个扁平贴图物品（项圈标签、所有权证书、偏折护盾、磁铁、羽毛竿；两种捕网为手持模板）。 方块模型（宠物床/往生灯笼/指令鼓）是手写 JSON，不在此生成。 */
public class PHItemProvider extends FabricModelProvider {

  public PHItemProvider(FabricDataOutput output) {
    super(output);
  }

  @Override
  public void generateItemModels(ItemModelGenerators itemModelGenerator) {
    itemModelGenerator.generateFlatItem(PHItemRegistry.COLLAR_TAG, ModelTemplates.FLAT_ITEM);
    itemModelGenerator.generateFlatItem(PHItemRegistry.DEED_OF_OWNERSHIP, ModelTemplates.FLAT_ITEM);
    itemModelGenerator.generateFlatItem(PHItemRegistry.DEFLECTION_SHIELD, ModelTemplates.FLAT_ITEM);
    itemModelGenerator.generateFlatItem(PHItemRegistry.MAGNET, ModelTemplates.FLAT_ITEM);
    itemModelGenerator.generateFlatItem(
        PHItemRegistry.FEATHER_ON_A_STICK, ModelTemplates.FLAT_ITEM);
    itemModelGenerator.generateFlatItem(PHItemRegistry.NET_ITEM, ModelTemplates.FLAT_HANDHELD_ITEM);
    itemModelGenerator.generateFlatItem(
        PHItemRegistry.NET_HAS_ITEM, ModelTemplates.FLAT_HANDHELD_ITEM);
    // 哨子/自由之铃（72fa6ca3）：FLAT 模板（对齐 26.3 fabric PHModelProvider）
    itemModelGenerator.generateFlatItem(PHItemRegistry.WHISTLE, ModelTemplates.FLAT_ITEM);
    itemModelGenerator.generateFlatItem(PHItemRegistry.FREEDOM_BELL, ModelTemplates.FLAT_ITEM);
    itemModelGenerator.generateFlatItem(PHItemRegistry.ROTTEN_APPLE, ModelTemplates.FLAT_ITEM);
    itemModelGenerator.generateFlatItem(PHItemRegistry.SINISTER_CARROT, ModelTemplates.FLAT_ITEM);
  }

  /**
   * 方块状态 datagen：指令鼓 COMMAND 三态（cube_bottom_top 模型 + 变体）、16 色宠物床色座模型
   * （母模 pet_bed 静态手写，bed 槽 putForced 覆写）+ FACING 四向旋转、往生灯笼单变体。
   */
  @Override
  public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
    for (String mode : List.of("wander", "stay", "follow")) {
      TextureMapping mapping =
          new TextureMapping()
              .put(
                  TextureSlot.SIDE,
                  TextureMapping.getBlockTexture(PHBlockRegistry.DRUM, "_side"))
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
                    .select(
                        0,
                        Variant.variant()
                            .with(VariantProperties.MODEL, id("block/drum_wander")))
                    .select(
                        1,
                        Variant.variant()
                            .with(VariantProperties.MODEL, id("block/drum_stay")))
                    .select(
                        2,
                        Variant.variant()
                            .with(VariantProperties.MODEL, id("block/drum_follow")))));

    for (var color : DyeColors.COLORS.keySet()) {
      String name = "pet_bed_" + color.name().toLowerCase();
      Block bed = PHBlockRegistry.PET_BED_BLOCKS.get(color);
      var modelId = id("block/" + name);
      // 色座模型：继承静态母模 pet_bed（5 元素手写），只覆写 bed 槽贴图
      // （bed 槽不进模板 requiredSlots，只在 mapping 端 putForced）
      new ModelTemplate(Optional.of(id("block/pet_bed")), Optional.empty())
          .create(
              modelId,
              new TextureMapping()
                  .putForced(
                      BED_SLOT,
                      id("block/pet_bed_seat_" + color.name().toLowerCase())),
              blockStateModelGenerator.modelOutput);
      blockStateModelGenerator.blockStateOutput.accept(
          MultiVariantGenerator.multiVariant(bed)
              .with(
                  PropertyDispatch.property(PetBedBlock.FACING)
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

    // BlockItem 物品模型：引用本 provider 生成的方块模型（vanilla 无 BlockItem auto-fallback，
    // 缺文件时物品在 GUI 显示紫黑）。drum → drum_wander；16 色宠物床 → 同色座模型。
    new ModelTemplate(Optional.of(id("block/drum_wander")), Optional.empty())
        .create(id("item/drum"), new TextureMapping(), blockStateModelGenerator.modelOutput);
    for (var color : DyeColors.COLORS.keySet()) {
      String name = "pet_bed_" + color.name().toLowerCase();
      new ModelTemplate(Optional.of(id("block/" + name)), Optional.empty())
          .create(id("item/" + name), new TextureMapping(), blockStateModelGenerator.modelOutput);
    }
    // 往生灯笼：display 变换复刻 2ebcdadc 删除的原静态文件（vanilla ModelTemplate 不支持 display，
    // 用自定义 JSON 写出）
    blockStateModelGenerator.modelOutput.accept(
        id("item/wayward_lantern"), PHItemProvider::waywardLanternItemModel);
  }

  /** 往生灯笼物品模型：parent 指向 12 元素方块模型，display 变换复刻原静态文件。 */
  private static JsonObject waywardLanternItemModel() {
    JsonObject json = new JsonObject();
    json.addProperty("parent", "pet_home:block/wayward_lantern");
    JsonObject display = new JsonObject();
    display.add("thirdperson_righthand", transform(75f, 45f, 0f, 0, 2.5, 0, 0.375));
    display.add("thirdperson_lefthand", transform(75f, 45f, 0f, 0, 2.5, 0, 0.375));
    display.add("firstperson_righthand", transform(0f, 45f, 0f, 1.5, 2.5, 0, 0.4));
    display.add("firstperson_lefthand", transform(0f, 225f, 0f, 2, 2.5, 0, 0.4));
    display.add("ground", transform(null, null, null, 0, 0.5, 0, 0.5));
    display.add("gui", transform(30f, 225f, 0f, 0, -1.25, 0, 0.8));
    display.add("head", transform(0f, 180f, 0f, 0, 13, -0.25, 1.5));
    display.add("fixed", transform(null, null, null, 0, -1.75, -0.75, null));
    json.add("display", display);
    return json;
  }

  private static JsonObject transform(
      Float rotX, Float rotY, Float rotZ, double tx, double ty, double tz, Double scale) {
    JsonObject t = new JsonObject();
    if (rotX != null) {
      JsonArray rotation = new JsonArray();
      rotation.add(rotX);
      rotation.add(rotY);
      rotation.add(rotZ);
      t.add("rotation", rotation);
    }
    JsonArray translation = new JsonArray();
    translation.add(tx);
    translation.add(ty);
    translation.add(tz);
    t.add("translation", translation);
    if (scale != null) {
      JsonArray scaleArr = new JsonArray();
      scaleArr.add(scale);
      scaleArr.add(scale);
      scaleArr.add(scale);
      t.add("scale", scaleArr);
    }
    return t;
  }

  /** 宠物床色座贴图槽（自定义槽必须复用同一实例：TextureSlot.create 每次返回新对象）。 */
  private static final TextureSlot BED_SLOT = TextureSlot.create("bed");

  private static ResourceLocation id(String path) {
    return ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, path);
  }

  @Override
  public String getName() {
    return "mod item models";
  }
}
