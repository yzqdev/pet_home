package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.event.ServerEventContexts;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 掉落物「腐烂」（替代 NeoForge 的 {@code ItemExpireEvent}）。
 *
 * <p>语义：苹果在地上放久了会变成烂苹果（概率与堆叠数相关，见
 * {@code ServerEvent.onItemDespawnEvent}）。</p>
 *
 * <p>实现说明：NeoForge 的 {@code ItemExpireEvent} 是在原版即将移除掉落物的瞬间触发的，
 * Fabric 侧拿不到那个时间点（需要 @@Shadow 原版私有字段 {@code age} / {@code lifespan}）。
 * 这里改为<b>自行按 tick 计数</b>：存活满 {@link #PET_HOME_DESPAWN_TICKS} tick 后触发一次，
 * 与「掉落物寿命 6000 tick」的原版行为对齐；处理器若 {@code setExtraLife(n)}
 * 则把计数回退 n 个 tick（等价于延长寿命）。</p>
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityExpireMixin {

    /** 原版掉落物寿命 6000 tick */
    @Unique
    private static final int PET_HOME_DESPAWN_TICKS = 6000;

    @Unique
    private int pethome$age = 0;

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void pethome$onTick(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }
        if (++this.pethome$age < PET_HOME_DESPAWN_TICKS) {
            return;
        }
        ServerEventContexts.ItemExpireEvent event = new ServerEventContexts.ItemExpireEvent(self);
        ServerEvent.onItemDespawnEvent(event);
        // 与 NeoForge 的 setExtraLife 等价：把计数回退，等价于延长寿命
        this.pethome$age = PET_HOME_DESPAWN_TICKS - Math.max(0, event.getExtraLife());
    }
}
