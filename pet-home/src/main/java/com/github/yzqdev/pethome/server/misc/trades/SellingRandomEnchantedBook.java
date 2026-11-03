package com.github.yzqdev.pethome.server.misc.trades;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.enchantment.PetEnchantment;
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

/**
 * 售卖随机宠物附魔书（驯兽师高级交易）：反射扫描 {@link PHEnchantmentRegistry}，
 * 取「允许上书 + 配置启用 + {@code getFromAnimalTamer}」的附魔随机一本；绿宝石价随等级上涨、
 * treasure-only 翻倍、上限 64。候选集为空时 {@code list.get(...)} 会抛
 * {@code IndexOutOfBoundsException}（仅在全部附魔被禁用/关闭时可能发生）。
 */
public class SellingRandomEnchantedBook implements VillagerTrades.ItemListing {



    public MerchantOffer getOffer(Entity trader, RandomSource pRandom) {
        List<PetEnchantment> list=new ArrayList<>();
        try {
            for (Field f : PHEnchantmentRegistry.class.getDeclaredFields()) {
                Object obj = null;
                obj = f.get(null);
                if (obj instanceof PetEnchantment enchant) {

                    if (enchant.isAllowedOnBooks() && PetHomeMod.CONFIG.isEnchantEnabled(enchant)&&enchant.getFromAnimalTamer()) {
                      list.add(enchant);
                    }
                }
            }
        } catch (Exception e) {
            PetHomeMod.LOGGER.error("Exception", e);
            // getOffer 在服务端运行：不能用 Minecraft.getInstance()（专用服务器会崩），走实体级消息（非玩家实体为空实现）
            trader.sendSystemMessage(Component.translatable(LangDefinition.MESSAGE_ENCHANT_ERROR));
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
