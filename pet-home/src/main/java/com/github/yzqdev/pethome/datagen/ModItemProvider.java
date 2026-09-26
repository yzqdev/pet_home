package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;

/**
 * @author yzqde
 * @date time 2024/12/11 14:45
 * @modified By:
 */
public class ModItemProvider extends FabricModelProvider {

    public ModItemProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        itemModelGenerator.generateFlatItem(PHItemRegistry.COLLAR_TAG, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(PHItemRegistry.DEED_OF_OWNERSHIP, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(PHItemRegistry.DEFLECTION_SHIELD, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(PHItemRegistry.MAGNET, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(PHItemRegistry.FEATHER_ON_A_STICK, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(PHItemRegistry.NET_ITEM, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(PHItemRegistry.NET_HAS_ITEM, ModelTemplates.FLAT_HANDHELD_ITEM);
    }

    @Override
    public void generateBlockStateModels(net.minecraft.data.models.BlockModelGenerators blockStateModelGenerator) {
        // 方块模型（pet_bed/wayward_lantern/drum）为手写 JSON，位于 src/main/resources
    }

    @Override
    public String getName() {
        return "mod item models";
    }
}
