package com.github.yzqdev.pethome.server.item;

import net.minecraft.world.item.CreativeModeTab;

/**
 * 自定义创造模式物品栏填充接口：实现者自行决定往哪个 {@code CreativeModeTab} 塞什么 （由创造模式物品栏注册处的遍历回调调用），用于表达原版 {@code Item}
 * 无法描述的特殊填充行为。
 */
public interface CustomTabBehavior {
  void fillItemCategory(CreativeModeTab.Output contents);
}
