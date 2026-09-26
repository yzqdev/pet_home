package com.github.yzqdev.pethome.util;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * @author yzqdev
 */
public record ItemMobTooltip(net.minecraft.nbt.CompoundTag compoundTag) implements TooltipComponent {
}
