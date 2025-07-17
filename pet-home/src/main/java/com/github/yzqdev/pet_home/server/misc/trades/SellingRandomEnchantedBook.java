package com.github.yzqdev.pet_home.server.misc.trades;


import com.github.yzqdev.pet_home.datagen.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.Optional;

public class SellingRandomEnchantedBook implements VillagerTrades.ItemListing {
    private final int villagerXp;
    public SellingRandomEnchantedBook(int villagerXp) {
        this.villagerXp=villagerXp;
    }

    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        Optional<Holder<Enchantment>> optional = trader.level()
                .registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getRandomElementOf(ModTags.TradableEnchantmentKey, random);
        int i;
        ItemStack itemstack;
        if (!optional.isEmpty()) {
            Holder<Enchantment> holder = optional.get();
            Enchantment enchantment = holder.value();
            int j = Math.max(enchantment.getMinLevel(), 0);
            int k = Math.min(enchantment.getMaxLevel(), Integer.MAX_VALUE);
            int l = Mth.nextInt(random, j, k);
            itemstack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, l));
            i = 2 + random.nextInt(5 + l * 10) + 3 * l;
            if (holder.is(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
                i *= 2;
            }

            if (i > 64) {
                i = 64;
            }
        } else {
            i = 1;
            itemstack = new ItemStack(Items.BOOK);
        }

        return new MerchantOffer(new ItemCost(Items.EMERALD, i), Optional.of(new ItemCost(Items.BOOK)), itemstack, 12, villagerXp, 0.2F);
    }
}
