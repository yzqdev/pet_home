package com.github.yzqdev.pethome.server.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/** 通用 BlockItem 变体：固定方块引用并控制装箱行为——自身是潜影盒类方块时不能再塞进其它容器（防无限套娃）。 */
public class PHBlockItem extends BlockItem {

  /** 对应方块引用（覆写 {@link #getBlock()} 固定返回）。 */
  private final Block block;

  public PHBlockItem(Block block, Item.Properties props) {
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
  public void onDestroyed(ItemEntity itemEntity) {}
}
