package com.github.yzqdev.pethome.server.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

/**
 * 仅存档展示用的占位物品（偏转护盾 / 磁铁等特效物品）：{@code fillItemCategory} 为空，
 * 不出现在创造模式物品栏，只能经其它途径获得。
 */
public class InventoryOnlyItem extends Item implements CustomTabBehavior {

    public InventoryOnlyItem(Properties properties) {
        super(properties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab.Output contents) {

    }
}
