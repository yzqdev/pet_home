package com.github.yzqdev.pethome.server.enchantment;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.world.entity.EquipmentSlot;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 宠物附魔基类：附魔本体只携带等级范围 / 兼容性 / 可发现性等定义，不含效果逻辑——
 * 效果由事件层读取宠物身上挂载的附魔等级后实现（载体是宠物而非装备，经项圈标签写入）。
 * {@code minXP} 为 1 级附魔的基础经验消耗。
 */
public class PetEnchantment extends Enchantment {

    private int levels;
    private int minXP;
    private String registryName;

    protected PetEnchantment(String name, Rarity r, int levels, int minXP) {
        super(r, PHEnchantmentRegistry.CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
        return this != enchantment && PHEnchantmentRegistry.areCompatible(this, enchantment);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof com.github.yzqdev.pethome.server.item.CollarTagItem && PetHomeMod.CONFIG.isEnchantEnabled(this);
    }


    @Override
    public boolean isDiscoverable() {
        return super.isDiscoverable() && PetHomeMod.CONFIG.isEnchantEnabled(this);
    }

    public String getName() {
        return registryName;
    }
}
