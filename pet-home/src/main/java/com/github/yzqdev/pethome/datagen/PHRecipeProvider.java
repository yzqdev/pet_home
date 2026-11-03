package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;


/** 配方 datagen：捕网 / 捕网发射器 / 宠物罗盘 / 哨子 / 自由之铃合成配方（均以材料存在为解锁条件）。 */
public class PHRecipeProvider extends FabricRecipeProvider {

    public PHRecipeProvider(FabricDataOutput output) {
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

        // 哨子：竖排 竹子（管身）+ 铜锭（哨芯）+ 皮革（挂绳），1×3 呼应项圈牌的竖排风格（72fa6ca3）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PHItemRegistry.WHISTLE)
                .pattern("b").pattern("c").pattern("l")
                .define('b', Items.BAMBOO).define('c', Items.COPPER_INGOT).define('l', Items.LEATHER)
                .unlockedBy("has_bamboo", has(Items.BAMBOO))
                .save(writer);

        // 自由之铃：铃形 金锭×4（铃身）+ 铁链（铃舌/提绳），放生是消耗性高危操作所以用金（72fa6ca3）
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PHItemRegistry.FREEDOM_BELL)
                .pattern(" g ").pattern("g g").pattern(" c ")
                .define('g', Items.GOLD_INGOT).define('c', Items.CHAIN)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(writer);
    }

    @Override
    public String getName() {
        return "pet_home recipes";
    }
}
