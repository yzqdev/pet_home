package com.github.yzqdev.pethome.server.enchantment;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class PetEnchantment extends Enchantment {

    private int levels;
    private int minXP;
    private String registryName;

    protected PetEnchantment(String name, Rarity r, int levels, int minXP) {
        super(r, DIEnchantmentRegistry.CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
        this.levels = levels;
        this.minXP = minXP;
        this.registryName = name;
    }

    @Override
    public int getMinCost(int i) {
        return minXP + (i - 1) * minXP;
    }

    @Override
    public int getMaxCost(int i) {
        return super.getMinCost(i) + 5;
    }

    @Override
    public int getMaxLevel() {
        return levels;
    }

    public boolean getFromAnimalTamer() {
        return true;
    }

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        return this != enchantment && DIEnchantmentRegistry.areCompatible(this, enchantment);
    }

    @Override
    public boolean isTradeable() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return super.isDiscoverable() && PetHomeMod.CONFIG.isEnchantEnabled(this);
    }

    @Override
    public boolean isAllowedOnBooks() {
        return super.isAllowedOnBooks() && PetHomeMod.CONFIG.isEnchantEnabled(this);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return super.canApplyAtEnchantingTable(stack) && PetHomeMod.CONFIG.isEnchantEnabled(this);
    }

    public String getName() {
        return registryName;
    }
}
