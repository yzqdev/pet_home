package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.event.ServerEventContexts;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 下坐骑拦截（替代 NeoForge 的 {@code EntityMountEvent}）。
 *
 * <p>语义：巨泡（GiantBubbleEntity）存活时，骑乘者不能主动下来。</p>
 *
 * <p>{@code stopRiding} 无参，被骑乘实体用 {@code getVehicle()} 取到，
 * 因此在 HEAD 注入即可，无需匹配参数列表。</p>
 */
@Mixin(Entity.class)
public abstract class EntityMountMixin {

    @Inject(method = "stopRiding", at = @At("HEAD"), cancellable = true, require = 0)
    private void pethome$onDismount(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        Entity vehicle = self.getVehicle();
        if (vehicle == null) {
            return;
        }
        ServerEventContexts.EntityMountEvent event =
                new ServerEventContexts.EntityMountEvent(self, vehicle, true);
        ServerEvent.onEntityMount(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
