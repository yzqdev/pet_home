package com.github.yzqdev.pethome.datagen.loot;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.ModEnchantments;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;


public final class PetHomeLootInjector {

    private static final String MINECRAFT = "minecraft";

    /**
     * 「宝箱表 → 注入类型」映射，与 NeoForge 的 9 个 loot_modifier json 一一对应
     * （注释里给出对应的 json 文件名）。
     */
    private static final List<Entry> ENTRIES = List.of(
            new Entry("chests/woodland_mansion", Type.SINISTER_CARROT),
            new Entry("chests/woodland_mansion", Type.VAMPIRISM),
            new Entry("chests/buried_treasure", Type.BUBBLING),
            new Entry("chests/end_city_treasure", Type.SHARE),
            new Entry("chests/abandoned_mineshaft", Type.ORE_SCENTING),
            new Entry("chests/ancient_city", Type.SONIC_BOOM),
            new Entry("chests/nether_bridge", Type.BLAZING_PROTECTION),
            new Entry("chests/abandoned_mineshaft", Type.PARALYSIS),
            new Entry("chests/desert_pyramid", Type.PARALYSIS),
            new Entry("chests/spawn_bonus_chest", Type.PARALYSIS),
            new Entry("chests/abandoned_mineshaft", Type.TOUGH)
    );

    private PetHomeLootInjector() {
    }

    public static void init() {
        LootTableEvents.MODIFY.register(PetHomeLootInjector::modifyLootTable);
    }

    private static void modifyLootTable(ResourceKey<LootTable> key,
                                        LootTable.Builder builder,
                                        LootTableSource source,
                                        HolderLookup.Provider registries) {
        Identifier id = key.identifier();
        if (!MINECRAFT.equals(id.getNamespace())) {
            return;
        }
        String path = id.getPath();
        for (Entry entry : ENTRIES) {
            if (entry.table().equals(path)) {
                apply(builder, registries, entry.type());
            }
        }
    }

    private static void apply(LootTable.Builder builder, HolderLookup.Provider registries, Type type) {
        switch (type) {
            case SINISTER_CARROT -> addItem(builder, PHItemRegistry.SINISTER_CARROT,
                    PetHomeConfig.sinisterCarrotLootChance);
            case BUBBLING -> addBook(builder, registries, ModEnchantments.BUBBLING,
                    PetHomeConfig.bubblingLootChance);
            case VAMPIRISM -> addBook(builder, registries, ModEnchantments.VAMPIRE,
                    PetHomeConfig.vampirismLootChance);
            case SHARE -> addBook(builder, registries, ModEnchantments.SHARE,
                    PetHomeConfig.shareLootChance);
            case ORE_SCENTING -> addBook(builder, registries, ModEnchantments.ORE_SCENTING,
                    PetHomeConfig.oreScentingLootChance);
            case SONIC_BOOM -> addBook(builder, registries, ModEnchantments.SonicBoom,
                    PetHomeConfig.sonicBoomLootChance);
            case BLAZING_PROTECTION -> addBook(builder, registries, ModEnchantments.BLAZING_PROTECTION,
                    PetHomeConfig.blazingProtectionLootChance);
            case PARALYSIS -> addBook(builder, registries, ModEnchantments.PARALYSIS,
                    PetHomeConfig.paralysisLootChance);
            case TOUGH -> addBook(builder, registries, ModEnchantments.TOUGH,
                    PetHomeConfig.toughLootChance);
        }
    }

    /** 按概率往表里加一个物品（独立一次 roll，对应一个 loot_modifier） */
    private static void addItem(LootTable.Builder builder, Item item, double chance) {
        LootPool.Builder pool = LootPool.lootPool()
                .when(LootItemRandomChanceCondition.randomChance(ConstantValue.exactly((float) chance)))
                .add(LootItem.lootTableItem(item));
        builder.pool(pool.build());
    }

    /** 按概率往表里加一本「指定附魔、等级随机」的附魔书 */
    private static void addBook(LootTable.Builder builder, HolderLookup.Provider registries,
                                ResourceKey<Enchantment> enchantmentKey, double chance) {
        // 被配置禁用的附魔不再出现在战利品里
        if (!PetHomeConfig.isEnchantEnabled(enchantmentKey)) {
            return;
        }
        HolderLookup.RegistryLookup<Enchantment> lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> enchantment = lookup.get(enchantmentKey).orElse(null);
        if (enchantment == null) {
            // 附魔未加载（例如数据包被移除）时跳过，避免生成空附魔书
            return;
        }
        LootItemFunction.Builder enchant = new SetEnchantmentsFunction.Builder()
                .withEnchantment(enchantment, randomLevel(enchantment.value().getMaxLevel()));
        LootPool.Builder pool = LootPool.lootPool()
                .when(LootItemRandomChanceCondition.randomChance(ConstantValue.exactly((float) chance)))
                .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK).apply(enchant));
        builder.pool(pool.build());
    }


    private static NumberProvider randomLevel(int maxLevel) {
        return maxLevel > 1
                ? UniformGenerator.between(1.0F, (float) (maxLevel - 1))
                : ConstantValue.exactly(1.0F);
    }

    private record Entry(String table, Type type) {
    }

    private enum Type {
        SINISTER_CARROT,
        BUBBLING,
        VAMPIRISM,
        SHARE,
        ORE_SCENTING,
        SONIC_BOOM,
        BLAZING_PROTECTION,
        PARALYSIS,
        TOUGH
    }
}
