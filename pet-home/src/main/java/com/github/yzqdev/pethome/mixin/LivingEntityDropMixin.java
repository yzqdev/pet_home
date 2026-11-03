package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入：{@code LivingEntity#dropAllDeathLoot} 的 HEAD（可取消）。已绑定宠物床的宠物死亡时不掉落任何战利品——
 * 宠物将进入复活队列、次日于床边原样复活，装备与身上的物品应随宠物一起保留。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDropMixin {
  @Inject(
      method = {"dropAllDeathLoot"},
      at = {@At("HEAD")},
      cancellable = true)
  public void onDropAllDeathLoot(CallbackInfo ci) {
    LivingEntity entity = (LivingEntity) ((Object) this);
    if (TameableUtils.isTamed(entity) && TameableUtils.getPetBedPos(entity) != null) {
      ci.cancel();
    }
  }
}
