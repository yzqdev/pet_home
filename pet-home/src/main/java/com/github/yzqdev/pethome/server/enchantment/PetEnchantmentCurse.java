package com.github.yzqdev.pethome.server.enchantment;

import com.github.yzqdev.pethome.PetHomeMod;

/**
 * 诅咒附魔基类（immaturity/undead 等负面附魔）：固定 1 级、费用 25~50、不进村民交易；
 * {@code petCurseEnchantmentsLootOnly} 开启时 treasure-only（只出现在战利品，不进附魔台）。
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
