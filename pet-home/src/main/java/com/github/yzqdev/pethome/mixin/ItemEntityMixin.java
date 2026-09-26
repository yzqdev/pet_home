package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 替代 Forge ItemExpireEvent（苹果过期变腐烂苹果）。
 */
@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Shadow
    private int age;

    @Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
    private void ph_onItemExpire(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (!self.level().isClientSide() && this.age >= 6000 && ServerEvent.onItemDespawnEvent(self)) {
            ci.cancel();
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void ph_onEntityItemUpdate(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        // 替代 Forge IForgeItem#onEntityItemUpdate（捕捉网掉落物发光/无敌/不坠入虚空）
        if (self.getItem().getItem() instanceof com.github.yzqdev.pethome.server.item.NetItem) {
            if (!self.isCurrentlyGlowing()) {
                self.setGlowingTag(true);
            }
            if (!self.isInvulnerable()) {
                self.setInvulnerable(true);
            }
            if (self.getY() < self.level().getMinBuildHeight()) {
                self.setNoGravity(true);
                self.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                self.setPos(self.getX(), self.level().getMinBuildHeight(), self.getZ());
            }
        }
    }
}
