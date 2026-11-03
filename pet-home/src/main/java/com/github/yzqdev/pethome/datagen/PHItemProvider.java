package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.DyeColors;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;


/**
 * @author yzqde
 * @date time 2024/12/11 14:45
 * @modified By:
 */
public class PHItemProvider extends ItemModelProvider {
    public PHItemProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, PetHomeMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        simpleItem(PHItemRegistry.COLLAR_TAG);
        simpleItem(PHItemRegistry.DEED_OF_OWNERSHIP);
        simpleItem(PHItemRegistry.DEFLECTION_SHIELD);
        simpleItem(PHItemRegistry.ROTTEN_APPLE);
        simpleItem(PHItemRegistry.SINISTER_CARROT);
        simpleItem(PHItemRegistry.MAGNET);
        handheldItem(PHItemRegistry.NET_ITEM.get());

        handheldItem(PHItemRegistry.NET_HAS_ITEM.get());

        // 指挥鼓：物品模型复用 PHBlockStateProvider 生成的方块模型（UncheckedModelFile 跨 provider 引用）
        getBuilder("drum").parent(new ModelFile.UncheckedModelFile(
                ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "block/drum_wander")));

        // 宠物床：BlockItem 引用同色方块模型（与 PHBlockStateProvider 的色座模型同名）
        for (var color : DyeColors.COLORS.keySet()) {
            getBuilder("pet_bed_" + color.name().toLowerCase()).parent(new ModelFile.UncheckedModelFile(
                    ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "block/pet_bed_" + color.name().toLowerCase())));
        }

        // 往生灯笼：BlockItem 引用静态 12 元素方块模型，display 变换复刻原静态物品模型
        getBuilder("wayward_lantern")
                .parent(new ModelFile.UncheckedModelFile(
                        ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "block/wayward_lantern")))
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(75, 45, 0).translation(0, 2.5f, 0).scale(0.375f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(75, 45, 0).translation(0, 2.5f, 0).scale(0.375f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 45, 0).translation(1.5f, 2.5f, 0).scale(0.4f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 225, 0).translation(2, 2.5f, 0).scale(0.4f).end()
                .transform(ItemDisplayContext.GROUND).translation(0, 0.5f, 0).scale(0.5f).end()
                .transform(ItemDisplayContext.GUI).rotation(30, 225, 0).translation(0, -1.25f, 0).scale(0.8f).end()
                .transform(ItemDisplayContext.HEAD).rotation(0, 180, 0).translation(0, 13, -0.25f).scale(1.5f).end()
                .transform(ItemDisplayContext.FIXED).translation(0, -1.75f, -0.75f).end()
                .end();

        handheldItem(PHItemRegistry.PET_COMPASS);
        simpleItem(PHItemRegistry.WHISTLE);
        simpleItem(PHItemRegistry.FREEDOM_BELL);
    }

    private ItemModelBuilder simpleItem(DeferredItem<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "item/" + item.getId().getPath()));
    }

    private ItemModelBuilder handheldItem(DeferredItem<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "item/" + item.getId().getPath()));
    }

}
