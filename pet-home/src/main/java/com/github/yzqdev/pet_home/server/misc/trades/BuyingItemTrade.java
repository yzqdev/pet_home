package com.github.yzqdev.pet_home.server.misc.trades;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;

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

    public MerchantOffer getOffer(Entity entity, RandomSource rng) {

        return new MerchantOffer(new ItemCost(this.tradeItem,this.itemCount), new ItemStack(Items.EMERALD, this.emeralds), this.maxUses, this.xpValue, this.priceMultiplier);
    }
}