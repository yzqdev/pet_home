package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.server.NbtKeys;
import com.github.yzqdev.pethome.server.entity.IFrog;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.misc.PHTagRegistry;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入：青蛙的完整驯服改造——{@link ModifiedToBeTameable} + {@link IComandableMob} + {@link IFrog}
 * 三接口适配。青蛙同样没有原版驯服字段，自建 TAMED / OWNER_UUID 同步数据；驯服交互（喂食） 走 {@link #onFrogInteract}（由 {@code
 * AnimalMixin} 的 {@code mobInteract} 注入转发进来）。
 *
 * <p>注入点：{@code defineSynchedData} 与 NBT 读写（驯服状态/主人/指令持久化）、{@code tick} TAIL （驯服后首次提升攻击力到 3）、{@code
 * customServerAiStep} TAIL（跟随主人攻击目标/仇恨目标，青蛙用 Brain 而非 GoalSelector，需同时写实体 target 与 Brain 记忆才生效）。
 */
@Mixin(Frog.class)
public abstract class FrogMixin extends Animal
    implements ModifiedToBeTameable, IComandableMob, IFrog {

  /** 驯服主人 UUID（自建字段，读取受 {@code PetHomeConfig.tameableFrog} 门控）。 */
  private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
      SynchedEntityData.defineId(Frog.class, EntityDataSerializers.OPTIONAL_UUID);

  /** 当前三态指令（0=游走 / 1=停留 / 2=跟随）。 */
  private static final EntityDataAccessor<Integer> COMMAND =
      SynchedEntityData.defineId(Frog.class, EntityDataSerializers.INT);

  /** 是否已驯服（自建字段）。 */
  private static final EntityDataAccessor<Boolean> TAMED =
      SynchedEntityData.defineId(Frog.class, EntityDataSerializers.BOOLEAN);

  /** 是否已做过驯服后攻击力初始化（只执行一次，避免每 tick 重设属性）。 */
  private boolean hasInitialDamage = false;

  protected FrogMixin(EntityType<? extends Animal> type, Level lvl) {
    super(type, lvl);
  }

  @Shadow
  public abstract Brain<Frog> getBrain();

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
      method = {"Lnet/minecraft/world/entity/animal/frog/Frog;tick()V"},
      at = {@At("TAIL")},
      remap = true)
  private void di_tick(CallbackInfo ci) {
    if (!hasInitialDamage && this.isTame()) {
      hasInitialDamage = true;
      this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0D);
    }
  }

  @Inject(
      at = {@At("TAIL")},
      remap = true,
      method = {
        "Lnet/minecraft/world/entity/animal/frog/Frog;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"
      })
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
      method = {
        "Lnet/minecraft/world/entity/animal/frog/Frog;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"
      })
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

  @Nullable
  public UUID getTameOwnerUUID() {
    return PetHomeConfig.tameableFrog ? this.entityData.get(OWNER_UUID).orElse(null) : null;
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

  private boolean isTamingItem(ItemStack stack) {
    return stack.is(PHTagRegistry.TAME_FROGS_WITH);
  }

  /**
   * 青蛙驯服/喂食交互（{@code IFrog} 接口实现）：未驯服时喂 {@code pet_home:tame_frogs_with} 标签物品， 1/4
   * 概率冒烟（失败）、否则驯服成功（爱心粒子 + 触发 TAME_ANIMAL 进度）；已驯服时喂驯服物品或黏液球回血， 空手（非水桶）且三态指令开启则循环切换指令。返回 {@code
   * true} 表示交互已被消费。
   */
  @Override
  public boolean onFrogInteract(Player player, InteractionHand hand) {
    if (PetHomeConfig.tameableFrog) {
      ItemStack itemStack = player.getItemInHand(hand);
      if (!this.isTame() && this.isTamingItem(itemStack)) {
        this.usePlayerItem(player, hand, itemStack);
        this.heal(2);
        this.playSound(SoundEvents.FROG_EAT, this.getSoundVolume(), this.getVoicePitch());
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
        return true;
      } else if (isTame() && itemStack.getItem() != Items.WATER_BUCKET) {
        if ((this.isTamingItem(itemStack) || itemStack.is(Items.SLIME_BALL))
            && this.getHealth() < this.getMaxHealth()) {
          this.heal(2);
          this.playSound(SoundEvents.FROG_EAT, this.getSoundVolume(), this.getVoicePitch());
          return true;
        } else if (PetHomeConfig.trinaryCommandSystem) {
          player.swing(hand, true);
          this.playerSetCommand(player, this);
          return false;
        }
      }
    }
    return false;
  }

  /**
   * 服务端 AI 追加：把“主人最近攻击的生物 / 最近伤害主人的生物”写入青蛙的攻击目标（同时设实体 target 与 Brain 的 ATTACK_TARGET
   * 记忆），并跳过同主人的宠物——青蛙是 Brain 驱动，没有 OwnerHurt 系 Goal 可用。
   */
  @Inject(
      at = {@At("TAIL")},
      remap = true,
      method = {"Lnet/minecraft/world/entity/animal/frog/Frog;customServerAiStep()V"})
  private void di_customServerAiStep(CallbackInfo ci) {
    if (this.isTame() && this.getTameOwner() != null) {
      if (this.getTameOwner().getLastHurtMob() != null
          && this.getTameOwner().getLastHurtMob().isAlive()
          && !TameableUtils.hasSameOwnerAs(this, this.getTameOwner().getLastHurtMob())) {
        this.setTarget(this.getTameOwner().getLastHurtMob());
        this.getBrain()
            .setMemory(MemoryModuleType.ATTACK_TARGET, this.getTameOwner().getLastHurtMob());
      }
      if (this.getTameOwner().getLastHurtByMob() != null
          && this.getTameOwner().getLastHurtByMob().isAlive()
          && !TameableUtils.hasSameOwnerAs(this, this.getTameOwner().getLastHurtByMob())) {
        this.setTarget(this.getTameOwner().getLastHurtByMob());
        this.getBrain()
            .setMemory(MemoryModuleType.ATTACK_TARGET, this.getTameOwner().getLastHurtByMob());
      }
    }
  }
}
