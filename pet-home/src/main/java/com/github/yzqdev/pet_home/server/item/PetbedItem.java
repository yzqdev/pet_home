package com.github.yzqdev.pet_home.server.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;


public class PetbedItem extends BlockItem {
    private final DyeColor color;
    private final DeferredHolder<Block,Block> blockSupplier;

    public PetbedItem(DeferredHolder<Block,Block> blockSupplier, Properties props, DyeColor color) {
        super(null, props);
        this.blockSupplier = blockSupplier;
        this.color = color;
    }

    @Override
    public Block getBlock() {
        return blockSupplier.get();
    }



    public void onDestroyed(ItemEntity itemEntity) {

    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltips.pet_home.substitute_pet_bed.desc").withStyle(ChatFormatting.GREEN));

    }
}
