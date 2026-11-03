package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeConfig;
import java.util.function.Predicate;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.raid.Raider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入：{@code Ravager#registerGoals} 的 TAIL。配置 {@code rabbitsScareRavagers} 开启时给劫掠兽补充
 * “害怕兔子”的回避目标（致敬现实中大象怕鼠的梗，13 格感知、1.5~2.0 逃离速度）。
 */
@Mixin(Ravager.class)
public abstract class RavagerMixin extends Raider {

  protected RavagerMixin(
      net.minecraft.world.entity.EntityType<? extends Raider> type,
      net.minecraft.world.level.Level level) {
    super(type, level);
  }

  @Inject(method = "registerGoals", at = @At("TAIL"))
  private void di_registerGoals(CallbackInfo ci) {
    if (PetHomeConfig.rabbitsScareRavagers) {
      Predicate<LivingEntity> avoidFilter =
          (Predicate<LivingEntity>) (Predicate<?>) EntitySelector.NO_SPECTATORS;
      this.goalSelector.addGoal(
          4,
          new AvoidEntityGoal<>(
              (PathfinderMob) (Object) this, Rabbit.class, 13.0F, 1.5D, 2.0D, avoidFilter));
    }
  }
}
