package com.github.yzqdev.pethome.util;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

/** 捕网 tooltip 组件（服务端构造、随物品 NBT 同步）：携带被装生物的完整实体 NBT，客户端据此重建并渲染 3D 形象。 */
public record ItemMobTooltip(net.minecraft.nbt.CompoundTag compoundTag) implements TooltipComponent {
}
