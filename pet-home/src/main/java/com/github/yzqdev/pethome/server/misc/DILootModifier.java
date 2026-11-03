package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import java.util.List;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.resources.ResourceLocation;
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
 * 战利品注入（替代 NeoForge 的 loot_modifiers JSON，Fabric 用 {@code LootTableEvents.MODIFY}
 * 运行时向原版结构战利品表追加 LootPool）：结构 → 掉落类型（见 {@link PHLootType}）+ 配置概率。 由 {@code onInitialize} 调用
 * {@link #init()} 注册，每次战利品表构建时实时读配置。
 */
public class DILootModifier {

    /**
     * 「原版宝箱表 → 掉落类型」映射，与 NeoForge 侧的各 loot_modifier 一一对应； 原先是一长串 {@code if/else if}，改为
     * 表驱动后与其它侧同构，新增掉落只加一行。
     */
    private static final List<Entry> ENTRIES = List.of(
            new Entry(BuiltInLootTables.WOODLAND_MANSION, PHLootType.SINISTER_CARROT),
            new Entry(BuiltInLootTables.WOODLAND_MANSION, PHLootType.VAMPIRISM),
            new Entry(BuiltInLootTables.BURIED_TREASURE, PHLootType.BUBBLING),
            // VOID_CLOUD（虚空之云）/ SHARE（平摊）：末地城
            new Entry(BuiltInLootTables.END_CITY_TREASURE, PHLootType.VOID_CLOUD),
            new Entry(BuiltInLootTables.END_CITY_TREASURE, PHLootType.SHARE),
            new Entry(BuiltInLootTables.ABANDONED_MINESHAFT, PHLootType.ORE_SCENTING),
            // TOUGH（稳固）/ PARALYSIS（麻痹）：废弃矿井
            new Entry(BuiltInLootTables.ABANDONED_MINESHAFT, PHLootType.TOUGH),
            new Entry(BuiltInLootTables.ABANDONED_MINESHAFT, PHLootType.PARALYSIS),
            // MUFFLED（安静）/ SONIC_BOOM（冲击波）：远古城市
            new Entry(BuiltInLootTables.ANCIENT_CITY, PHLootType.MUFFLED),
            new Entry(BuiltInLootTables.ANCIENT_CITY, PHLootType.SONIC_BOOM),
            new Entry(BuiltInLootTables.NETHER_BRIDGE, PHLootType.BLAZING_PROTECTION),
            // PARALYSIS（麻痹）：沙漠神殿 / 初始宝箱
            new Entry(BuiltInLootTables.DESERT_PYRAMID, PHLootType.PARALYSIS),
            new Entry(BuiltInLootTables.SPAWN_BONUS_CHEST, PHLootType.PARALYSIS));

    private DILootModifier() {
    }

    /** 注册 LootTableEvents.MODIFY 回调：林地府邸 / 埋藏宝藏 / 末地城 / 废弃矿井 / 远古城市 / 下界要塞 / 沙漠神殿与初始宝箱。 */
    public static void init() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            for (Entry entry : ENTRIES) {
                if (id.equals(entry.table())) {
                    apply(tableBuilder, entry.type());
                }
            }
        });
    }

    /** 按掉落类型分派：附魔书类走 {@link #addEnchantedBook}，特殊物品类（阴森胡萝卜）加 1~2 个。 */
    private static void apply(LootTable.Builder tableBuilder, PHLootType type) {
        if (type.isEnchantedBook()) {
            addEnchantedBook(tableBuilder, type.enchantment(), (float) type.chance());
            return;
        }
        tableBuilder.withPool(LootPool.lootPool()
                .when(LootItemRandomChanceCondition.randomChance((float) type.chance()))
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(PHItemRegistry.SINISTER_CARROT)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))));
    }

    /** 追加一个附魔书池：按 chance 概率出现 1 本；多级附魔等级在 1~maxLevel 均匀分布（含满级）。 */
    private static void addEnchantedBook(LootTable.Builder tableBuilder, Enchantment enchantment, float chance) {
        // 保持与本侧原有行为一致：由 PetHomeConfig 的附魔开关在别处统一过滤，此处不额外判 isEnchantEnabled
        int maxLevels = enchantment.getMaxLevel();
        SetEnchantmentsFunction.Builder function = new SetEnchantmentsFunction.Builder().withEnchantment(enchantment,
                maxLevels > 1 ? UniformGenerator.between(1.0F, maxLevels) : ConstantValue.exactly(1));
        tableBuilder.withPool(LootPool.lootPool()
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK).apply(function)));
    }

    private record Entry(ResourceLocation table, PHLootType type) {
    }
}
