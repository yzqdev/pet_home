package com.github.yzqdev.pet_home.server.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class DeedOfOwnershipItem extends Item {

    public DeedOfOwnershipItem() {
        super(new Item.Properties().stacksTo(1));
    }




    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.pet_home.deed_of_ownership.desc").withStyle(ChatFormatting.GREEN));

    }

}