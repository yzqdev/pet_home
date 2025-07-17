package com.github.yzqdev.pethome.server.enchantment;

import com.github.yzqdev.pethome.PetHomeMod;

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
