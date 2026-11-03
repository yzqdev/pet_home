package com.github.yzqdev.pethome.datagen.loot;

import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.datagen.PHTags;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * 26.3 迁移：vanilla LootTableProvider 变为 SingleRegistryBootstrap（不再走 DataProvider 工厂），
 * Fabric 侧 chest loot 统一改走 {@link SimpleFabricLootTableSubProvider}（generate(BiConsumer) 形式）。
 * 表内容与 NeoForge 26.3 基准一致（见 datagen/loot/LootTableGen 的表 key 常量）。
 */
public class PHLootProvider extends SimpleFabricLootTableSubProvider {

    private final CompletableFuture<HolderLookup.Provider> registriesFuture;

    public PHLootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture, LootContextParamSets.CHEST);
        this.registriesFuture = registriesFuture;
    }

    public void run() {
        // vanilla LootTableSubProvider.run()：实际生成由 DataProvider.run(CachedOutput) 驱动
    }

    /** 定义两张箱战利品表：{@code pet_protection}（Tough 附魔书）与 {@code pet_loot}（宠物杂项 + 项圈/附魔书）。 */
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        var enchantments = this.registriesFuture.join().lookupOrThrow(Registries.ENCHANTMENT);

        consumer.accept(LootTableGen.PET_PROTECTION_TABLE,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                                               .setRolls(ConstantValue.exactly(1))
                                .when(LootItemRandomChanceCondition.randomChance(0.05F))
                                .add(enchantItem(enchantments, Items.BOOK, PHEnchantments.TOUGH, 1, 4))));

        consumer.accept(LootTableGen.PET_LOOT_TABLE,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(LootItemRandomChanceCondition.randomChance(0.05F))
                                .add(LootItem.lootTableItem(PHItemRegistry.ROTTEN_APPLE)))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(LootItemRandomChanceCondition.randomChance(0.05F))
                                .add(LootItem.lootTableItem(PHItemRegistry.SINISTER_CARROT)))
                        .withPool(LootPool.lootPool()
                                                               .setRolls(ConstantValue.exactly(1))
                                .when(LootItemRandomChanceCondition.randomChance(0.15F))
                                .add(enchantItem(enchantments, Items.BOOK, PHEnchantments.BUBBLING, 1, 2))
                                .add(enchantItem(enchantments, Items.BOOK, PHEnchantments.AMPHIBIOUS, 1, 1))
                                .add(enchantItem(enchantments, Items.BOOK, PHEnchantments.VAMPIRE, 1, 2)))
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(1, 2))
                                .when(LootItemRandomChanceCondition.randomChance(0.5f))
                                .add(LootItem.lootTableItem(PHItemRegistry.COLLAR_TAG)
                                        .setWeight(1)
                                        .apply(EnchantRandomlyFunction.randomEnchantment()
                                                .withOneOf(enchantments.getOrThrow(PHTags.TradableEnchantmentKey))))
                                .add(LootItem.lootTableItem(PHItemRegistry.COLLAR_TAG).setWeight(2)))
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(5, 12))
                                .add(LootItem.lootTableItem(Items.TROPICAL_FISH)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 5))))
                                .add(LootItem.lootTableItem(Items.BONE)
                                        .setWeight(4)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                                .add(LootItem.lootTableItem(Items.LEAD)
                                        .setWeight(4)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                .add(LootItem.lootTableItem(Items.IRON_HORSE_ARMOR)
                                        .setWeight(2)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                .add(LootItem.lootTableItem(Items.AXOLOTL_BUCKET).setWeight(1))
                                .add(LootItem.lootTableItem(Items.TADPOLE_BUCKET).setWeight(2))
                                .add(LootItem.lootTableItem(Items.EMERALD)
                                        .setWeight(3)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                .add(LootItem.lootTableItem(Items.CARROT)
                                        .setWeight(4)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))));
    }

    /** 构造"物品 + 指定附魔（1~{@code maxLevel} 随机等级，固定附魔不随机降级）"的战利品条目。 */
    private LootPoolEntryContainer.Builder<?> enchantItem(
            HolderLookup.RegistryLookup<Enchantment> enchantments, Item item,
            ResourceKey<Enchantment> enchant, int weight, Integer maxLevel) {
        Holder<Enchantment> enchantment = enchantments.getOrThrow(enchant);
        return LootItem.lootTableItem(item)
                .setWeight(weight)
                .apply(new SetEnchantmentsFunction.Builder(true)
                        .withEnchantment(enchantment, UniformGenerator.between(1, maxLevel)));
    }
}
