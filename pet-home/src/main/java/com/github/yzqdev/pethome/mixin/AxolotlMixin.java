package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.server.NbtKeys;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.util.TameableUtils;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 注入：美西螈的完整驯服改造——{@link ModifiedToBeTameable} + {@link IComandableMob} 双接口适配， 自建 TAMED /
 * OWNER_UUID 同步数据（美西螈无原版驯服字段）。喂热带鱼驯服（{@code mobInteract} HEAD 接管）、 跟随主人攻击目标（{@code
 * customServerAiStep} TAIL）、驯服个体不再因远离而消失（{@code removeWhenFarAway}）。
 *
 * <p>特色注入：{@code saveToBucketTag} / {@code loadFromBucketTag}——美西螈可被装进水桶，驯服数据
 * （主人/指令/驯服状态）必须随桶持久化，否则放出来就“失忆”。
 */
@Mixin(Axolotl.class)
public abstract class AxolotlMixin extends Animal implements ModifiedToBeTameable, IComandableMob {

  @Shadow
  public abstract void readAdditionalSaveData(CompoundTag compound);

  /** 驯服主人 UUID（自建字段，读取受 {@code PetHomeConfig.tameableAxolotl} 门控）。 */
  private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
      SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.OPTIONAL_UUID);

  /** 当前三态指令（0=游走 / 1=停留 / 2=跟随）。 */
  private static final EntityDataAccessor<Integer> COMMAND =
      SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.INT);

  /** 是否已驯服（自建字段）。 */
  private static final EntityDataAccessor<Boolean> TAMED =
      SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.BOOLEAN);

  protected AxolotlMixin(EntityType<? extends Animal> type, Level lvl) {
    super(type, lvl);
  }

  @Inject(
      at = {@At("TAIL")},
      remap = true,
      method = {"defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V"})
  private void di_registerData(SynchedEntityData.Builder builder, CallbackInfo ci) {
    builder.define(OWNER_UUID, Optional.empty());
    builder.define(COMMAND, 0);
    builder.define(TAMED, false);
  }

  @Inject(
      at = {@At("TAIL")},
      remap = true,
      method = {"addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"})
  private void di_writeAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
    compoundNBT.putInt(NbtKeys.DI_COMMAND, this.getCommand());
    compoundNBT.putBoolean(NbtKeys.TAMED, this.isTame());
    if (this.getTameOwnerUUID() != null) {
      compoundNBT.putUUID(NbtKeys.OWNER, this.getTameOwnerUUID());
    }
  }

  @Inject(
      at = {@At("TAIL")},
      remap = true,
      method = {"readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"})
  private void di_readAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
    this.setCommand(compoundNBT.getInt(NbtKeys.DI_COMMAND));
    this.setTame(compoundNBT.getBoolean(NbtKeys.TAMED));
    UUID uuid;
    if (compoundNBT.hasUUID(NbtKeys.OWNER)) {
      uuid = compoundNBT.getUUID(NbtKeys.OWNER);
    } else {
      String s = compoundNBT.getString(NbtKeys.OWNER);
      uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
    }

    if (uuid != null) {
      try {
        this.setTameOwnerUUID(uuid);
        this.setTame(true);
      } catch (Throwable throwable) {
        this.setTame(false);
      }
    }
  }

  @Inject(
      at = {@At("TAIL")},
      remap = true,
      method = {"saveToBucketTag(Lnet/minecraft/world/item/ItemStack;)V"})
  private void di_writeAdditionalBucket(ItemStack stack, CallbackInfo ci) {
    CompoundTag compoundNBT =
        stack
            .getOrDefault(
                net.minecraft.core.component.DataComponents.BUCKET_ENTITY_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY)
            .copyTag();
    this.addAdditionalSaveData(compoundNBT);
    compoundNBT.putInt(NbtKeys.DI_COMMAND, this.getCommand());
    compoundNBT.putBoolean(NbtKeys.TAMED, this.isTame());
    if (this.getTameOwnerUUID() != null) {
      compoundNBT.putUUID(NbtKeys.OWNER, this.getTameOwnerUUID());
    }
    stack.set(
        net.minecraft.core.component.DataComponents.BUCKET_ENTITY_DATA,
        net.minecraft.world.item.component.CustomData.of(compoundNBT));
  }

  @Inject(
      at = {@At("TAIL")},
      remap = true,
      method = {"loadFromBucketTag(Lnet/minecraft/nbt/CompoundTag;)V"})
  private void di_readAdditionalBucket(CompoundTag compoundNBT, CallbackInfo ci) {
    this.readAdditionalSaveData(compoundNBT);
    this.setCommand(compoundNBT.getInt(NbtKeys.DI_COMMAND));
    this.setTame(compoundNBT.getBoolean(NbtKeys.TAMED));
    UUID uuid;
    if (compoundNBT.hasUUID(NbtKeys.OWNER)) {
      uuid = compoundNBT.getUUID(NbtKeys.OWNER);
    } else {
      String s = compoundNBT.getString(NbtKeys.OWNER);
      uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
    }
    if (uuid != null) {
      try {
        this.setTameOwnerUUID(uuid);
        this.setTame(true);
      } catch (Throwable throwable) {
        this.setTame(false);
      }
    }
  }

  @Inject(
      method = {
        "mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
      },
      remap = true,
      at = @At(value = "HEAD"),
      cancellable = true)
  private void di_onInteract(
      Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
    if (PetHomeConfig.tameableAxolotl) {
      ItemStack itemStack = player.getItemInHand(hand);
      if (!this.isTame() && this.isFish(itemStack)) {
        this.usePlayerItem(player, hand, itemStack);
        this.heal(2);
        this.playSound(SoundEvents.CAT_EAT, this.getSoundVolume(), this.getVoicePitch());
        if (!this.level().isClientSide()) {
          if (this.getRandom().nextInt(4) == 0) {
            this.spawnTamingParticles(true);
          } else {
            this.spawnTamingParticles(false);
            this.setTame(true);
            this.setTameOwnerUUID(player.getUUID());
            if (player instanceof ServerPlayer) {
              CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer) player, this);
            }
          }
        }
        cir.setReturnValue(InteractionResult.SUCCESS);
      } else if (isTame() && itemStack.getItem() != Items.WATER_BUCKET) {
        if (this.isFish(itemStack) && this.getHealth() < this.getMaxHealth()) {
          this.heal(2);
          this.playSound(SoundEvents.CAT_EAT, this.getSoundVolume(), this.getVoicePitch());
          cir.setReturnValue(InteractionResult.SUCCESS);
        } else if (super.mobInteract(player, hand) == InteractionResult.PASS
            && PetHomeConfig.trinaryCommandSystem) {
          player.swing(hand, true);
          cir.setReturnValue(this.playerSetCommand(player, this));
        }
      }
    }
  }

  @Inject(
      at = {@At("TAIL")},
      remap = true,
      method = {"customServerAiStep()V"})
  private void di_customServerAiStep(CallbackInfo ci) {
    if (this.isTame() && this.getTameOwner() != null) {
      if (this.getTameOwner().getLastHurtMob() != null
          && this.getTameOwner().getLastHurtMob().isAlive()
          && !TameableUtils.hasSameOwnerAs(this, this.getTameOwner().getLastHurtMob())) {
        this.setTarget(this.getTameOwner().getLastHurtMob());
      }
      if (this.getTameOwner().getLastHurtByMob() != null
          && this.getTameOwner().getLastHurtByMob().isAlive()
          && !TameableUtils.hasSameOwnerAs(this, this.getTameOwner().getLastHurtByMob())) {
        this.setTarget(this.getTameOwner().getLastHurtByMob());
      }
    }
  }

  @Inject(
      at = {@At("HEAD")},
      remap = true,
      method = {"Lnet/minecraft/world/entity/animal/axolotl/Axolotl;removeWhenFarAway(D)Z"},
      cancellable = true)
  private void di_removeWhenFarAway(double dist, CallbackInfoReturnable<Boolean> cir) {
    if (this.isTame()) {
      cir.setReturnValue(false);
    }
  }

  private void spawnTamingParticles(boolean smoke) {
    if (!level().isClientSide()) {
      ParticleOptions particleoptions = smoke ? ParticleTypes.SMOKE : ParticleTypes.HEART;
      for (int i = 0; i < 7; ++i) {
        double d0 = this.getRandom().nextGaussian() * 0.02D;
        double d1 = this.getRandom().nextGaussian() * 0.02D;
        double d2 = this.getRandom().nextGaussian() * 0.02D;
        ((ServerLevel) this.level())
            .sendParticles(
                particleoptions,
                this.getRandomX(1.0D),
                this.getRandomY() + 0.5D,
                this.getRandomZ(1.0D),
                3,
                d0,
                d1,
                d2,
                0.03F);
      }
    }
  }

  public int getCommand() {
    return this.entityData.get(COMMAND);
  }

  public void setCommand(int i) {
    this.entityData.set(COMMAND, i);
  }

  public boolean isTame() {
    return this.entityData.get(TAMED);
  }

  public void setTame(boolean b) {
    this.entityData.set(TAMED, b);
  }

  private boolean isFish(ItemStack stack) {
    return isFood(stack) || stack.getItem() == Items.TROPICAL_FISH;
  }

  @Nullable
  public UUID getTameOwnerUUID() {
    return PetHomeConfig.tameableAxolotl
        ? this.entityData.get(OWNER_UUID).orElse((UUID) null)
        : null;
  }

  public void setTameOwnerUUID(@Nullable UUID uuid) {
    this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
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
    return this.getCommand() == 2 && PetHomeConfig.trinaryCommandSystem;
  }

  public boolean isStayingStill() {
    return this.getCommand() == 1 && PetHomeConfig.trinaryCommandSystem;
  }

  public boolean isValidAttackTarget(LivingEntity target) {
    if (this.isAlliedTo(target)) {
      return false;
    }
    if (this.getTameOwner() != null
        && this.getTameOwner().getLastHurtMob() != null
        && this.getTameOwner().getLastHurtMob().equals(target)) {
      return !TameableUtils.hasSameOwnerAs(this, target) && !this.isAlliedTo(target);
    }
    if (this.getTameOwner() != null
        && this.getTameOwner().getLastHurtByMob() != null
        && this.getTameOwner().getLastHurtByMob().equals(target)) {
      return !TameableUtils.hasSameOwnerAs(this, target) && !this.isAlliedTo(target);
    }
    return false;
  }

  @Override
  public void sendCommandMessage(Player owner, int command, Component name) {
    owner.displayClientMessage(
        Component.translatable("message.pet_home.command_" + command, name), true);
  }
}
