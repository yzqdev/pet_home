package com.github.yzqdev.pethome.datagen.loot;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
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
        add("sinister_carrot", new PHLootModifier(new LootItemCondition[]{new LootTableIdCondition.Builder(
                BuiltInLootTables.WOODLAND_MANSION.identifier()).build()}, 0, 0));
        add("ore_scenting_enchanted_book", new PHLootModifier(new LootItemCondition[]{new LootTableIdCondition.Builder(
                BuiltInLootTables.ABANDONED_MINESHAFT.identifier()).build()}, 0, 4));
        add("bubbling_enchanted_book", new PHLootModifier(new LootItemCondition[]{new LootTableIdCondition.Builder(
                BuiltInLootTables.BURIED_TREASURE.identifier()).build()}, 0, 1));
        add("vampirism_enchanted_book", new PHLootModifier(new LootItemCondition[]{new LootTableIdCondition.Builder(
                BuiltInLootTables.WOODLAND_MANSION.identifier()).build()}, 0, 2));
        add("blazing_protection_enchanted_book", new PHLootModifier(
                new LootItemCondition[]{new LootTableIdCondition.Builder(
                        BuiltInLootTables.NETHER_BRIDGE.identifier()).build()}, 0, 6));
        add("share_enchanted_book", new PHLootModifier(new LootItemCondition[]{new LootTableIdCondition.Builder(
                BuiltInLootTables.END_CITY_TREASURE.identifier()).build()}, 0, 3));
        add("sonic_boom_enchanted_book", new PHLootModifier(new LootItemCondition[]{new LootTableIdCondition.Builder(
                BuiltInLootTables.ANCIENT_CITY.identifier()).build()}, 0, 5));
        add("paralysis_enchanted_book", new PHLootModifier(new LootItemCondition[]{AnyOfCondition.anyOf(
                manyChests(BuiltInLootTables.ABANDONED_MINESHAFT, BuiltInLootTables.DESERT_PYRAMID,
                          BuiltInLootTables.SPAWN_BONUS_CHEST)).build()}, 0, 7));
        add("tough_enchant_book", new PHLootModifier(new LootItemCondition[]{LootTableIdCondition.builder(
                BuiltInLootTables.ABANDONED_MINESHAFT.identifier()).build()}, 0, 8));
        // below using table but its chance is constant
//        add("tough_enchant_book", new AddTableLootModifier(new LootItemCondition[]{LootTableIdCondition.builder(BuiltInLootTables.ABANDONED_MINESHAFT.identifier()).build()},LootTableGen.PET_PROTECTION_TABLE) );
    }

    @SafeVarargs
    private LootTableIdCondition.Builder[] manyChests(ResourceKey<LootTable>... lootablekey) {


        return Arrays.stream(lootablekey)
                .map(i -> LootTableIdCondition.builder(i.identifier()))
                .toArray(LootTableIdCondition.Builder[]::new);
    }
}