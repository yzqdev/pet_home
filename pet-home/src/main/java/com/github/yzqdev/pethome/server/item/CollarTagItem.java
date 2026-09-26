package com.github.yzqdev.pethome.server.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class CollarTagItem extends Item {

    public CollarTagItem() {
        super(new Item.Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltips.pet_home.substitute_collar.desc").withStyle(ChatFormatting.GREEN));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    /**
     * 项圈需要能被附魔台识别。
     *
     * <p>原 Forge 版通过 {@code EnchantmentCategory.create("pet", item -> item == COLLAR_TAG)} 注册了
     * 自定义附魔类别；1.20.1 原版没有 {@code EnchantmentCategory.create}，Fabric 侧改用
     * {@link net.minecraft.world.item.enchantment.EnchantmentCategory#BREAKABLE}，
     * 而该类别要求 {@code canBeDepleted()} 为 true。</p>
     *
     * <p>项圈本身没有耐久（{@code maxDamage == 0}，不会被消耗、也不显示耐久条），
     * 这里只放开判定，使附魔台/铁砧对项圈的判定与 Forge 版一致。</p>
     */
    @Override
    public boolean canBeDepleted() {
        return true;
    }
}
