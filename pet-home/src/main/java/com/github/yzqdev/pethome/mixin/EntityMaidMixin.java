package com.github.yzqdev.pethome.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.yzqdev.pethome.util.PetBedDrop;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 第三方 mod（touhou little maid）联动：女仆绑定宠物床后死亡不掉落装备（对齐 {@code LivingEntityDropMixin}）。
 * 目标类不存在于原版环境，能否应用由 {@code PetHomeMixinPlugin.shouldApplyMixin} 按 mod 是否加载决定。
 */
@Mixin(EntityMaid.class)
public abstract class EntityMaidMixin {

  @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
  private void pethome$dropEquipment(CallbackInfo ci) {
    if (PetBedDrop.hasPetBedPos((LivingEntity) (Object) this)) {
      ci.cancel();
    }
  }
}
