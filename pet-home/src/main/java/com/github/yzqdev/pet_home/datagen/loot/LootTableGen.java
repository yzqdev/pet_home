package com.github.yzqdev.pet_home.datagen.loot;

import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.datagen.ModEnchantments;
import com.github.yzqdev.pet_home.datagen.ModTags;
import com.github.yzqdev.pet_home.server.item.PHItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
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

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class LootTableGen {
    public static final ResourceKey<LootTable> PET_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "chests/petshop_chest"));
    public static final ResourceKey<LootTable> PET_PROTECTION_TABLE = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "chests/pet_protection_loot"));


    public record ChestLootTables(HolderLookup.Provider provider) implements LootTableSubProvider {
        private Holder<Enchantment> getEnchantment(ResourceKey<Enchantment> key) {
            return provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
        }

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
            consumer.accept(PET_PROTECTION_TABLE, LootTable.lootTable().withPool(LootPool.lootPool()
                    .name("enchant")
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.05F))

                    .add(enchantItem(Items.BOOK, ModEnchantments.TOUGH, 1, 4))

            ));

            consumer.accept(PET_LOOT_TABLE, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .name("rotten_apple")
                            .setRolls(ConstantValue.exactly(1))
                            .when(LootItemRandomChanceCondition.randomChance(0.05F))
                            .add(LootItem.lootTableItem(PHItemRegistry.ROTTEN_APPLE.get())))
                    .withPool(LootPool.lootPool()
                            .name("sinister_carrot")
                            .setRolls(ConstantValue.exactly(1))
                            .when(LootItemRandomChanceCondition.randomChance(0.05F))
                            .add(LootItem.lootTableItem(PHItemRegistry.SINISTER_CARROT.get())))
                    .withPool(LootPool.lootPool()
                            .name("enchant")
                            .setRolls(ConstantValue.exactly(1))
                            .when(LootItemRandomChanceCondition.randomChance(0.15F))

                            .add(enchantItem(Items.BOOK, ModEnchantments.BUBBLING, 1, 2))
                            .add(enchantItem(Items.BOOK, ModEnchantments.AMPHIBIOUS, 1, 1))
                            .add(enchantItem(Items.BOOK, ModEnchantments.VAMPIRE, 1, 2))

                    ).withPool(LootPool.lootPool()
                            .name("petshop_chest_collars")
                            .setRolls(UniformGenerator.between(1, 2))
                            .when(LootItemRandomChanceCondition.randomChance(0.5f))
                            .add(LootItem.lootTableItem(PHItemRegistry.COLLAR_TAG.get()).setWeight(1).apply(EnchantRandomlyFunction.randomEnchantment().withOneOf(provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ModTags.TradableEnchantmentKey))))
                            .add(LootItem.lootTableItem(PHItemRegistry.COLLAR_TAG.get()).setWeight(2))

                    )
                    .withPool(LootPool.lootPool()
                            .name("petshop_chest")
                            .setRolls(UniformGenerator.between(5, 12))
                            .add(LootItem.lootTableItem(Items.TROPICAL_FISH).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 5))))
                            .add(LootItem.lootTableItem(Items.BONE).setWeight(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                            .add(LootItem.lootTableItem(Items.LEAD).setWeight(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))).add(LootItem.lootTableItem(Items.IRON_HORSE_ARMOR).setWeight(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                            .add(LootItem.lootTableItem(Items.AXOLOTL_BUCKET).setWeight(1)).add(LootItem.lootTableItem(Items.TADPOLE_BUCKET).setWeight(2)).add(LootItem.lootTableItem(Items.EMERALD).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                            .add(LootItem.lootTableItem(Items.CARROT).setWeight(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))


                    ));

        }

        private LootPoolEntryContainer.Builder<?> enchantItem(Item item, ResourceKey<Enchantment> enchant, int weight, Integer maxLevel) {
            var enchantment = getEnchantment(enchant);
            return LootItem.lootTableItem(item).setWeight(weight).apply(new SetEnchantmentsFunction.Builder(true).withEnchantment(enchantment, UniformGenerator.between(1, maxLevel)));
        }
    }


    private ItemStack enchantedBook(ResourceKey<Enchantment> enchantmentKey, RandomSource randomSource, LootContext context) {

        var reg = context.getLevel()
                .registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT);
        var enchant = reg.get(enchantmentKey);

        int maxLevels = enchant.getMaxLevel();
        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(reg.wrapAsHolder(enchant), maxLevels > 1 ? 1 + randomSource.nextInt(maxLevels - 1) : 1));

    }

    public static class BlockLootTables extends BlockLootSubProvider {
        public final Set<Block> knownBlocks = new HashSet<>();

        public BlockLootTables(HolderLookup.Provider provider) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
        }

        @Override
        public void generate() {


        }

        @Override
        public void add(Block block, LootTable.Builder builder) {
            this.knownBlocks.add(block);
            super.add(block, builder);
        }

        @Override
        public Iterable<Block> getKnownBlocks() {
            return this.knownBlocks;
        }
    }
}
