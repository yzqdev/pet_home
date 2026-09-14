package com.github.yzqdev.pethome.server.item;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * 兔子 -> 邪恶兔子、僵尸马 -> 骷髅马的转换逻辑统一在 PlayerInteractEntityHandler.handleEntityConversions 里做，
 * 这里只保留食物与 tooltip 行为（在物品里重复实现会导致双马/双倍消耗，见该 handler 的注释）。
 */
public class SinisterCarrotItem extends Item {

    public SinisterCarrotItem() {
        super(new Properties().rarity(Rarity.UNCOMMON).food((new FoodProperties.Builder()).nutrition(1).saturationModifier(0.3f).effect(new MobEffectInstance(MobEffects.WITHER, 100), 1.0F).build()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltips.pet_home.substitute_sinister_carrot.desc").withStyle(ChatFormatting.GREEN));

    }
}
