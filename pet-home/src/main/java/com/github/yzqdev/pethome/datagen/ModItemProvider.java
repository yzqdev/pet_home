package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * @author yzqde
 * @date time 2024/12/11 14:45
 * @modified By:
 */
public class ModItemProvider extends ModelProvider {

    public ModItemProvider(PackOutput output, String modId) {
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


        itemModels.declareCustomModelItem(PHItemRegistry.FEATHER_ON_A_STICK.get());
        itemModels.declareCustomModelItem(PHItemRegistry.MAGNET.get());
        itemModels.declareCustomModelItem(PHItemRegistry.ROTTEN_APPLE.get());
        itemModels.declareCustomModelItem(PHItemRegistry.SINISTER_CARROT.get());
        itemModels.declareCustomModelItem(PHItemRegistry.NET_LAUNCHER_ITEM.get());


        itemModels.itemModelOutput.accept(
                PHBlockRegistry.DRUM_ITEM.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "block/drum_wander"))
        );


    }


}
