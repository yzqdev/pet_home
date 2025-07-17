package com.github.yzqdev.pet_home.server.misc.trades;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;

public class SellingOneOfTheseItemsTrade implements VillagerTrades.ItemListing {
    private final ImmutableSet<ItemLike> sellingItems;
    private final int emeraldCount;
    private final int sellingItemCount;
    private final int maxUses;
    private final int xpValue;
    private final float priceMultiplier;


    public SellingOneOfTheseItemsTrade(ImmutableSet<ItemLike> sellingItems, int emeraldCount, int sellingItemCount, int maxUses, int xpValue) {
        this(sellingItems, emeraldCount, sellingItemCount, maxUses, xpValue, 0.05F);
    }

    public SellingOneOfTheseItemsTrade(ImmutableSet<ItemLike> sellingItems, int emeraldCount, int sellingItemCount, int maxUses, int xpValue, float priceMultiplier) {
        this.sellingItems = sellingItems;
        this.emeraldCount = emeraldCount;
        this.sellingItemCount = sellingItemCount;
        this.maxUses = maxUses;
        this.xpValue = xpValue;
        this.priceMultiplier = priceMultiplier;
    }

    public MerchantOffer getOffer(Entity trader, RandomSource rand) {
        return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldCount), new ItemStack(this.sellingItems.asList().get(rand.nextInt(sellingItems.size() - 1)).asItem(), this.sellingItemCount), this.maxUses, this.xpValue, this.priceMultiplier);
    }
}
