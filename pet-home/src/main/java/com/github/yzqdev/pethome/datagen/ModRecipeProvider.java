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

public class ModRecipeProvider extends RecipeProvider {


    public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
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
                    .requires(ModTags.PetBedKey)
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
