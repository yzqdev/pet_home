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
public class ModRecipeProvider extends RecipeProvider {


    public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
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
                    .requires(ModTags.PetBedKey)
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
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Recipes - " + PetHomeMod.MODID;
        }
    }
}
