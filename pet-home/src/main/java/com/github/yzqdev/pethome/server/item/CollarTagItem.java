package com.github.yzqdev.pethome.server.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * 项圈标签物品（可附魔等级 1，附魔载体是标签本身）：右键宠物应用/替换附魔的逻辑在
 * {@code PlayerInteractEntityHandler.handleCollarTag}——物品类只是 tooltip 壳。
 */
public class CollarTagItem extends Item {

    public CollarTagItem(Properties properties) {

        super(properties.enchantable(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltip, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("tooltips.pet_home.substitute_collar.desc").withStyle(ChatFormatting.GREEN));
    }
}
