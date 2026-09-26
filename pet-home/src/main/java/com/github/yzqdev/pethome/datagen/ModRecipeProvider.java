package com.github.yzqdev.pethome.datagen;


import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

/**
 * @author yzqde
 * @date time 2024/12/11 14:14
 * @modified By:
 */
public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {


    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_ITEM.get()).pattern(" w ").pattern("wew").pattern(" w ").define('w', Tags.Items.INGOTS_IRON).define('e', Items.ENDER_PEARL).unlockedBy("has_craft",has(Items.CRAFTING_TABLE)).save(pWriter);


        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_LAUNCHER_ITEM.get()).pattern("iii").pattern(" eb").pattern("iii").define('b', Items.BOW).define('e', Items.ENDER_PEARL).define('i',Tags.Items.INGOTS_IRON).unlockedBy("has_craft",has(Items.CRAFTING_TABLE)).save(pWriter);

        // 宠物罗盘：指南针 + 项圈牌 + 皮革（1.21/26.1 同款配方）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PHItemRegistry.PET_COMPASS.get()).pattern(" l ").pattern("lcl").pattern(" l ").define('l', Items.LEATHER).define('c', Items.COMPASS).unlockedBy("has_compass", has(Items.COMPASS)).save(pWriter);

    }
}
