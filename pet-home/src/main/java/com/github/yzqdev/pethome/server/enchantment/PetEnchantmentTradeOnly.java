package com.github.yzqdev.pethome.server.enchantment;

/**
 * 仅交易附魔：在 {@link PetEnchantmentLootOnly} 基础上放开 {@code isTradeable}——
 * 只能从驯兽师村民交易获得（战利品/附魔台/发现渠道均不可用）。
 */
public class PetEnchantmentTradeOnly extends PetEnchantmentLootOnly {

    protected PetEnchantmentTradeOnly(String name, Rarity r, int levels, int minXP) {
        super(name, r, levels, minXP);
    }

    public boolean isTradeable() {
        return true;
    }
}
