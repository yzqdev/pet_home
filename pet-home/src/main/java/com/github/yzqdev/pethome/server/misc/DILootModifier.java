package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;


public class DILootModifier {

    public static void init() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (id.equals(BuiltInLootTables.WOODLAND_MANSION)) {

                tableBuilder.withPool(net.minecraft.world.level.storage.loot.LootPool.lootPool()
                        .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.randomChance(PetHomeMod.CONFIG.sinisterCarrotLootChance.get().floatValue()))
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(PHItemRegistry.SINISTER_CARROT)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))));

                addEnchantedBook(tableBuilder, DIEnchantmentRegistry.VAMPIRE, PetHomeMod.CONFIG.vampirismLootChance.get().floatValue());
            } else if (id.equals(BuiltInLootTables.BURIED_TREASURE)) {
                addEnchantedBook(tableBuilder, DIEnchantmentRegistry.BUBBLING, PetHomeMod.CONFIG.bubblingLootChance.get().floatValue());
            } else if (id.equals(BuiltInLootTables.END_CITY_TREASURE)) {
                addEnchantedBook(tableBuilder, DIEnchantmentRegistry.VOID_CLOUD, PetHomeMod.CONFIG.voidCloudLootChance.get().floatValue());
                // Share（平摊）：末地城
                addEnchantedBook(tableBuilder, DIEnchantmentRegistry.SHARE, PetHomeMod.CONFIG.shareLootChance.get().floatValue());
            } else if (id.equals(BuiltInLootTables.ABANDONED_MINESHAFT)) {
                addEnchantedBook(tableBuilder, DIEnchantmentRegistry.ORE_SCENTING, PetHomeMod.CONFIG.oreScentingLootChance.get().floatValue());
                // Tough（稳固）/ Paralysis（麻痹）：废弃矿井
                addEnchantedBook(tableBuilder, DIEnchantmentRegistry.TOUGH, PetHomeMod.CONFIG.toughLootChance.get().floatValue());
                addEnchantedBook(tableBuilder, DIEnchantmentRegistry.PARALYSIS, PetHomeMod.CONFIG.paralysisLootChance.get().floatValue());
            } else if (id.equals(BuiltInLootTables.ANCIENT_CITY)) {
                addEnchantedBook(tableBuilder, DIEnchantmentRegistry.MUFFLED, PetHomeMod.CONFIG.muffledLootChance.get().floatValue());
                // Sonic Boom（冲击波）：远古城市
                addEnchantedBook(tableBuilder, DIEnchantmentRegistry.SONIC_BOOM, PetHomeMod.CONFIG.sonicBoomLootChance.get().floatValue());
            } else if (id.equals(BuiltInLootTables.NETHER_BRIDGE)) {
                addEnchantedBook(tableBuilder, DIEnchantmentRegistry.BLAZING_PROTECTION, PetHomeMod.CONFIG.blazingProtectionLootChance.get().floatValue());
            } else if (id.equals(BuiltInLootTables.DESERT_PYRAMID) || id.equals(BuiltInLootTables.SPAWN_BONUS_CHEST)) {
                // Paralysis（麻痹）：沙漠神殿 / 初始宝箱
                addEnchantedBook(tableBuilder, DIEnchantmentRegistry.PARALYSIS, PetHomeMod.CONFIG.paralysisLootChance.get().floatValue());
            }
        });
    }

    private static void addEnchantedBook(net.minecraft.world.level.storage.loot.LootTable.Builder tableBuilder, net.minecraft.world.item.enchantment.Enchantment enchantment, float chance) {
        int maxLevels = enchantment.getMaxLevel();
        SetEnchantmentsFunction.Builder function = new SetEnchantmentsFunction.Builder().withEnchantment(enchantment,
                maxLevels > 1 ? UniformGenerator.between(1.0F, maxLevels) : ConstantValue.exactly(1));
        tableBuilder.withPool(net.minecraft.world.level.storage.loot.LootPool.lootPool()
                .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.randomChance(chance))
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(net.minecraft.world.item.Items.ENCHANTED_BOOK).apply(function)));
    }
}
