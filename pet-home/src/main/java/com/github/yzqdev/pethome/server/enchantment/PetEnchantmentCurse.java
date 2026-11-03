package com.github.yzqdev.pethome.server.enchantment;

import com.github.yzqdev.pethome.PetHomeMod;

/**
 * 诅咒类宠物附魔（三个诅咒）：固定 1 级、高经验消耗、不出现于交易，
 * 是否为“宝藏附魔”（只进战利品 / 不上附魔台）由配置 {@code pet_curse_enchantments_loot_only} 决定。
 */
public class PetEnchantmentCurse extends PetEnchantment {

    protected PetEnchantmentCurse(String name, Rarity r) {
        super(name, r, 1, 25);
    }

    public int getMinCost(int cost) {
        return 25;
    }

    public int getMaxCost(int cost) {
        return 50;
    }

    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    public boolean isTreasureOnly() {
        return PetHomeMod.CONFIG.petCurseEnchantmentsLootOnly.get();
    }


    public boolean isCurse() {
        return true;
    }
}
