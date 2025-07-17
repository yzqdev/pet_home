package com.github.yzqdev.pethome.util;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record ItemMobTooltip(net.minecraft.nbt.CompoundTag compoundTag) implements TooltipComponent {
}
