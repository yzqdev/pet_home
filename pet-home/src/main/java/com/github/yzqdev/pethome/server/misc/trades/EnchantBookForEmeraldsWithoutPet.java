package com.github.yzqdev.pethome.server.misc.trades;

import com.github.yzqdev.pethome.server.enchantment.PetEnchantment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;
import java.util.stream.Collectors;

public   class EnchantBookForEmeraldsWithoutPet implements VillagerTrades.ItemListing {
        private final int villagerXp;

        public EnchantBookForEmeraldsWithoutPet(int pVillagerXp) {
            this.villagerXp = pVillagerXp;
        }

        public MerchantOffer getOffer(Entity pTrader, RandomSource pRandom) {
            List<Enchantment> list =  BuiltInRegistries.ENCHANTMENT.stream().filter(Enchantment::isTradeable).filter(i->!(i instanceof PetEnchantment)).collect(Collectors.toList());
            Enchantment enchantment = (Enchantment)list.get(pRandom.nextInt(list.size()));
            int i = Mth.nextInt(pRandom, enchantment.getMinLevel(), enchantment.getMaxLevel());
            ItemStack itemstack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, i));
            int j = 2 + pRandom.nextInt(5 + i * 10) + 3 * i;
            if (enchantment.isTreasureOnly()) {
                j *= 2;
            }

            if (j > 64) {
                j = 64;
            }

            return new MerchantOffer(new ItemStack(Items.EMERALD, j), new ItemStack(Items.BOOK), itemstack, 12, this.villagerXp, 0.2F);
        }


}