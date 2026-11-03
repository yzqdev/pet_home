package com.github.yzqdev.pethome.server.misc.trades;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;

import java.util.Random;

/**
 * 收购交易：村民按固定数量收购指定物品，付绿宝石（驯兽师收购类交易的基础实现）。
 */
public class BuyingItemTrade implements VillagerTrades.ItemListing {
    private final Item tradeItem;
    private final int itemCount;
    private final int emeralds;
    private final int maxUses;
    private final int xpValue;
    private final float priceMultiplier;

    public BuyingItemTrade(ItemLike item, int itemCount, int emeralds, int maxUses, int xp) {
        this.tradeItem = item.asItem();
        this.itemCount = itemCount;
        this.emeralds = emeralds;
        this.maxUses = maxUses;
        this.xpValue = xp;
        this.priceMultiplier = 0.05F;
    }

    /** 每次调用生成固定报价（无随机成分）。 */
    public MerchantOffer getOffer(Entity entity, RandomSource rng) {
        ItemStack buyingItem = new ItemStack(this.tradeItem, this.itemCount);
        return new MerchantOffer(buyingItem, new ItemStack(Items.EMERALD, this.emeralds), this.maxUses, this.xpValue, this.priceMultiplier);
    }
}