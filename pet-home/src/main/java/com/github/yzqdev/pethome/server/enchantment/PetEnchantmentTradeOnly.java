package com.github.yzqdev.pethome.server.enchantment;

/** 仅交易附魔（CHARISMA）：同样不可附魔台发现，但可由驯兽师村民出售（{@code isTradeable} 复写为 true）。 */
public class PetEnchantmentTradeOnly extends PetEnchantmentLootOnly {

    protected PetEnchantmentTradeOnly(String name, Rarity r, int levels, int minXP) {
        super(name, r, levels, minXP);
    }

    public boolean isTradeable() {
        return true;
    }
}
