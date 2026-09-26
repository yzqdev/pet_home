package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 替代 Forge AnvilUpdateEvent：铁砧上合并两个带附魔的项圈。
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

    public AnvilMenuMixin(MenuType<?> menuType, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(menuType, containerId, playerInventory, access);
    }

    @Shadow
    private DataSlot cost;

    @Inject(method = "createResult()V", at = @At("TAIL"))
    private void ph_onAnvilUpdate(CallbackInfo ci) {
        ItemStack left = this.inputSlots.getItem(0);
        ItemStack right = this.inputSlots.getItem(1);
        Pair<ItemStack, Integer> result = ServerEvent.onAnvilUpdate(left, right);
        if (result != null) {
            this.resultSlots.setItem(0, result.getFirst());
            this.cost.set(result.getSecond());
        }
    }
}
