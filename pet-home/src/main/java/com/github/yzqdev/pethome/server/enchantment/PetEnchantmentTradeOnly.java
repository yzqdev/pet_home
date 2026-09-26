package com.github.yzqdev.pethome.server.enchantment;

public class PetEnchantmentTradeOnly extends PetEnchantmentLootOnly {

    protected PetEnchantmentTradeOnly(String name, Rarity r, int levels, int minXP) {
        super(name, r, levels, minXP);
    }

    public boolean isTradeable() {
        return true;
    }
}
