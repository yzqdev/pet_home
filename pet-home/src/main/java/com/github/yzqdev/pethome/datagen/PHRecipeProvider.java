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
public class PHRecipeProvider extends RecipeProvider implements IConditionBuilder {


    public PHRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_ITEM.get()).pattern(" w ").pattern("wew").pattern(" w ").define('w', Tags.Items.INGOTS_IRON).define('e', Items.ENDER_PEARL).unlockedBy("has_craft",has(Items.CRAFTING_TABLE)).save(pWriter);


        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_LAUNCHER_ITEM.get()).pattern("iii").pattern(" eb").pattern("iii").define('b', Items.BOW).define('e', Items.ENDER_PEARL).define('i',Tags.Items.INGOTS_IRON).unlockedBy("has_craft",has(Items.CRAFTING_TABLE)).save(pWriter);

        // 宠物罗盘：指南针 + 项圈牌 + 皮革（1.21/26.1 同款配方）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PHItemRegistry.PET_COMPASS.get()).pattern(" l ").pattern("lcl").pattern(" l ").define('l', Items.LEATHER).define('c', Items.COMPASS).unlockedBy("has_compass", has(Items.COMPASS)).save(pWriter);

        // 哨子：竖排 竹子（管身）+ 铜锭（哨芯）+ 皮革（挂绳），1×3 呼应项圈牌的竖排风格（1.21/26.1/26.3 同款配方）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PHItemRegistry.WHISTLE.get()).pattern("b").pattern("c").pattern("l")
                .define('b', Items.BAMBOO).define('c', Tags.Items.INGOTS_COPPER).define('l', Items.LEATHER)
                .unlockedBy("has_bamboo", has(Items.BAMBOO)).save(pWriter);

        // 自由之铃：铃形 金锭×4（铃身）+ 铁链（铃舌/提绳），放生是消耗性高危操作所以用金
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PHItemRegistry.FREEDOM_BELL.get()).pattern(" g ").pattern("g g").pattern(" c ")
                .define('g', Tags.Items.INGOTS_GOLD).define('c', Items.CHAIN)
                .unlockedBy("has_gold_ingot", has(Tags.Items.INGOTS_GOLD)).save(pWriter);

    }
}
