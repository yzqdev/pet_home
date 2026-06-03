package com.github.yzqdev.pethome.server.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DIBlockItem extends BlockItem {

    private final DeferredHolder<Block, Block> blockSupplier;

    public DIBlockItem(DeferredHolder<Block, Block> blockSupplier, Item.Properties props) {
        super(null, props);
        this.blockSupplier = blockSupplier;
    }

    @Override
    public Block getBlock() {
        return blockSupplier.get();
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return !(blockSupplier.get() instanceof ShulkerBoxBlock);
    }

    @Override
    public void onDestroyed(ItemEntity p_150700_) {

    }
}
