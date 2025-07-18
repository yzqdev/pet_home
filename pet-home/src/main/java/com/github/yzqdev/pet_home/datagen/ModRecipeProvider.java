package com.github.yzqdev.pet_home.datagen;

import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.server.block.PHBlockRegistry;

import com.github.yzqdev.pet_home.server.item.PHItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * @author yzqde
 * @date time 2024/12/11 14:14
 * @modified By:
 */
public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {


    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput pWriter) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID,"wayward_lantern"))).pattern("LLL").pattern("LIL").pattern(" L ").define('I', Items.LANTERN).define('L', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_craft", has(Items.CRAFTING_TABLE)).save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PHItemRegistry.COLLAR_TAG.get()).pattern("I").pattern("C").define('I', Items.CHAIN).define('C', Tags.Items.INGOTS_COPPER)
                .unlockedBy("has_craft", has(Items.CRAFTING_TABLE)).save(pWriter);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PHItemRegistry.FEATHER_ON_A_STICK.get()).pattern("I ").pattern(" C").define('I', Items.FISHING_ROD).define('C', Tags.Items.FEATHERS)
                .unlockedBy("has_craft", has(Items.CRAFTING_TABLE)).save(pWriter);

        PHBlockRegistry.PetBedItems.forEach((color, item) ->{
            ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, item.get(), 1)
                    .unlockedBy("has_bone", has(Items.BONE))
                    .requires(ModTags.PetBedKey )
                    .requires(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("minecraft", color + "_dye")))
                    .save(pWriter, PetHomeMod.MODID + ":pet_bed_from_dye_" + color  );
            ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, item.get(), 1)
                    .unlockedBy("has_bone", has(Items.BONE))
                    .requires(ItemTags.PLANKS)
                    .requires(Items.BONE)
                    .requires(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("minecraft", color + "_wool")))

                    .save(pWriter, PetHomeMod.MODID + ":pet_bed_item_" + color  );
        });
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_ITEM.get()).pattern(" w ").pattern("wew").pattern(" w ").define('w', Tags.Items.INGOTS_IRON).define('e', Items.ENDER_PEARL).unlockedBy("has_craft",has(Items.CRAFTING_TABLE)).save(pWriter);


        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_LAUNCHER_ITEM.get()).pattern("iii").pattern(" eb").pattern("iii").define('b', Items.BOW).define('e', Items.ENDER_PEARL).define('i',Tags.Items.INGOTS_IRON).unlockedBy("has_craft",has(Items.CRAFTING_TABLE)).save(pWriter);
  }
}
