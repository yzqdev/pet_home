package com.github.yzqdev.pethome.server.misc.trades;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.enchantment.PetEnchantment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SellingRandomEnchantedBook implements VillagerTrades.ItemListing {



    public MerchantOffer getOffer(Entity trader, RandomSource pRandom) {
        List<PetEnchantment> list=new ArrayList<>();
        try {
            for (Field f : DIEnchantmentRegistry.class.getDeclaredFields()) {
                Object obj = null;
                obj = f.get(null);
                if (obj instanceof PetEnchantment enchant) {

                    if (enchant.isAllowedOnBooks() && PetHomeMod.CONFIG.isEnchantEnabled(enchant)&&enchant.getFromAnimalTamer()) {
                      list.add(enchant);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("附魔出错了"));
        }

        Enchantment enchantment = list.get(pRandom.nextInt(list.size()));
        int i = Mth.nextInt(pRandom, enchantment.getMinLevel(), enchantment.getMaxLevel());
        ItemStack itemstack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, i));
        int j = 2 + pRandom.nextInt(5 + i * 10) + 3 * i;
        if (enchantment.isTreasureOnly()) {
            j *= 2;
        }

        if (j > 64) {
            j = 64;
        }

        return new MerchantOffer(new ItemStack(Items.EMERALD, j), new ItemStack(Items.BOOK), itemstack, 12,1, 0.2F);
    }
}
