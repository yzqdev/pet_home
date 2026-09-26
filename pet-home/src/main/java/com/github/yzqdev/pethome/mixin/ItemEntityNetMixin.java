package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.PHDataComponents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 捕网掉落物的特殊处理。
 *
 * <p>NeoForge 侧这段逻辑写在 {@code NetItem#onEntityItemUpdate}（物品级 tick 回调），
 * Fabric 没有该钩子，因此改注入到 {@link ItemEntity#tick}：
 * 装了生物的捕网掉落物<b>发光、免疫伤害、不会掉出世界底部</b>，
 * 避免玩家辛苦抓到的生物因为掉落物消失 / 掉进虚空而丢失。</p>
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityNetMixin {

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void pethome$keepNetSafe(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }
        ItemStack stack = self.getItem();
        if (stack.isEmpty() || !stack.has(PHDataComponents.ENTITY_HOLDER)) {
            return;
        }
        if (!self.isCurrentlyGlowing()) {
            self.setGlowingTag(true);
        }
        if (!self.isInvulnerable()) {
            self.setInvulnerable(true);
        }
        Vec3 position = self.position();
        int minY = self.level().getMinY();
        if (position.y < minY) {
            self.setNoGravity(true);
            self.setDeltaMovement(Vec3.ZERO);
            self.setPos(position.x, minY, position.z);
        }
    }
}
