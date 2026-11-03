package com.github.yzqdev.pethome.server.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 仅战利品附魔（BUBBLING / VAMPIRE / VOID_CLOUD 等）：不可附魔台发现（{@code isDiscoverable} 为 false）、
 * 不参与交易，只能从结构战利品注入获得（见 {@code DILootModifier}）。
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
