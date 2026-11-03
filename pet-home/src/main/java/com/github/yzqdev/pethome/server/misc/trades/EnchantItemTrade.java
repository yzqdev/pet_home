package com.github.yzqdev.pethome.server.misc.trades;

import com.google.common.collect.Lists;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;

/**
 * 绿宝石换附魔物品交易：对固定物品按原版附魔台逻辑（复刻的 {@code selectEnchantment}）
 * 随机附 1~N 条魔再卖给玩家；候选池含诅咒、排除 treasure-only，经验等级影响出高等级魔的概率。
 */
public class EnchantItemTrade implements VillagerTrades.ItemListing {
    private final ItemStack itemStack;
    private final int enchantXp;
    private final int baseEmeraldCost;
    private final int maxUses;
    private final int villagerXp;
    private final int enchantmentCount;
    private final float priceMultiplier;

    public EnchantItemTrade(Item item, int enchantXp, int enchantmentCount, int emeralds, int maxUses, int villagerXp) {
        this(item, enchantXp, enchantmentCount, emeralds, maxUses, villagerXp, 0.05F);
    }

    public EnchantItemTrade(Item item, int enchantXp, int enchantmentCount, int emeralds, int maxUses, int villagerXp, float priceMultiplier) {
        this.itemStack = new ItemStack(item);
        this.enchantXp = enchantXp;
        this.baseEmeraldCost = emeralds;
        this.maxUses = maxUses;
        this.villagerXp = villagerXp;
        this.enchantmentCount = enchantmentCount;
        this.priceMultiplier = priceMultiplier;
    }

    /** 报价时即时附魔：附魔等级高低决定绿宝石价（{@code baseEmeraldCost + enchantXp}，上限 64）。 */
    public MerchantOffer getOffer(Entity entity, RandomSource random) {
        int i = Math.max(6, enchantXp + 5 - random.nextInt(5));
        ItemStack itemstack = enchant(random, new ItemStack(this.itemStack.getItem()), i, enchantmentCount);
        int j = Math.min(this.baseEmeraldCost + i, 64);
        ItemStack itemstack1 = new ItemStack(Items.EMERALD, j);
        return new MerchantOffer(itemstack1, itemstack, this.maxUses, this.villagerXp, this.priceMultiplier);
    }

    public ItemStack enchant(RandomSource random, ItemStack stack, int enchantXp, int howManyEnchants) {
        List<EnchantmentInstance> list = selectEnchantment(random, stack, enchantXp, howManyEnchants);
        for(EnchantmentInstance enchantmentinstance : list) {
            stack.enchant(enchantmentinstance.enchantment, enchantmentinstance.level);
        }
        return stack;
    }

    public static List<EnchantmentInstance> selectEnchantment(RandomSource random, ItemStack stacks, int expIThink, int enchantmentCount) {
        List<EnchantmentInstance> list = Lists.newArrayList();
        Item item = stacks.getItem();
        int i = stacks.getEnchantmentValue();
        if (i <= 0) {
            return list;
        } else {
            expIThink += 1 + random.nextInt(i / 4 + 1) + random.nextInt(i / 4 + 1);
            float f = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
            expIThink = Mth.clamp(Math.round((float)expIThink + (float)expIThink * f), 1, Integer.MAX_VALUE);
            List<EnchantmentInstance> list1 = getAvailableEnchantmentResults(expIThink, stacks);
            int ehcantmentsSoFar = 0;
            if (!list1.isEmpty()) {
                WeightedRandom.getRandomItem(random, list1).ifPresent(list::add);
                while(ehcantmentsSoFar < enchantmentCount && random.nextInt(25) != 0) {
                    if (!list.isEmpty()) {
                        EnchantmentHelper.filterCompatibleEnchantments(list1, Util.lastOf(list));
                    }

                    if (list1.isEmpty()) {
                        break;
                    }

                    WeightedRandom.getRandomItem(random, list1).ifPresent(list::add);
                    ehcantmentsSoFar++;
                    expIThink /= 2;
                }
            }
            return list;
        }
    }

    /*
        Inclusive of curses, not of treasure
     */
    private static List<EnchantmentInstance> getAvailableEnchantmentResults(int levels, ItemStack stack) {
        List<EnchantmentInstance> list = Lists.newArrayList();
        Item item = stack.getItem();
        boolean flag = stack.is(Items.BOOK);

        for(Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
            if ( enchantment.isDiscoverable() && (enchantment.canApplyAtEnchantingTable(stack) || (flag && enchantment.isAllowedOnBooks()))) {
                for(int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
                    if (levels >= enchantment.getMinCost(i) && levels <= enchantment.getMaxCost(i)) {
                        list.add(new EnchantmentInstance(enchantment, i));
                        break;
                    }
                }
            }
        }

        return list;
    }
}
