package com.github.yzqdev.pethome.server.enchantment;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 宠物附魔基类：不走原版装备附魔路径（挂在宠物项圈的 citadel tag 上，等级由
 * {@code TameableUtils.getEnchantLevel} 解析），本类只提供原版附魔元数据——
 * 经验费用曲线、兼容矩阵（{@link PHEnchantmentRegistry#areCompatible}）与
 * 配置驱动的出现开关（禁用后不出现在战利品/附魔台/生成书上）。
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

    /** 是否出现在驯兽师村民交易中（datagen trade_set 引用；子类可覆写为 false）。 */
    public boolean getFromAnimalTamer() {
        return true;
    }

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        return this != enchantment && PHEnchantmentRegistry.areCompatible(this, enchantment);
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

    /** 附魔注册名（也是配置 enchantments 节的开关键与语言键后缀，非 Enchantment 官方 registry name）。 */
    public String getName() {
        return registryName;
    }
}
