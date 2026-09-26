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
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Optional;


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

            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(bed)
                    .with(PropertyDispatch.initial(PetBedBlock.FACING)
                            .select(Direction.NORTH, BlockModelGenerators.plainVariant(modelId))
                            .select(Direction.EAST, BlockModelGenerators.plainVariant(modelId)
                                    .with(VariantMutator.Y_ROT.withValue(Quadrant.R90)))
                            .select(Direction.SOUTH, BlockModelGenerators.plainVariant(modelId)
                                    .with(VariantMutator.Y_ROT.withValue(Quadrant.R180)))
                            .select(Direction.WEST, BlockModelGenerators.plainVariant(modelId)
                                    .with(VariantMutator.Y_ROT.withValue(Quadrant.R270)))));
            // 宠物床物品模型：直接引用方块模型
            new ModelTemplate(Optional.of(modelId), Optional.empty())
                    .create(id("item/" + name), new TextureMapping(), blockModels.modelOutput);
        }

        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
                PHBlockRegistry.WAYWARD_LANTERN,
                BlockModelGenerators.plainVariant(id("block/wayward_lantern"))));
    }

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

        itemModels.declareCustomModelItem(PHItemRegistry.FEATHER_ON_A_STICK);
        itemModels.declareCustomModelItem(PHItemRegistry.NET_LAUNCHER_ITEM);

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
}
