package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.DrumBlock;
import com.github.yzqdev.pethome.server.block.DyeColors;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.block.PetBedBlock;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
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
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author yzqde
 * @date time 2024/12/11 14:45
 * @modified By:
 */
public class PHItemProvider extends ModelProvider {

    public PHItemProvider(PackOutput output, String modId) {
        super(output, modId);
    }


    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(PHItemRegistry.COLLAR_TAG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.DEED_OF_OWNERSHIP.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.DEFLECTION_SHIELD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.NET_ITEM.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.NET_HAS_ITEM.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.PET_COMPASS.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.WHISTLE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.FREEDOM_BELL.get(), ModelTemplates.FLAT_ITEM);


        itemModels.declareCustomModelItem(PHItemRegistry.FEATHER_ON_A_STICK.get());
        itemModels.generateFlatItem(PHItemRegistry.MAGNET.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.ROTTEN_APPLE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(PHItemRegistry.SINISTER_CARROT.get(), ModelTemplates.FLAT_ITEM);
        registerNetLauncherItemModels(itemModels);


        itemModels.itemModelOutput.accept(
                PHBlockRegistry.DRUM_ITEM.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "block/drum_wander"))
        );

        registerBlockStates(blockModels);

    }

    /**
     * 方块状态 datagen（与 26.3 侧同逻辑）：指令鼓按 COMMAND 切三张顶面（三态 cube_bottom_top
     * 模型由此生成）；16 色宠物床按 FACING 旋转 + 色座模型（母模 pet_bed 静态手写，只覆写 bed
     * 槽贴图）；往生灯笼单变体（12 元素模型静态手写）。宠物床/灯笼的 BlockItem 物品模型定义由
     * ModelProvider.finalizeAndValidate 的 auto-fallback 生成（指向 block/&lt;name&gt;）。
     */
    private void registerBlockStates(BlockModelGenerators blockModels) {
        for (String mode : List.of("wander", "stay", "follow")) {
            TextureMapping mapping =
                    new TextureMapping()
                            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(PHBlockRegistry.DRUM.get(), "_side"))
                            .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(PHBlockRegistry.DRUM.get(), "_bottom"))
                            .put(TextureSlot.TOP, new Material(id("block/drum_" + mode)));
            ModelTemplates.CUBE_BOTTOM_TOP.create(id("block/drum_" + mode), mapping, blockModels.modelOutput);
        }
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(PHBlockRegistry.DRUM.get())
                        .with(
                                PropertyDispatch.initial(DrumBlock.COMMAND)
                                        .select(0, BlockModelGenerators.plainVariant(id("block/drum_wander")))
                                        .select(1, BlockModelGenerators.plainVariant(id("block/drum_stay")))
                                        .select(2, BlockModelGenerators.plainVariant(id("block/drum_follow")))));

        for (var color : DyeColors.COLORS.keySet()) {
            String name = "pet_bed_" + color.name().toLowerCase();
            Block bed = PHBlockRegistry.PET_BED_BLOCKS.get(color).get();
            var modelId = id("block/" + name);
            // 色座模型：继承静态母模 pet_bed（5 元素手写），只覆写 bed 槽贴图
            // （bed 槽不进模板 requiredSlots——requiredSlots 与 mapping.getForced() 会拼接出重复键；
            //   只在 mapping 端 putForced 即可产出该槽）
            new ModelTemplate(Optional.of(id("block/pet_bed")), Optional.empty())
                    .create(
                            modelId,
                            new TextureMapping()
                                    .putForced(
                                            BED_SLOT,
                                            new Material(id("block/pet_bed_seat_" + color.name().toLowerCase()))),
                            blockModels.modelOutput);
            blockModels.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(bed)
                            .with(
                                    PropertyDispatch.initial(PetBedBlock.FACING)
                                            .select(Direction.NORTH, BlockModelGenerators.plainVariant(modelId))
                                            .select(
                                                    Direction.EAST,
                                                    BlockModelGenerators.plainVariant(modelId)
                                                            .with(VariantMutator.Y_ROT.withValue(Quadrant.R90)))
                                            .select(
                                                    Direction.SOUTH,
                                                    BlockModelGenerators.plainVariant(modelId)
                                                            .with(VariantMutator.Y_ROT.withValue(Quadrant.R180)))
                                            .select(
                                                    Direction.WEST,
                                                    BlockModelGenerators.plainVariant(modelId)
                                                            .with(VariantMutator.Y_ROT.withValue(Quadrant.R270)))));
        }

        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(
                        PHBlockRegistry.WAYWARD_LANTERN.get(),
                        BlockModelGenerators.plainVariant(id("block/wayward_lantern"))));
    }

    /** 宠物床色座贴图槽（自定义槽必须复用同一实例：TextureSlot.create 每次返回新对象）。 */
    private static final TextureSlot BED_SLOT = TextureSlot.create("bed");

    /**
     * 捕网发射器的物品模型定义（原版 bow/trident 范式，与 1.20/1.21 对齐）：所有上下文
     * （手持/GUI/地面/展示框/物品架）用 3D 模型（pet_home:item/net_launcher，模型自带各
     * 视角展示段）；蓄力时按 use_duration 用 range_dispatch 切换能量核心贴图逐级点亮
     * （charging_1 → charging_2）。
     */
    private static void registerNetLauncherItemModels(ItemModelGenerators itemModels) {
        ItemModel.Unbaked handModel =
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "item/net_launcher"));
        ItemModel.Unbaked chargingModel =
                ItemModelUtils.rangeSelect(
                        new UseDuration(false),
                        0.05F,
                        handModel,
                        ItemModelUtils.override(
                                ItemModelUtils.plainModel(
                                        Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "item/net_launcher_charging_1")),
                                0.5F),
                        ItemModelUtils.override(
                                ItemModelUtils.plainModel(
                                        Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "item/net_launcher_charging_2")),
                                1.0F));
        itemModels.itemModelOutput.accept(
            PHItemRegistry.NET_LAUNCHER_ITEM.get(),
            ItemModelUtils.conditional(ItemModelUtils.isUsingItem(), chargingModel, handModel));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(PetHomeMod.MODID, path);
    }
}

