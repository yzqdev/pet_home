package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 替代 Forge EntityMountEvent：阻止从存活的巨型泡泡上主动下来。
 */
@Mixin(Entity.class)
public class EntityDismountMixin {

    /** 注入 {@code Entity#stopRiding} 头部（可取消）：坐骑是存活的巨型泡泡时拒绝下坐骑请求（泡泡作为宠物移动载具，提前下来会失效）。 */
    @Inject(method = "stopRiding()V", at = @At("HEAD"), cancellable = true)
    private void ph_onDismount(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (ServerEvent.onEntityMount(self)) {
            ci.cancel();
        }
    }
}
