package com.github.yzqdev.pethome.server.item;

import net.minecraft.world.item.CreativeModeTab;

/**
 * 创造模式物品栏自定义填充接口：实现它的物品在 {@code PHCreativeTabRegistry}
 * 遍历时被识别，用 {@code fillItemCategory} 决定往 Tab 里放哪些物品
 * （普通物品按注册顺序自动加入，无需实现本接口）。
 */
public interface CustomTabBehavior {
    /** 向创造模式 Tab 输出本物品应展示的物品堆。 */
    void fillItemCategory(CreativeModeTab.Output contents);
}
