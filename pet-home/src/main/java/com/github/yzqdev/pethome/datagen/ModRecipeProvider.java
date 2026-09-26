package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;

import java.util.concurrent.CompletableFuture;

/**
 * @author yzqde
 * @date time 2024/12/11 14:14
 * @modified By:
 */
public class ModRecipeProvider extends FabricRecipeProvider {


    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<net.minecraft.core.HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void buildRecipes(RecipeOutput pWriter) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "wayward_lantern"))).pattern("LLL").pattern("LIL").pattern(" L ").define('I', Items.LANTERN).define('L', Ingredient.of(ConventionalItemTags.IRON_INGOTS))
                .unlockedBy("has_craft", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE)).save(pWriter);
        // 1.20 侧 drum.json 配方的移植（皮革 x3 + 木板 x3）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PHBlockRegistry.DRUM).pattern("LLL").pattern("P P").pattern("PPP").define('L', Items.LEATHER).define('P', ItemTags.PLANKS)
                .unlockedBy("has_craft", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE)).save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PHItemRegistry.COLLAR_TAG).pattern("I").pattern("C").define('I', Items.CHAIN).define('C', Ingredient.of(ConventionalItemTags.COPPER_INGOTS))
                .unlockedBy("has_craft", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE)).save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PHItemRegistry.FEATHER_ON_A_STICK).pattern("I ").pattern(" C").define('I', Items.FISHING_ROD).define('C', Items.FEATHER)
                .unlockedBy("has_craft", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE)).save(pWriter);

        PHBlockRegistry.PetBedItems.forEach((color, item) -> {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, item, 1)
                    .unlockedBy("has_bone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BONE))
                    .requires(ModTags.PetBedKey)
                    .requires(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("minecraft", color + "_dye")))
                    .save(pWriter, PetHomeMod.MODID + ":pet_bed_from_dye_" + color);
            ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, item, 1)
                    .unlockedBy("has_bone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BONE))
                    .requires(ItemTags.PLANKS)
                    .requires(Items.BONE)
                    .requires(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("minecraft", color + "_wool")))

                    .save(pWriter, PetHomeMod.MODID + ":pet_bed_item_" + color);
        });
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_ITEM).pattern(" w ").pattern("wew").pattern(" w ").define('w', Ingredient.of(ConventionalItemTags.IRON_INGOTS)).define('e', Items.ENDER_PEARL).unlockedBy("has_craft", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE)).save(pWriter);


        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_LAUNCHER_ITEM).pattern("iii").pattern(" eb").pattern("iii").define('b', Items.BOW).define('e', Items.ENDER_PEARL).define('i', Ingredient.of(ConventionalItemTags.IRON_INGOTS)).unlockedBy("has_craft", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE)).save(pWriter);

        // 宠物罗盘：指南针 + 皮革（与 1.21/furina 侧配方一致）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PHItemRegistry.PET_COMPASS).pattern(" l ").pattern("lcl").pattern(" l ")
                .define('l', Items.LEATHER).define('c', Items.COMPASS)
                .unlockedBy("has_compass", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COMPASS)).save(pWriter);
    }
}
