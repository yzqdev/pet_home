package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PHVillagerTrade {

    // --- VillagerTrade keys ---
    public static final ResourceKey<VillagerTrade> TAMER_1_BONE = createKey("tamer/1/bone");
    public static final ResourceKey<VillagerTrade> TAMER_1_EGG = createKey("tamer/1/egg");
    public static final ResourceKey<VillagerTrade> TAMER_1_COD = createKey("tamer/1/cod");
    public static final ResourceKey<VillagerTrade> TAMER_1_BOOK = createKey("tamer/1/book");
    public static final ResourceKey<VillagerTrade> TAMER_2_TROPICAL_FISH_BUCKET = createKey("tamer/2/tropical_fish_bucket");
    public static final ResourceKey<VillagerTrade> TAMER_2_BUY_CARROT = createKey("tamer/2/buy_carrot");
    public static final ResourceKey<VillagerTrade> TAMER_2_BUY_STICK = createKey("tamer/2/buy_stick");
    public static final ResourceKey<VillagerTrade> TAMER_2_APPLE = createKey("tamer/2/apple");
    public static final ResourceKey<VillagerTrade> TAMER_2_BOOK = createKey("tamer/2/book");
    public static final ResourceKey<VillagerTrade> TAMER_3_ROTTEN_APPLE = createKey("tamer/3/rotten_apple");
    public static final ResourceKey<VillagerTrade> TAMER_3_TADPOLE_BUCKET = createKey("tamer/3/tadpole_bucket");
    public static final ResourceKey<VillagerTrade> TAMER_3_COLLAR_TAG = createKey("tamer/3/collar_tag");
    public static final ResourceKey<VillagerTrade> TAMER_3_ENCHANTED_COLLAR = createKey("tamer/3/enchanted_collar");
    public static final ResourceKey<VillagerTrade> TAMER_3_BOOK = createKey("tamer/3/book");
    public static final ResourceKey<VillagerTrade> TAMER_4_AXOLOTL_BUCKET = createKey("tamer/4/axolotl_bucket");
    public static final ResourceKey<VillagerTrade> TAMER_4_TURTLE_EGG = createKey("tamer/4/turtle_egg");
    public static final ResourceKey<VillagerTrade> TAMER_4_ENCHANTED_COLLAR = createKey("tamer/4/enchanted_collar");
    public static final ResourceKey<VillagerTrade> TAMER_4_BOOK = createKey("tamer/4/book");
    public static final ResourceKey<VillagerTrade> TAMER_5_GOLDEN_CARROT = createKey("tamer/5/golden_carrot");
    public static final ResourceKey<VillagerTrade> TAMER_5_GOLDEN_APPLE = createKey("tamer/5/golden_apple");
    public static final ResourceKey<VillagerTrade> TAMER_5_COLLAR_TAG = createKey("tamer/5/collar_tag");
    public static final ResourceKey<VillagerTrade> TAMER_5_ENCHANTED_COLLAR = createKey("tamer/5/enchanted_collar");
    public static final ResourceKey<VillagerTrade> TAMER_5_BOOK = createKey("tamer/5/book");

    // --- TradeSet keys ---
    public static final ResourceKey<TradeSet> TAMER_TRADE_SET_LEVEL_1 = createTradeSetKey("tamer/level_1");
    public static final ResourceKey<TradeSet> TAMER_TRADE_SET_LEVEL_2 = createTradeSetKey("tamer/level_2");
    public static final ResourceKey<TradeSet> TAMER_TRADE_SET_LEVEL_3 = createTradeSetKey("tamer/level_3");
    public static final ResourceKey<TradeSet> TAMER_TRADE_SET_LEVEL_4 = createTradeSetKey("tamer/level_4");
    public static final ResourceKey<TradeSet> TAMER_TRADE_SET_LEVEL_5 = createTradeSetKey("tamer/level_5");

    // --- villager_trade tags（每个等级一个，TradeSet 通过 tag 引用交易） ---
    public static final TagKey<VillagerTrade> TAMER_LVL1 = createTagKey("tamer/level_1");
    public static final TagKey<VillagerTrade> TAMER_LVL2 = createTagKey("tamer/level_2");
    public static final TagKey<VillagerTrade> TAMER_LVL3 = createTagKey("tamer/level_3");
    public static final TagKey<VillagerTrade> TAMER_LVL4 = createTagKey("tamer/level_4");
    public static final TagKey<VillagerTrade> TAMER_LVL5 = createTagKey("tamer/level_5");

    public static ResourceKey<VillagerTrade> createKey(String name) {
        return ResourceKey.create(Registries.VILLAGER_TRADE, Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name));
    }

    public static ResourceKey<TradeSet> createTradeSetKey(String name) {
        return ResourceKey.create(Registries.TRADE_SET, Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name));
    }

    public static TagKey<VillagerTrade> createTagKey(String name) {
        return TagKey.create(Registries.VILLAGER_TRADE, Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name));
    }

    public static void bootstrapVillagerTrade(BootstrapContext<VillagerTrade> context) {
        // 1.20/1.21 侧 SellingItemTrade：绿宝石价格 → N 件商品；默认 maxUses 12、priceMultiplier 0.05
        sell(context, TAMER_1_BONE, 3, Items.BONE, 6, 6, 1);
        sell(context, TAMER_1_EGG, 3, Items.EGG, 6, 6, 1);
        sell(context, TAMER_1_COD, 2, Items.COD, 6, 12, 1);
        sellBook(context, TAMER_1_BOOK, 5, 12, 1);

        sell(context, TAMER_2_TROPICAL_FISH_BUCKET, 2, Items.TROPICAL_FISH_BUCKET, 1, 6, 7);
        buy(context, TAMER_2_BUY_CARROT, Items.CARROT, 5, 1, 12, 7);
        buy(context, TAMER_2_BUY_STICK, Items.STICK, 20, 1, 12, 6);
        sell(context, TAMER_2_APPLE, 4, Items.APPLE, 12, 3, 7);
        sellBook(context, TAMER_2_BOOK, 8, 12, 2);

        sell(context, TAMER_3_ROTTEN_APPLE, 4, PHItemRegistry.ROTTEN_APPLE, 1, 1, 10);
        sellBook(context, TAMER_3_BOOK, 12, 12, 3);
        sell(context, TAMER_3_TADPOLE_BUCKET, 6, Items.TADPOLE_BUCKET, 1, 4, 13);
        sell(context, TAMER_3_COLLAR_TAG, 4, PHItemRegistry.COLLAR_TAG, 1, 6, 6);
        sellEnchantedCollar(context, TAMER_3_ENCHANTED_COLLAR, 8, 2, 3, 10);

        sell(context, TAMER_4_AXOLOTL_BUCKET, 11, Items.AXOLOTL_BUCKET, 1, 2, 15);
        sell(context, TAMER_4_TURTLE_EGG, 26, Items.TURTLE_EGG, 1, 2, 15);
        sellBook(context, TAMER_4_BOOK, 15, 12, 3);
        sellEnchantedCollar(context, TAMER_4_ENCHANTED_COLLAR, 18, 3, 3, 15);

        sell(context, TAMER_5_GOLDEN_CARROT, 6, Items.GOLDEN_CARROT, 1, 6, 10);
        sell(context, TAMER_5_GOLDEN_APPLE, 10, Items.GOLDEN_APPLE, 1, 6, 10);
        sell(context, TAMER_5_COLLAR_TAG, 6, PHItemRegistry.COLLAR_TAG, 1, 6, 10);
        sellEnchantedCollar(context, TAMER_5_ENCHANTED_COLLAR, 38, 4, 3, 20);
        sellBook(context, TAMER_5_BOOK, 15, 12, 3);
    }

    public static void bootstrapTradeSet(BootstrapContext<TradeSet> context) {
        registerTradeSet(context, TAMER_TRADE_SET_LEVEL_1, TAMER_LVL1);
        registerTradeSet(context, TAMER_TRADE_SET_LEVEL_2, TAMER_LVL2);
        registerTradeSet(context, TAMER_TRADE_SET_LEVEL_3, TAMER_LVL3);
        registerTradeSet(context, TAMER_TRADE_SET_LEVEL_4, TAMER_LVL4);
        registerTradeSet(context, TAMER_TRADE_SET_LEVEL_5, TAMER_LVL5);
    }

    private static void registerTradeSet(BootstrapContext<TradeSet> context, ResourceKey<TradeSet> key, TagKey<VillagerTrade> tradeTag) {
        NumberProvider amount = ConstantValue.exactly(2.0F);
        context.register(key, new TradeSet(
                context.lookup(Registries.VILLAGER_TRADE).getOrThrow(tradeTag),
                amount,
                false,
                Optional.of(key.identifier().withPrefix("trade_set/"))
        ));
    }

    private static void sell(BootstrapContext<VillagerTrade> context, ResourceKey<VillagerTrade> key,
                             int emeralds, Item item, int count, int maxUses, int xp) {
        context.register(key, new VillagerTrade(
                new TradeCost(Items.EMERALD, emeralds),
                Optional.empty(),
                new ItemStackTemplate(item, count),
                maxUses,
                xp,
                0.05F,
                Optional.empty(),
                List.of(),
                Optional.empty()
        ));
    }

    private static void buy(BootstrapContext<VillagerTrade> context, ResourceKey<VillagerTrade> key,
                            Item item, int count, int emeralds, int maxUses, int xp) {
        context.register(key, new VillagerTrade(
                new TradeCost(item, count),
                Optional.empty(),
                new ItemStackTemplate(Items.EMERALD, emeralds),
                maxUses,
                xp,
                0.05F,
                Optional.empty(),
                List.of(),
                Optional.empty()
        ));
    }

    // 随机宠物附魔书：对齐原版图书匠 "enchant_randomly + filtered" 的写法，附魔池为驯兽师可售附魔 tag
    private static void sellBook(BootstrapContext<VillagerTrade> context, ResourceKey<VillagerTrade> key,
                                 int emeralds, int maxUses, int xp) {
        List<LootItemFunction> modifiers = new ArrayList<>();
        modifiers.add(EnchantRandomlyFunction.randomEnchantment()
                .withOneOf(context.lookup(Registries.ENCHANTMENT).getOrThrow(ModTags.TradableEnchantmentKey))
                .allowingIncompatibleEnchantments()
                .build());
        context.register(key, new VillagerTrade(
                new TradeCost(Items.EMERALD, emeralds),
                Optional.empty(),
                new ItemStackTemplate(Items.ENCHANTED_BOOK, 1),
                maxUses,
                xp,
                0.05F,
                Optional.empty(),
                modifiers,
                Optional.empty()
        ));
    }

    // 附魔项圈：给项圈标签随机附上 N 条宠物附魔（对齐 1.20 EnchantItemTrade 的 enchantmentCount）
    private static void sellEnchantedCollar(BootstrapContext<VillagerTrade> context, ResourceKey<VillagerTrade> key,
                                            int emeralds, int enchantmentCount, int maxUses, int xp) {
        List<LootItemFunction> modifiers = new ArrayList<>();
        for (int i = 0; i < enchantmentCount; i++) {
            modifiers.add(EnchantRandomlyFunction.randomEnchantment()
                    .withOneOf(context.lookup(Registries.ENCHANTMENT).getOrThrow(ModTags.TradableEnchantmentKey))
                    .allowingIncompatibleEnchantments()
                    .build());
        }
        context.register(key, new VillagerTrade(
                new TradeCost(Items.EMERALD, emeralds),
                Optional.empty(),
                new ItemStackTemplate(PHItemRegistry.COLLAR_TAG, 1),
                maxUses,
                xp,
                0.05F,
                Optional.empty(),
                modifiers,
                Optional.empty()
        ));
    }
}
