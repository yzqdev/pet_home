package com.github.yzqdev.pethome.datagen.loot;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.ModEnchantments;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;


public class PHLootRegistry {

    public static void init() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (source.isBuiltin()) {
                return;
            }
            for (int lootType : lootTypesFor(key)) {
                applyLootType(tableBuilder, registries, lootType);
            }
        });
    }

    private static int[] lootTypesFor(ResourceKey<LootTable> key) {
        // loot_type: 0 sinister_carrot, 1 bubbling, 2 vampirism, 3 share, 4 ore_scenting,
        //            5 sonic_boom, 6 blazing_protection, 7 paralysis, 8 tough, 9 void_cloud
        if (key.location().equals(BuiltInLootTables.WOODLAND_MANSION.location())) {
            return new int[]{0, 2};
        }
        if (key.location().equals(BuiltInLootTables.BURIED_TREASURE.location())) {
            return new int[]{1};
        }
        if (key.location().equals(BuiltInLootTables.END_CITY_TREASURE.location())) {
            return new int[]{3, 9};
        }
        if (key.location().equals(BuiltInLootTables.ABANDONED_MINESHAFT.location())) {
            return new int[]{4, 7, 8};
        }
        if (key.location().equals(BuiltInLootTables.ANCIENT_CITY.location())) {
            return new int[]{5};
        }
        if (key.location().equals(BuiltInLootTables.NETHER_BRIDGE.location())) {
            return new int[]{6};
        }
        if (key.location().equals(BuiltInLootTables.DESERT_PYRAMID.location())
                || key.location().equals(BuiltInLootTables.SPAWN_BONUS_CHEST.location())) {
            return new int[]{7};
        }
        return new int[0];
    }

    private static void applyLootType(LootTable.Builder tableBuilder, HolderLookup.Provider registries, int lootType) {
        switch (lootType) {
            case 0 -> {
                if (PetHomeConfig.sinisterCarrotLootChance > 0) {
                    tableBuilder.withPool(LootPool.lootPool()
                            .when(LootItemRandomChanceCondition.randomChance((float) PetHomeConfig.sinisterCarrotLootChance))
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(PHItemRegistry.SINISTER_CARROT)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))));
                }
            }
            case 1 -> addEnchantedBook(tableBuilder, registries, ModEnchantments.BUBBLING, PetHomeConfig.bubblingLootChance);
            case 2 -> addEnchantedBook(tableBuilder, registries, ModEnchantments.VAMPIRE, PetHomeConfig.vampirismLootChance);
            case 3 -> addEnchantedBook(tableBuilder, registries, ModEnchantments.SHARE, PetHomeConfig.shareLootChance);
            case 4 -> addEnchantedBook(tableBuilder, registries, ModEnchantments.ORE_SCENTING, PetHomeConfig.oreScentingLootChance);
            case 5 -> addEnchantedBook(tableBuilder, registries, ModEnchantments.SonicBoom, PetHomeConfig.sonicBoomLootChance);
            case 6 -> addEnchantedBook(tableBuilder, registries, ModEnchantments.BLAZING_PROTECTION, PetHomeConfig.blazingProtectionLootChance);
            case 7 -> addEnchantedBook(tableBuilder, registries, ModEnchantments.PARALYSIS, PetHomeConfig.paralysisLootChance);
            case 8 -> addEnchantedBook(tableBuilder, registries, ModEnchantments.TOUGH, PetHomeConfig.toughLootChance);
            case 9 -> addEnchantedBook(tableBuilder, registries, ModEnchantments.VOID_CLOUD, PetHomeConfig.voidCloudLootChance);
        }
    }

    private static void addEnchantedBook(LootTable.Builder tableBuilder, HolderLookup.Provider registries, ResourceKey<Enchantment> enchantmentKey, double chance) {
        // 被配置禁用的附魔不再出现在战利品里
        if (!PetHomeConfig.isEnchantEnabled(enchantmentKey)) {
            return;
        }
        var reg = registries.lookupOrThrow(Registries.ENCHANTMENT);
        var enchantHolder = reg.get(enchantmentKey).orElseThrow();
        int maxLevels = enchantHolder.value().getMaxLevel();
        SetEnchantmentsFunction.Builder function = new SetEnchantmentsFunction.Builder().withEnchantment(enchantHolder,
                maxLevels > 1 ? UniformGenerator.between(1.0F, maxLevels - 1.0F) : ConstantValue.exactly(1));
        tableBuilder.withPool(LootPool.lootPool()
                .when(LootItemRandomChanceCondition.randomChance((float) chance))
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK).apply(function)));
    }
}
