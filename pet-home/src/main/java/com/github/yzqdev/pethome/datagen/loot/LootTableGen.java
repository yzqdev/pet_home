package com.github.yzqdev.pethome.datagen.loot;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.datagen.PHTags;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/**
 * 宠物商店相关 chest 战利品表的 datagen 定义。 {@link #PET_LOOT_TABLE}（{@code
 * pet_home:chests/petshop_chest}）挂在村庄宠物商店箱子上： 低概率腐烂苹果 / 邪恶胡萝卜 /
 * 水泡·两栖·吸血附魔书、附魔项圈标签，外加鱼·骨·拴绳·桶装生物等杂货池； {@link #PET_PROTECTION_TABLE}（{@code
 * pet_home:chests/pet_protection_loot}）仅 5% 出一本坚韧附魔书， 本仓库代码与结构 NBT 均未引用该表（疑为遗留或对齐 NeoForge 侧保留）。
 */
public class LootTableGen {
  public static final ResourceKey<LootTable> PET_LOOT_TABLE =
      ResourceKey.create(
          Registries.LOOT_TABLE,
          ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "chests/petshop_chest"));
  public static final ResourceKey<LootTable> PET_PROTECTION_TABLE =
      ResourceKey.create(
          Registries.LOOT_TABLE,
          ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "chests/pet_protection_loot"));

  public record ChestLootTables(HolderLookup.Provider provider)
      implements net.minecraft.data.loot.LootTableSubProvider {
    private Holder<Enchantment> getEnchantment(ResourceKey<Enchantment> key) {
      return provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
      consumer.accept(
          PET_PROTECTION_TABLE,
          LootTable.lootTable()
              .withPool(
                  LootPool.lootPool()
                      .setRolls(ConstantValue.exactly(1))
                      .when(LootItemRandomChanceCondition.randomChance(0.05F))
                      .add(enchantItem(Items.BOOK, PHEnchantments.TOUGH, 1, 4))));

      consumer.accept(
          PET_LOOT_TABLE,
          LootTable.lootTable()
              .withPool(
                  LootPool.lootPool()
                      .setRolls(ConstantValue.exactly(1))
                      .when(LootItemRandomChanceCondition.randomChance(0.05F))
                      .add(LootItem.lootTableItem(PHItemRegistry.ROTTEN_APPLE)))
              .withPool(
                  LootPool.lootPool()
                      .setRolls(ConstantValue.exactly(1))
                      .when(LootItemRandomChanceCondition.randomChance(0.05F))
                      .add(LootItem.lootTableItem(PHItemRegistry.SINISTER_CARROT)))
              .withPool(
                  LootPool.lootPool()
                      .setRolls(ConstantValue.exactly(1))
                      .when(LootItemRandomChanceCondition.randomChance(0.15F))
                      .add(enchantItem(Items.BOOK, PHEnchantments.BUBBLING, 1, 2))
                      .add(enchantItem(Items.BOOK, PHEnchantments.AMPHIBIOUS, 1, 1))
                      .add(enchantItem(Items.BOOK, PHEnchantments.VAMPIRE, 1, 2)))
              .withPool(
                  LootPool.lootPool()
                      .setRolls(UniformGenerator.between(1, 2))
                      .when(LootItemRandomChanceCondition.randomChance(0.5f))
                      .add(
                          LootItem.lootTableItem(PHItemRegistry.COLLAR_TAG)
                              .setWeight(1)
                              .apply(
                                  EnchantRandomlyFunction.randomEnchantment()
                                      .withOneOf(
                                          provider
                                              .lookupOrThrow(Registries.ENCHANTMENT)
                                              .getOrThrow(PHTags.TradableEnchantmentKey))))
                      .add(LootItem.lootTableItem(PHItemRegistry.COLLAR_TAG).setWeight(2)))
              .withPool(
                  LootPool.lootPool()
                      .setRolls(UniformGenerator.between(5, 12))
                      .add(
                          LootItem.lootTableItem(Items.TROPICAL_FISH)
                              .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 5))))
                      .add(
                          LootItem.lootTableItem(Items.BONE)
                              .setWeight(4)
                              .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                      .add(
                          LootItem.lootTableItem(Items.LEAD)
                              .setWeight(4)
                              .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                      .add(
                          LootItem.lootTableItem(Items.IRON_HORSE_ARMOR)
                              .setWeight(2)
                              .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                      .add(LootItem.lootTableItem(Items.AXOLOTL_BUCKET).setWeight(1))
                      .add(LootItem.lootTableItem(Items.TADPOLE_BUCKET).setWeight(2))
                      .add(
                          LootItem.lootTableItem(Items.EMERALD)
                              .setWeight(3)
                              .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                      .add(
                          LootItem.lootTableItem(Items.CARROT)
                              .setWeight(4)
                              .apply(
                                  SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))));
    }

    private LootPoolEntryContainer.Builder<?> enchantItem(
        Item item, ResourceKey<Enchantment> enchant, int weight, Integer maxLevel) {
      var enchantment = getEnchantment(enchant);
      return LootItem.lootTableItem(item)
          .setWeight(weight)
          .apply(
              new SetEnchantmentsFunction.Builder(true)
                  .withEnchantment(enchantment, UniformGenerator.between(1, maxLevel)));
    }
  }
}
