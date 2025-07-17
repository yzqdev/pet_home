package com.github.yzqdev.pet_home.server.misc.trades;

import com.github.yzqdev.pet_home.datagen.ModTags;
import com.google.common.collect.Lists;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;


import java.util.List;

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

    public MerchantOffer getOffer(Entity entity, RandomSource random) {
        int i = Math.max(6, enchantXp + 5 - random.nextInt(5));
        ItemStack itemstack = enchant(random, new ItemStack(this.itemStack.getItem()), i, enchantmentCount,entity);
        int j = Math.min(this.baseEmeraldCost + i, 64);

        return new MerchantOffer(new ItemCost(Items.EMERALD,j), itemstack, this.maxUses, this.villagerXp, this.priceMultiplier);
    }

    public ItemStack enchant(RandomSource random, ItemStack stack, int enchantXp, int howManyEnchants,Entity trader) {
        List<EnchantmentInstance> list = selectEnchantment(random, stack, enchantXp, howManyEnchants, trader);
        for(EnchantmentInstance enchantmentinstance : list) {
            stack.enchant(enchantmentinstance.enchantment, enchantmentinstance.level);
        }
        return stack;
    }

    public static List<EnchantmentInstance> selectEnchantment(RandomSource random, ItemStack stacks, int expIThink, int enchantmentCount,Entity trader) {
        List<EnchantmentInstance> list = Lists.newArrayList();
        Item item = stacks.getItem();
        int i = stacks.getEnchantmentValue();
        if (i <= 0) {
            return list;
        } else {
            expIThink += 1 + random.nextInt(i / 4 + 1) + random.nextInt(i / 4 + 1);
            float f = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
            expIThink = Mth.clamp(Math.round((float)expIThink + (float)expIThink * f), 1, Integer.MAX_VALUE);
            List<EnchantmentInstance> list1 = getAvailableEnchantmentResults(expIThink, stacks,trader);
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
    private static List<EnchantmentInstance> getAvailableEnchantmentResults(int levels, ItemStack stack,Entity trader) {
        List<EnchantmentInstance> list = Lists.newArrayList();
        Item item = stack.getItem();
        boolean flag = stack.is(Items.BOOK);
      var enchantRegistry = trader.level()
                .registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT) ;
        for (Holder<Enchantment> enchantmentHolder : enchantRegistry.getTagOrEmpty(ModTags.TradableEnchantmentKey)) {
            var enchant=enchantmentHolder.value();
            for(int i = enchant.getMaxLevel(); i > enchant .getMinLevel() - 1; --i) {
                if (levels >= enchant .getMinCost(i) && levels <= enchant .getMaxCost(i)) {
                    list.add(new EnchantmentInstance(enchantmentHolder, i));
                    break;
                }
            }
//            list.add(new EnchantmentInstance(i ,i.value().getMaxLevel()));
        }

//        for(Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
//            if ( enchantment.isDiscoverable() && (enchantment.canApplyAtEnchantingTable(stack) || (flag && enchantment.isAllowedOnBooks()))) {

//            }
//        }

        return list;
    }
}
