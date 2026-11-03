package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

/**
 * 配方数据生成：往生灯笼/指令鼓/项圈标签/羽毛钓竿/捕网/捕网发射器/宠物罗盘的
 * 合成配方，以及 16 色宠物床的染色与羊毛+骨头两套配方。
 */
public class PHRecipeProvider extends RecipeProvider {


    public PHRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }


    @Override
    public void buildRecipes() {

        this.shaped(RecipeCategory.MISC, PHBlockRegistry.WAYWARD_LANTERN).pattern("LLL").pattern("LIL").pattern(" L ").define('I', Items.LANTERN).define('L', ConventionalItemTags.IRON_INGOTS)
                .unlockedBy("has_craft", has(Items.CRAFTING_TABLE)).save(this.output);

        this.shaped(RecipeCategory.MISC, PHBlockRegistry.DRUM).pattern("LLL").pattern("P P").pattern("PPP").define('L', Items.LEATHER).define('P', ItemTags.PLANKS)
                .unlockedBy("has_craft", has(Items.CRAFTING_TABLE)).save(this.output);
        this.shaped(RecipeCategory.MISC, PHItemRegistry.COLLAR_TAG).pattern("I").pattern("C").define('I', Items.IRON_CHAIN).define('C', ConventionalItemTags.COPPER_INGOTS)
                .unlockedBy("has_craft", has(Items.CRAFTING_TABLE)).save(this.output);
        this.shaped(RecipeCategory.MISC, PHItemRegistry.FEATHER_ON_A_STICK).pattern("I ").pattern(" C").define('I', Items.FISHING_ROD).define('C', ConventionalItemTags.FEATHERS)
                .unlockedBy("has_craft", has(Items.CRAFTING_TABLE)).save(this.output);

        PHBlockRegistry.PetBedItems.forEach((color, item) -> {
            this.shapeless(RecipeCategory.TOOLS, item, 1)
                    .unlockedBy("has_bone", has(Items.BONE))
                    .requires(PHTags.PetBedKey)
                    .requires(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", color + "_dye")))
                    .save(this.output, PetHomeMod.MODID + ":pet_bed_from_dye_" + color);
            this.shapeless(RecipeCategory.TOOLS, item, 1)
                    .unlockedBy("has_bone", has(Items.BONE))
                    .requires(ItemTags.PLANKS)
                    .requires(Items.BONE)
                    .requires(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", color + "_wool")))

                    .save(this.output, PetHomeMod.MODID + ":pet_bed_item_" + color);
        });
        this.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_ITEM).pattern(" w ").pattern("wew").pattern(" w ").define('w', ConventionalItemTags.IRON_INGOTS).define('e', Items.ENDER_PEARL).unlockedBy("has_craft", has(Items.CRAFTING_TABLE)).save(this.output);


        this.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_LAUNCHER_ITEM).pattern("iii").pattern(" eb").pattern("iii").define('b', Items.BOW).define('e', Items.ENDER_PEARL).define('i', ConventionalItemTags.IRON_INGOTS).unlockedBy("has_craft", has(Items.CRAFTING_TABLE)).save(this.output);

        // 宠物罗盘：指南针 + 皮革（三侧同款配方）
        this.shaped(RecipeCategory.TOOLS, PHItemRegistry.PET_COMPASS).pattern(" l ").pattern("lcl").pattern(" l ")
                .define('l', Items.LEATHER).define('c', Items.COMPASS)
                .unlockedBy("has_compass", has(Items.COMPASS)).save(this.output);

        // 哨子：竖排 竹子（管身）+ 铜锭（哨芯）+ 皮革（挂绳），1×3 呼应项圈牌的竖排风格
        this.shaped(RecipeCategory.TOOLS, PHItemRegistry.WHISTLE).pattern("b").pattern("c").pattern("l")
                .define('b', Items.BAMBOO).define('c', ConventionalItemTags.COPPER_INGOTS).define('l', Items.LEATHER)
                .unlockedBy("has_bamboo", has(Items.BAMBOO)).save(this.output);

        // 自由之铃：铃形 金锭×4（铃身）+ 铁链（铃舌/提绳），放生是消耗性高危操作所以用金
        this.shaped(RecipeCategory.TOOLS, PHItemRegistry.FREEDOM_BELL).pattern(" g ").pattern("g g").pattern(" c ")
                .define('g', ConventionalItemTags.GOLD_INGOTS).define('c', Items.IRON_CHAIN)
                .unlockedBy("has_gold_ingot", has(ConventionalItemTags.GOLD_INGOTS)).save(this.output);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new PHRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Recipes - " + PetHomeMod.MODID;
        }
    }
}
