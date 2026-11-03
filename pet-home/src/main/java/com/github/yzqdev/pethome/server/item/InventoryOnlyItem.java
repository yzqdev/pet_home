package com.github.yzqdev.pethome.server.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

/** 仅存在于物品栏/容器中的功能物品：不进任何创造模式物品栏（空实现 {@link #fillItemCategory}），玩家只能通过合成/交易获得。 */
public class InventoryOnlyItem extends Item implements CustomTabBehavior {

  public InventoryOnlyItem(Properties properties) {
    super(properties);
  }

  @Override
  public void fillItemCategory(CreativeModeTab.Output contents) {}
}
