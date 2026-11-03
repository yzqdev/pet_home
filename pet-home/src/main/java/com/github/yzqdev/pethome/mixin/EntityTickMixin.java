package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.EntityTickHandler;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入：{@code Entity#tick} 的 HEAD。把每个实体的 tick 事件转发给 {@link
 * EntityTickHandler#onLivingUpdate}——这是附魔“即行为开关”体系的每帧入口（项圈标签类持续效果在此驱动）。
 */
@Mixin(Entity.class)
public abstract class EntityTickMixin {
  @Inject(
      method = {"tick"},
      at = {@At("HEAD")})
  public void onTick(CallbackInfo ci) {
    Entity entity = (Entity) ((Object) this);
    EntityTickHandler.onLivingUpdate(entity);
  }
}
