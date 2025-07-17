package com.github.yzqdev.pet_home.datagen.loot;

import com.github.yzqdev.pet_home.PetHomeMod;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;


import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class GlobalLootModifier extends GlobalLootModifierProvider {


    public GlobalLootModifier(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, PetHomeMod.MODID);
    }


    @Override
    public void start() {
//        add("pet_home_chest", new AddTableLootModifier(
//                new LootItemCondition[]{new ModLootTableTypeCondition("chests/")},
//                LootTableGen.PET_LOOT_TABLE));
        add("sinister_carrot", new PHLootModifier(new LootItemCondition[]{new LootTableIdCondition.Builder(BuiltInLootTables.WOODLAND_MANSION.location()).build()}, 0));
        add("ore_scenting_enchanted_book", new PHLootModifier(new LootItemCondition[]{new LootTableIdCondition.Builder(BuiltInLootTables.ABANDONED_MINESHAFT.location()).build()}, 4));
        add("bubbling_enchanted_book", new PHLootModifier(new LootItemCondition[]{new LootTableIdCondition.Builder(BuiltInLootTables.BURIED_TREASURE.location()).build()}, 1));
        add("vampirism_enchanted_book", new PHLootModifier(new LootItemCondition[]{new LootTableIdCondition.Builder(BuiltInLootTables.WOODLAND_MANSION.location()).build()}, 2));
        add("blazing_protection_enchanted_book", new PHLootModifier(new LootItemCondition[]{new LootTableIdCondition.Builder(BuiltInLootTables.NETHER_BRIDGE.location()).build()}, 6));
        add("share_enchanted_book", new PHLootModifier(new LootItemCondition[]{new LootTableIdCondition.Builder(BuiltInLootTables.END_CITY_TREASURE.location()).build()}, 3));
        add("sonic_boom_enchanted_book", new PHLootModifier(new LootItemCondition[]{new LootTableIdCondition.Builder(BuiltInLootTables.ANCIENT_CITY.location()).build()}, 5));
        add("paralysis_enchanted_book", new PHLootModifier(new LootItemCondition[]{AnyOfCondition.anyOf(manyChests(BuiltInLootTables.ABANDONED_MINESHAFT, BuiltInLootTables.DESERT_PYRAMID, BuiltInLootTables.FISHING, BuiltInLootTables.SPAWN_BONUS_CHEST)).build()}, 7));
        add("tough_enchant_boot", new AddTableLootModifier(new LootItemCondition[]{LootTableIdCondition.builder(BuiltInLootTables.ABANDONED_MINESHAFT.location()).build()},LootTableGen.PET_PROTECTION_TABLE) );
    }

    @SafeVarargs
    private LootTableIdCondition.Builder[] manyChests(ResourceKey<LootTable>... lootablekey) {


        return Arrays.stream(lootablekey).map(i -> LootTableIdCondition.builder(i.location())).toArray(LootTableIdCondition.Builder[]::new);
    }
}