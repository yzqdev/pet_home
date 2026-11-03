package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/** 配方 datagen：往生灯笼 / 指令鼓 / 项圈标签等合成配方与驯兽师村民交易（附魔书、宠物床、捕网等）。 */
public class PHRecipeProvider extends FabricRecipeProvider {

  public PHRecipeProvider(
      FabricDataOutput output,
      CompletableFuture<net.minecraft.core.HolderLookup.Provider> registries) {
    super(output, registries);
  }

  @Override
  public void buildRecipes(RecipeOutput pWriter) {

    ShapedRecipeBuilder.shaped(
            RecipeCategory.MISC,
            BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "wayward_lantern")))
        .pattern("LLL")
        .pattern("LIL")
        .pattern(" L ")
        .define('I', Items.LANTERN)
        .define('L', Ingredient.of(ConventionalItemTags.IRON_INGOTS))
        .unlockedBy(
            "has_craft", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE))
        .save(pWriter);
    // 1.20 侧 drum.json 配方的移植（皮革 x3 + 木板 x3）
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PHBlockRegistry.DRUM)
        .pattern("LLL")
        .pattern("P P")
        .pattern("PPP")
        .define('L', Items.LEATHER)
        .define('P', ItemTags.PLANKS)
        .unlockedBy(
            "has_craft", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE))
        .save(pWriter);
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PHItemRegistry.COLLAR_TAG)
        .pattern("I")
        .pattern("C")
        .define('I', Items.CHAIN)
        .define('C', Ingredient.of(ConventionalItemTags.COPPER_INGOTS))
        .unlockedBy(
            "has_craft", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE))
        .save(pWriter);
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PHItemRegistry.FEATHER_ON_A_STICK)
        .pattern("I ")
        .pattern(" C")
        .define('I', Items.FISHING_ROD)
        .define('C', Items.FEATHER)
        .unlockedBy(
            "has_craft", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE))
        .save(pWriter);

    PHBlockRegistry.PetBedItems.forEach(
        (color, item) -> {
          ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, item, 1)
              .unlockedBy("has_bone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BONE))
              .requires(PHTags.PetBedKey)
              .requires(
                  BuiltInRegistries.ITEM.get(
                      ResourceLocation.fromNamespaceAndPath("minecraft", color + "_dye")))
              .save(pWriter, PetHomeMod.MODID + ":pet_bed_from_dye_" + color);
          ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, item, 1)
              .unlockedBy("has_bone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BONE))
              .requires(ItemTags.PLANKS)
              .requires(Items.BONE)
              .requires(
                  BuiltInRegistries.ITEM.get(
                      ResourceLocation.fromNamespaceAndPath("minecraft", color + "_wool")))
              .save(pWriter, PetHomeMod.MODID + ":pet_bed_item_" + color);
        });
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_ITEM)
        .pattern(" w ")
        .pattern("wew")
        .pattern(" w ")
        .define('w', Ingredient.of(ConventionalItemTags.IRON_INGOTS))
        .define('e', Items.ENDER_PEARL)
        .unlockedBy(
            "has_craft", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE))
        .save(pWriter);

    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, PHItemRegistry.NET_LAUNCHER_ITEM)
        .pattern("iii")
        .pattern(" eb")
        .pattern("iii")
        .define('b', Items.BOW)
        .define('e', Items.ENDER_PEARL)
        .define('i', Ingredient.of(ConventionalItemTags.IRON_INGOTS))
        .unlockedBy(
            "has_craft", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRAFTING_TABLE))
        .save(pWriter);

    // 宠物罗盘：指南针 + 皮革（与 1.21/furina 侧配方一致）
    ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PHItemRegistry.PET_COMPASS)
        .pattern(" l ")
        .pattern("lcl")
        .pattern(" l ")
        .define('l', Items.LEATHER)
        .define('c', Items.COMPASS)
        .unlockedBy("has_compass", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COMPASS))
        .save(pWriter);
    // 哨子：竖排 竹子（管身）+ 铜锭（哨芯）+ 皮革（挂绳），1×3 呼应项圈牌的竖排风格（72fa6ca3）
    ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PHItemRegistry.WHISTLE)
        .pattern("b")
        .pattern("c")
        .pattern("l")
        .define('b', Items.BAMBOO)
        .define('c', Ingredient.of(ConventionalItemTags.COPPER_INGOTS))
        .define('l', Items.LEATHER)
        .unlockedBy("has_bamboo", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BAMBOO))
        .save(pWriter);

    // 自由之铃：铃形 金锭×4（铃身）+ 铁链（铃舌/提绳），放生是消耗性高危操作所以用金（72fa6ca3）
    ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, PHItemRegistry.FREEDOM_BELL)
        .pattern(" g ")
        .pattern("g g")
        .pattern(" c ")
        .define('g', Ingredient.of(ConventionalItemTags.GOLD_INGOTS))
        .define('c', Items.CHAIN)
        .unlockedBy(
            "has_gold_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GOLD_INGOT))
        .save(pWriter);
  }
}
