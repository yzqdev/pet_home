package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.DrumBlock;
import com.github.yzqdev.pethome.server.block.DyeColors;
import com.github.yzqdev.pethome.server.block.PetBedBlock;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.mojang.math.Quadrant;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.numeric.UseDuration;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Optional;


/**
 * 方块/物品模型数据生成：指令鼓三态顶面贴图、16 色宠物床（带朝向旋转）、
 * 往生灯笼等方块状态与对应物品模型。
 */
public class PHModelProvider extends FabricModelProvider {

    public PHModelProvider(FabricPackOutput output) {
        super(output);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(PetHomeMod.MODID, path);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModels) {
        for (String mode : List.of("wander", "stay", "follow")) {
            TextureMapping mapping = new TextureMapping()
                    .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(PHBlockRegistry.DRUM, "_side"))
                    .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(PHBlockRegistry.DRUM, "_bottom"))
                    .put(TextureSlot.TOP, new Material(id("block/drum_" + mode)));
            ModelTemplates.CUBE_BOTTOM_TOP.create(id("block/drum_" + mode), mapping, blockModels.modelOutput);
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(PHBlockRegistry.DRUM)
                .with(PropertyDispatch.initial(DrumBlock.COMMAND)
                        .select(0, BlockModelGenerators.plainVariant(id("block/drum_wander")))
                        .select(1, BlockModelGenerators.plainVariant(id("block/drum_stay")))
                        .select(2, BlockModelGenerators.plainVariant(id("block/drum_follow")))));
        new ModelTemplate(Optional.of(id("block/drum_wander")), Optional.empty())
                .create(id("item/drum"), new TextureMapping(), blockModels.modelOutput);


        for (DyeColor color : DyeColors.COLORS.keySet()) {
            String name = "pet_bed_" + color.name().toLowerCase();
            Block bed = PHBlockRegistry.PET_BED_BLOCKS.get(color);
            var modelId = id("block/" + name);
            // 色座模型：继承静态母模 pet_bed（5 元素手写），只覆写 bed 槽贴图
            // （bed 槽不进模板 requiredSlots——requiredSlots 与 mapping.getForced() 会拼接出重复键；
            //   只在 mapping 端 putForced 即可产出该槽）
            new ModelTemplate(Optional.of(id("block/pet_bed")), Optional.empty())
                    .create(modelId, new TextureMapping()
                            .putForced(BED_SLOT, new Material(id("block/pet_bed_seat_" + color.name().toLowerCase()))),
                            blockModels.modelOutput);

            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(bed)
                    .with(PropertyDispatch.initial(PetBedBlock.FACING)
                            .select(Direction.NORTH, BlockModelGenerators.plainVariant(modelId))
                            .select(Direction.EAST, BlockModelGenerators.plainVariant(modelId)
                                    .with(VariantMutator.Y_ROT.withValue(Quadrant.R90)))
                            .select(Direction.SOUTH, BlockModelGenerators.plainVariant(modelId)
                                    .with(VariantMutator.Y_ROT.withValue(Quadrant.R180)))
                            .select(Direction.WEST, BlockModelGenerators.plainVariant(modelId)
                                    .with(VariantMutator.Y_ROT.withValue(Quadrant.R270)))));
        }

    blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
                PHBlockRegistry.WAYWARD_LANTERN,
                BlockModelGenerators.plainVariant(id("block/wayward_lantern"))));
    }

    /** 宠物床色座贴图槽（自定义槽必须复用同一实例：TextureSlot.create 每次返回新对象）。 */
    private static final TextureSlot BED_SLOT = TextureSlot.create("bed");

    @Override
    public void generateItemModels(ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(PHItemRegistry.COLLAR_TAG, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.DEED_OF_OWNERSHIP, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.DEFLECTION_SHIELD, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.NET_ITEM, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.NET_HAS_ITEM, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.MAGNET, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.PET_COMPASS, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.ROTTEN_APPLE, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.SINISTER_CARROT, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.WHISTLE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.FREEDOM_BELL, ModelTemplates.FLAT_ITEM);

        itemModels.declareCustomModelItem(PHItemRegistry.FEATHER_ON_A_STICK);
        registerNetLauncherItemModels(itemModels);

        itemModels.itemModelOutput.accept(
                PHBlockRegistry.DRUM_ITEM,
                ItemModelUtils.plainModel(id("block/drum_wander"))
        );
        itemModels.itemModelOutput.accept(
                PHBlockRegistry.WAYWARD_LANTERN_ITEM,
                ItemModelUtils.plainModel(id("block/wayward_lantern"))
        );
        PHBlockRegistry.PetBedItems.forEach((color, item) -> itemModels.itemModelOutput.accept(
                item,
                ItemModelUtils.plainModel(id("block/pet_bed_" + color.name().toLowerCase()))
        ));
    }

    /**
     * 捕网发射器的物品模型定义（原版 bow/trident 范式，与 1.20/1.21 对齐）：所有上下文
     * （手持/GUI/地面/展示框/物品架）用 3D 模型（pet_home:item/net_launcher，模型自带各
     * 视角展示段）；蓄力时按 use_duration 用 range_dispatch 切换能量核心贴图逐级点亮
     * （charging_1 → charging_2）。
     */
    private static void registerNetLauncherItemModels(ItemModelGenerators itemModels) {
        ItemModel.Unbaked handModel = ItemModelUtils.plainModel(id("item/net_launcher"));
        ItemModel.Unbaked chargingModel =
                ItemModelUtils.rangeSelect(
                        new UseDuration(false),
                        0.05F,
                        handModel,
                        ItemModelUtils.override(
                                ItemModelUtils.plainModel(id("item/net_launcher_charging_1")), 0.5F),
                        ItemModelUtils.override(
                                ItemModelUtils.plainModel(id("item/net_launcher_charging_2")), 1.0F));
        itemModels.itemModelOutput.accept(
            PHItemRegistry.NET_LAUNCHER_ITEM,
            ItemModelUtils.conditional(ItemModelUtils.isUsingItem(), chargingModel, handModel));
    }
}
