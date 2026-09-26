package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;


public class ModRecipeProvider extends FabricRecipeProvider {

    public ModRecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> writer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_ITEM)
                .pattern(" w ").pattern("wew").pattern(" w ")
                .define('w', Items.IRON_INGOT).define('e', Items.ENDER_PEARL)
                .unlockedBy("has_craft", has(Items.CRAFTING_TABLE))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_LAUNCHER_ITEM)
                .pattern("iii").pattern(" eb").pattern("iii")
                .define('b', Items.BOW).define('e', Items.ENDER_PEARL).define('i', Items.IRON_INGOT)
                .unlockedBy("has_craft", has(Items.CRAFTING_TABLE))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PHItemRegistry.PET_COMPASS)
                .pattern(" l ").pattern("lcl").pattern(" l ")
                .define('l', Items.LEATHER).define('c', Items.COMPASS)
                .unlockedBy("has_compass", has(Items.COMPASS))
                .save(writer);
    }

    @Override
    public String getName() {
        return "pet_home recipes";
    }
}
