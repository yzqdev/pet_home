package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.event.ServerEventContexts;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 掉落物拦截（替代 NeoForge 的 {@code LivingDropsEvent}）。
 *
 * <p>语义：绑定了宠物床的宠物死亡时<b>不掉落任何物品</b>（因为它会在第二天重生）。
 * Fabric 没有掉落物事件，这里在 {@code dropAllDeathLoot} 头部整体取消掉落逻辑。</p>
 *
 * <p>{@code require = 0}：目标方法签名若在版本更新中变化，只记日志不崩溃。</p>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDropsMixin {

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true, require = 0)
    private void pethome$onLivingDrops(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        ServerEventContexts.LivingDropsEvent event = new ServerEventContexts.LivingDropsEvent(self);
        ServerEvent.onLivingDrops(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
