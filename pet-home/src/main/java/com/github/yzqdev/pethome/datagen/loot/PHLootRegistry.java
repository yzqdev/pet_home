package com.github.yzqdev.pethome.datagen.loot;

import com.github.yzqdev.pethome.PetHomeConfig;
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

/**
 * 运行时战利品注入（区别于 {@link PHLootProvider} 的 datagen 产物）： 经 {@code LootTableEvents.MODIFY} 在原版战利品表加载后追加
 * LootPool，把 {@link PHLootType} 列出的掉落（阴森胡萝卜 + 10 本可掉落的附魔书）按概率塞进对应结构箱子 （林地府邸 / 沙藏宝藏 / 末地城 /
 * 废弃矿井 / 远古城市 / 下界要塞 / 沙漠神殿 / 初始箱）。 注入的是内存中的表，不修改任何 JSON；概率与附魔开关全部实时读 {@link PetHomeConfig}。
 */
public class PHLootRegistry {

  /** 挂 MODIFY 事件；只处理原版内建表（{@code source.isBuiltin()} 时跳过，避免与 datagen 的模组表重复触发）。 */
  public static void init() {
    LootTableEvents.MODIFY.register(
        (key, tableBuilder, source, registries) -> {
          if (source.isBuiltin()) {
            return;
          }
          for (PHLootType type : lootTypesFor(key)) {
            applyLootType(tableBuilder, registries, type);
          }
        });
  }

  /** 战利品表 → 掉落类型的映射；未列出的表返回空数组表示不注入。 */
  private static PHLootType[] lootTypesFor(ResourceKey<LootTable> key) {
    if (key.location().equals(BuiltInLootTables.WOODLAND_MANSION.location())) {
      return new PHLootType[] {PHLootType.SINISTER_CARROT, PHLootType.VAMPIRISM};
    }
    if (key.location().equals(BuiltInLootTables.BURIED_TREASURE.location())) {
      return new PHLootType[] {PHLootType.BUBBLING};
    }
    if (key.location().equals(BuiltInLootTables.END_CITY_TREASURE.location())) {
      return new PHLootType[] {PHLootType.SHARE, PHLootType.VOID_CLOUD};
    }
    if (key.location().equals(BuiltInLootTables.ABANDONED_MINESHAFT.location())) {
      return new PHLootType[] {
        PHLootType.ORE_SCENTING, PHLootType.PARALYSIS, PHLootType.TOUGH
      };
    }
    if (key.location().equals(BuiltInLootTables.ANCIENT_CITY.location())) {
      // muffled：此前该侧配置了 muffled_loot_chance 却没接进任何战利品表（配置形同虚设），这里补上
      return new PHLootType[] {PHLootType.SONIC_BOOM, PHLootType.MUFFLED};
    }
    if (key.location().equals(BuiltInLootTables.NETHER_BRIDGE.location())) {
      return new PHLootType[] {PHLootType.BLAZING_PROTECTION};
    }
    if (key.location().equals(BuiltInLootTables.DESERT_PYRAMID.location())
        || key.location().equals(BuiltInLootTables.SPAWN_BONUS_CHEST.location())) {
      return new PHLootType[] {PHLootType.PARALYSIS};
    }
    return new PHLootType[0];
  }

  /** 按掉落类型分派：附魔书类走 {@link #addEnchantedBook}，特殊物品类（阴森胡萝卜）加 1~2 个。 */
  private static void applyLootType(
      LootTable.Builder tableBuilder, HolderLookup.Provider registries, PHLootType type) {
    if (type.isEnchantedBook()) {
      addEnchantedBook(tableBuilder, registries, type.enchantmentKey(), type.chance());
      return;
    }
    double chance = type.chance();
    if (chance <= 0) {
      return;
    }
    tableBuilder.withPool(
        LootPool.lootPool()
            .when(LootItemRandomChanceCondition.randomChance((float) chance))
            .setRolls(ConstantValue.exactly(1))
            .add(
                LootItem.lootTableItem(PHItemRegistry.SINISTER_CARROT)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))));
  }

  /** 往表里加一本目标附魔书；附魔被配置禁用时静默跳过（书仍不掉落），等级在 1 到最高等级-1 间随机（单级附魔恒为 1）。 */
  private static void addEnchantedBook(
      LootTable.Builder tableBuilder,
      HolderLookup.Provider registries,
      ResourceKey<Enchantment> enchantmentKey,
      double chance) {
    // 被配置禁用的附魔不再出现在战利品里
    if (!PetHomeConfig.isEnchantEnabled(enchantmentKey)) {
      return;
    }
    var reg = registries.lookupOrThrow(Registries.ENCHANTMENT);
    var enchantHolder = reg.get(enchantmentKey).orElseThrow();
    int maxLevels = enchantHolder.value().getMaxLevel();
    SetEnchantmentsFunction.Builder function =
        new SetEnchantmentsFunction.Builder()
            .withEnchantment(
                enchantHolder,
                maxLevels > 1
                    ? UniformGenerator.between(1.0F, maxLevels - 1.0F)
                    : ConstantValue.exactly(1));
    tableBuilder.withPool(
        LootPool.lootPool()
            .when(LootItemRandomChanceCondition.randomChance((float) chance))
            .setRolls(ConstantValue.exactly(1))
            .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK).apply(function)));
  }
}
