package com.github.yzqdev.pethome.server.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 仅战利品附魔：treasure-only、不进村民交易、{@code isDiscoverable=false}
 * （不出现于附魔台/钓鱼等发现渠道），只能从 PHLootModifier 注入的战利品表获取。
 */
public class PetEnchantmentLootOnly extends PetEnchantment {

    protected PetEnchantmentLootOnly(String name, Rarity r, int levels, int minXP) {
        super(name, r, levels, minXP);
    }

    public boolean isTreasureOnly() {
        return true;
    }

    public boolean isTradeable() {
        return false;
    }

    public boolean isDiscoverable() {
        return false;
    }

    protected boolean checkCompatibility(Enchantment enchantment) {
        return this != enchantment && PHEnchantmentRegistry.areCompatible(this, enchantment);
    }

}
