package com.github.yzqdev.pethome.server.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public class DIBlockItem extends BlockItem {

    private final Block block;

    public DIBlockItem(Block block, Item.Properties props) {
        super(block, props);
        this.block = block;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return !(block instanceof ShulkerBoxBlock);
    }

    @Override
    public void onDestroyed(ItemEntity p_150700_) {

    }
}
