package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ItemExpireEvent / Item.onEntityItemUpdate 的替代（苹果腐烂、捕网落地保护）。
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
    public void onTick(CallbackInfo ci) {
        ItemEntity entity = (ItemEntity) ((Object) this);
        if (ServerEvent.onItemDespawnEvent(entity)) {
            ci.cancel();
            return;
        }
        ServerEvent.onNetItemEntityTick(entity);
    }
}
