package com.github.yzqdev.pethome.datagen.loot;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
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


/**
 * 运行时战利品注入（NeoForge 的 9 个 {@code loot_modifiers} JSON 在 Fabric 无对应机制，
 * 改由 {@code LootTableEvents.MODIFY} 在每张表首次构建时重建）。{@code ENTRIES} 列出
 * 11 条「原版宝箱表 → 物品/附魔书 + 配置概率」；附魔书等级取
 * {@code UniformGenerator.between(1, maxLevel-1)}（不给满级）；被
 * {@code enchantments.<name>_enabled} 禁用的附魔不注入。
 */
public final class PetHomeLootInjector {

    private static final String MINECRAFT = "minecraft";

    /**
     * 「宝箱表 → 注入类型」映射，与 NeoForge 的 9 个 loot_modifier json 一一对应
     * （注释里给出对应的 json 文件名）。
     */
    private static final List<Entry> ENTRIES = List.of(
            new Entry("chests/woodland_mansion", PHLootType.SINISTER_CARROT),
            new Entry("chests/woodland_mansion", PHLootType.VAMPIRISM),
            new Entry("chests/buried_treasure", PHLootType.BUBBLING),
            new Entry("chests/end_city_treasure", PHLootType.SHARE),
            new Entry("chests/end_city_treasure", PHLootType.VOID_CLOUD),
            new Entry("chests/abandoned_mineshaft", PHLootType.ORE_SCENTING),
            new Entry("chests/ancient_city", PHLootType.SONIC_BOOM),
            new Entry("chests/nether_bridge", PHLootType.BLAZING_PROTECTION),
            new Entry("chests/abandoned_mineshaft", PHLootType.PARALYSIS),
            new Entry("chests/desert_pyramid", PHLootType.PARALYSIS),
            new Entry("chests/spawn_bonus_chest", PHLootType.PARALYSIS),
            new Entry("chests/abandoned_mineshaft", PHLootType.TOUGH),
            // muffled：远古城市箱子出安静附魔书（此前该侧配置了 muffled_loot_chance 却没接进来，配置形同虚设）
            new Entry("chests/ancient_city", PHLootType.MUFFLED)
    );

    private PetHomeLootInjector() {
    }

    /** 注册 {@code LootTableEvents.MODIFY} 回调（由 {@code PetHomeMod.onInitialize} 调用）。 */
    public static void init() {
        LootTableEvents.MODIFY.register(PetHomeLootInjector::modifyLootTable);
    }

    /** 只处理原版命名空间的表；命中 {@code ENTRIES} 的路径逐条 apply（同表可注入多条）。 */
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

    /** 按掉落类型分派：附魔书类走 {@link #addBook}，特殊物品类直接加物品池（概率读配置）。 */
    private static void apply(LootTable.Builder builder, HolderLookup.Provider registries, PHLootType type) {
        if (type.isEnchantedBook()) {
            addBook(builder, registries, type.enchantmentKey(), type.chance());
            return;
        }
        addItem(builder, PHItemRegistry.SINISTER_CARROT, type.chance());
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

    private record Entry(String table, PHLootType type) {
    }
}
