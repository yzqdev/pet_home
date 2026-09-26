package com.github.yzqdev.pethome.server.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import java.util.function.Consumer;


public class PetbedItem extends BlockItem {
    private final DyeColor color;
    private final Block block;

    public PetbedItem(Block block, Properties props, DyeColor color) {
        super(block, props);
        this.block = block;
        this.color = color;
    }

    @Override
    public Block getBlock() {
        return block;
    }


    public void onDestroyed(ItemEntity itemEntity) {

    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltip, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("tooltips.pet_home.substitute_pet_bed.desc").withStyle(ChatFormatting.GREEN));

    }
}
