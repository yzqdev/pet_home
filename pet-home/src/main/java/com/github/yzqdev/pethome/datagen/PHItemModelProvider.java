package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;


/** 物品模型 datagen：仅生成无手工 JSON 的标准平铺物品模型（项圈标签、捕网两态），其余模型由 PHModelProvider / 静态资源负责。 */
public class PHItemModelProvider extends FabricModelProvider {

    public PHItemModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        itemModelGenerator.generateFlatItem(PHItemRegistry.COLLAR_TAG, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(PHItemRegistry.NET_ITEM, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(PHItemRegistry.NET_HAS_ITEM, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerator.generateFlatItem(PHItemRegistry.WHISTLE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(PHItemRegistry.FREEDOM_BELL, ModelTemplates.FLAT_ITEM);
        // 哨子/自由之铃（72fa6ca3）：FLAT 模板
    }

    @Override
    public String getName() {
        return PetHomeMod.MODID + " item models";
    }
}
