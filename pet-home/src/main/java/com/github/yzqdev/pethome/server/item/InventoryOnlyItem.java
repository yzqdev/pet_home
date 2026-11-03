package com.github.yzqdev.pethome.server.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

/**
 * 仅物品栏展示的特效占位物品（deflection_shield / magnet 等）：不进创造 Tab
 * （{@code fillItemCategory} 为空，实现 {@link CustomTabBehavior} 由 Tab 注册器按需填充），
 * 无使用行为，效果由 Mixin/事件读取"玩家是否持有"实现。
 */
public class InventoryOnlyItem extends Item implements CustomTabBehavior {

    public InventoryOnlyItem(Properties properties) {
        super(properties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab.Output contents) {

    }
}
