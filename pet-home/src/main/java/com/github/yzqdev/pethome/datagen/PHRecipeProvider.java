package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

/**
 * @author yzqde
 * @date time 2024/12/11 14:14
 * @modified By:
 */
public class PHRecipeProvider extends RecipeProvider {


    public PHRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {

        this.shaped(RecipeCategory.MISC, PHBlockRegistry.WAYWARD_LANTERN.get()).pattern("LLL").pattern("LIL").pattern(" L ").define('I', Items.LANTERN).define('L', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_craft", has(Items.CRAFTING_TABLE)).save(this.output);

        this.shaped(RecipeCategory.MISC, PHBlockRegistry.DRUM.get()).pattern("LLL").pattern("P P").pattern("PPP").define('L', Items.LEATHER).define('P', ItemTags.PLANKS)
                .unlockedBy("has_craft", has(Items.CRAFTING_TABLE)).save(this.output);
        this.shaped(RecipeCategory.MISC, PHItemRegistry.COLLAR_TAG.get()).pattern("I").pattern("C").define('I', Items.IRON_CHAIN).define('C', Tags.Items.INGOTS_COPPER)
                .unlockedBy("has_craft", has(Items.CRAFTING_TABLE)).save(this.output);
        this.shaped(RecipeCategory.MISC, PHItemRegistry.FEATHER_ON_A_STICK.get()).pattern("I ").pattern(" C").define('I', Items.FISHING_ROD).define('C', Tags.Items.FEATHERS)
                .unlockedBy("has_craft", has(Items.CRAFTING_TABLE)).save(this.output);
        // 宠物罗盘：指南针 + 项圈牌 + 皮革
        this.shaped(RecipeCategory.TOOLS, PHItemRegistry.PET_COMPASS.get()).pattern(" l ").pattern("lcl").pattern(" l ")
                .define('l', Items.LEATHER).define('c', Items.COMPASS)
                .unlockedBy("has_compass", has(Items.COMPASS)).save(this.output);

        PHBlockRegistry.PetBedItems.forEach((color, item) -> {
            this.shapeless(RecipeCategory.TOOLS, item.get(), 1)
                    .unlockedBy("has_bone", has(Items.BONE))
                    .requires(PHTags.PetBedKey)
                    .requires(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", color + "_dye")))
                    .save(this.output, PetHomeMod.MODID + ":pet_bed_from_dye_" + color);
            this.shapeless(RecipeCategory.TOOLS, item.get(), 1)
                    .unlockedBy("has_bone", has(Items.BONE))
                    .requires(ItemTags.PLANKS)
                    .requires(Items.BONE)
                    .requires(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", color + "_wool")))

                    .save(this.output, PetHomeMod.MODID + ":pet_bed_item_" + color);
        });
        this.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_ITEM.get()).pattern(" w ").pattern("wew").pattern(" w ").define('w', Tags.Items.INGOTS_IRON).define('e', Items.ENDER_PEARL).unlockedBy("has_craft", has(Items.CRAFTING_TABLE)).save(this.output);


        this.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_LAUNCHER_ITEM.get()).pattern("iii").pattern(" eb").pattern("iii").define('b', Items.BOW).define('e', Items.ENDER_PEARL).define('i', Tags.Items.INGOTS_IRON).unlockedBy("has_craft", has(Items.CRAFTING_TABLE)).save(this.output);

        // 哨子：竖排 竹子（管身）+ 铜锭（哨芯）+ 皮革（挂绳），1×3 呼应项圈牌的竖排风格
        this.shaped(RecipeCategory.TOOLS, PHItemRegistry.WHISTLE.get()).pattern("b").pattern("c").pattern("l")
                .define('b', Items.BAMBOO).define('c', Tags.Items.INGOTS_COPPER).define('l', Items.LEATHER)
                .unlockedBy("has_bamboo", has(Items.BAMBOO)).save(this.output);

        // 自由之铃：铃形 金锭×4（铃身）+ 铁链（铃舌/提绳），放生是消耗性高危操作所以用金
        this.shaped(RecipeCategory.TOOLS, PHItemRegistry.FREEDOM_BELL.get()).pattern(" g ").pattern("g g").pattern(" c ")
                .define('g', Tags.Items.INGOTS_GOLD).define('c', Items.IRON_CHAIN)
                .unlockedBy("has_gold_ingot", has(Tags.Items.INGOTS_GOLD)).save(this.output);
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
