package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.ShootTongue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 注入：青蛙吐舌头行为 {@code ShootTongue} 的两个方法，让已驯服青蛙保留捕猎能力但不吞掉猎物：
 *
 * <ul>
 *   <li>{@code checkExtraStartConditions}：在 {@code Frog#canEat} 调用点强制返回 true——驯服青蛙忽略原版
 *       “嘴里没含物品才可捕猎”的限制，持续为主人打猎；
 *   <li>{@code eatEntity}：取消原版吞食，改为只咬一口（{@code doHurtTarget}）并播放吐舌音效； 仅当青蛙嘴里还能装物品（{@code
 *       Frog.canEat}）才真正移除目标——避免驯服青蛙把战利品连实体一起吞没。
 * </ul>
 */
@Mixin(ShootTongue.class)
public class ShootTongueMixin {

  @Inject(
      method = {
        "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/frog/Frog;)Z"
      },
      remap = true,
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/animal/frog/Frog;canEat(Lnet/minecraft/world/entity/LivingEntity;)Z"),
      cancellable = true)
  private void di_checkExtraStartConditions(
      ServerLevel level, Frog frog, CallbackInfoReturnable<Boolean> cir) {
    if (frog instanceof ModifiedToBeTameable tameable && tameable.isTame()) {
      cir.setReturnValue(true);
    }
  }

  @Inject(
      method = {
        "eatEntity(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/frog/Frog;)V"
      },
      remap = true,
      at = @At("HEAD"),
      cancellable = true)
  private void di_eatEntity(ServerLevel level, Frog frog, CallbackInfo ci) {
    if (frog instanceof ModifiedToBeTameable tameable && tameable.isTame()) {
      ci.cancel();
      frog.playSound(SoundEvents.FROG_TONGUE);
      Optional<Entity> optional = frog.getTongueTarget();
      if (optional.isPresent()) {
        Entity entity = optional.get();
        if (entity.isAlive()) {
          frog.doHurtTarget(entity);
          if (!entity.isAlive() && Frog.canEat(frog)) {
            entity.remove(Entity.RemovalReason.KILLED);
          }
        }
      }
    }
  }
}
