package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.util.TameableUtils;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入：让马类（{@code AbstractHorse} 全家）适配 {@link ModifiedToBeTameable} 接口（驯服状态/主人 UUID
 * 直接桥接到原版字段，{@code isTame()} 额外受 {@code PetHomeConfig.tameableHorse} 门控），使马能绑定宠物床、 被捕网捕捉等。核心注入是
 * {@code tickRidden} 的 HEAD（可取消）：带两栖附魔的马被骑乘入水时，接管原版骑乘 tick
 * 为“骑手控制的游泳移动”（原版马在水中无法正常被操控），含下潜（跳跃键蓄力下压）与客户端运动插值。
 */
@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal implements ModifiedToBeTameable {

  @Shadow protected float playerJumpPendingScale;
  @Shadow protected boolean isJumping;
  @Shadow private boolean allowStandSliding;

  protected AbstractHorseMixin(EntityType<? extends Animal> type, Level level) {
    super(type, level);
  }

  @Shadow
  public abstract boolean isTamed();

  @Shadow
  public abstract void setTamed(boolean tame);

  @Shadow
  public abstract void setIsJumping(boolean jumping);

  @Shadow
  public abstract boolean isStanding();

  @Shadow
  @Nullable
  public abstract UUID getOwnerUUID();

  @Shadow
  public abstract void setOwnerUUID(@org.jetbrains.annotations.Nullable UUID uuid);

  @Inject(
      method = {
        "tickRidden(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/Vec3;)V"
      },
      remap = true,
      at = {@At("HEAD")},
      cancellable = true)
  private void di_tickRidden(Player rider, Vec3 vec3, CallbackInfo ci) {
    if (this.isAlive()
        && this.isVehicle()
        && this.isInWaterOrBubble()
        && TameableUtils.hasEnchant(this, PHEnchantments.AMPHIBIOUS)) {
      LivingEntity livingentity = (LivingEntity) this.getControllingPassenger();
      this.setYRot(livingentity.getYRot());
      this.yRotO = this.getYRot();
      this.setXRot(livingentity.getXRot() * 0.5F);
      this.setRot(this.getYRot(), this.getXRot());
      this.yBodyRot = this.getYRot();
      this.yHeadRot = this.yBodyRot;
      float f = livingentity.xxa * 0.5F;
      float f1 = livingentity.zza;
      if (f1 <= 0.0F) {
        f1 *= 0.25F;
      }
      float down = 0.08F;
      if (playerJumpPendingScale != 0) {
        down -= playerJumpPendingScale;
        playerJumpPendingScale = 0;
      }
      if (this.isControlledByLocalInstance()) {
        this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
        super.travel(new Vec3(f, vec3.y - down, f1));
      } else if (livingentity instanceof Player) {
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D).add(0, -down, 0));
      }
      this.calculateEntityAnimation(false);
      this.tryCheckInsideBlocks();
      ci.cancel();
    }
  }

  public boolean isTame() {
    return this.isTamed() && PetHomeConfig.tameableHorse;
  }

  public void setTame(boolean tame) {
    this.setTamed(tame);
  }

  public UUID getTameOwnerUUID() {
    return this.getOwnerUUID();
  }

  public void setTameOwnerUUID(@Nullable UUID uuid) {
    this.setOwnerUUID(uuid);
  }

  @Nullable
  public LivingEntity getTameOwner() {
    try {
      UUID uuid = this.getTameOwnerUUID();
      return uuid == null ? null : this.level().getPlayerByUUID(uuid);
    } catch (IllegalArgumentException illegalargumentexception) {
      return null;
    }
  }

  public boolean isFollowingOwner() {
    return false;
  }

  public boolean isStayingStill() {
    return false;
  }

  public boolean isValidAttackTarget(LivingEntity target) {
    return false;
  }
}
