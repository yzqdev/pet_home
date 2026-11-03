package com.github.yzqdev.pethome.util;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * 捕网/发射器物品携带的被捕获生物 NBT 的 tooltip 组件（{@code TooltipComponent}）， 客户端经 {@link ClientMobTooltip}
 * 渲染为物品悬浮提示里的旋转实体模型。
 */
public record ItemMobTooltip(net.minecraft.nbt.CompoundTag compoundTag)
    implements TooltipComponent {}
