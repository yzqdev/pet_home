package com.github.yzqdev.pet_home.datagen;

import com.github.yzqdev.pet_home.PetHomeMod;

import com.github.yzqdev.pet_home.server.item.PHItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;


/**
 * @author yzqde
 * @date time 2024/12/11 14:45
 * @modified By:
 */
public class ModItemProvider extends ItemModelProvider {
    public ModItemProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, PetHomeMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(PHItemRegistry.COLLAR_TAG);
        simpleItem(PHItemRegistry.DEED_OF_OWNERSHIP);
        simpleItem(PHItemRegistry.DEFLECTION_SHIELD);
        handheldItem(PHItemRegistry.NET_ITEM.get());

        handheldItem(PHItemRegistry.NET_HAS_ITEM.get());

    }
    private ItemModelBuilder simpleItem(DeferredItem<Item> item) {
        return withExistingParent(item.getId().getPath(),
                 ResourceLocation.parse("item/generated")).texture("layer0",
              ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID,"item/" + item.getId().getPath()));
    }
    private ItemModelBuilder handheldItem(DeferredItem<Item> item) {
        return withExistingParent(item.getId().getPath(),
              ResourceLocation.parse("item/handheld")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "item/" + item.getId().getPath()));
    }

}
