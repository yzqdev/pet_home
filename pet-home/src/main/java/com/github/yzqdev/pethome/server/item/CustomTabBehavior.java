package com.github.yzqdev.pethome.server.item;

import net.minecraft.world.item.CreativeModeTab;

/** 物品自定义创造模式物品栏填充行为：由创造物品栏构建处调用，物品据此决定是否/如何入栏。 */
public interface CustomTabBehavior {
    void fillItemCategory(CreativeModeTab.Output contents);
}
