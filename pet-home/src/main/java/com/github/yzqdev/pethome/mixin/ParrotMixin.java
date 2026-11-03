package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.server.NbtKeys;
import com.github.yzqdev.pethome.util.IComandableMob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 注入：把鹦鹉接入三态指令系统——实现 {@link IComandableMob}，注入点与 {@link WolfMixin} 同构： {@code
 * defineSynchedData}（注册指令字段）、NBT 读写（指令持久化）、{@code mobInteract} 的 {@code setOrderedToSit}
 * 调用点（可取消，右键空手由“切换坐下”改为循环切换三态指令）。
 */
@Mixin(Parrot.class)
public abstract class ParrotMixin extends TamableAnimal implements IComandableMob {
  /** 当前三态指令（0=游走 / 1=停留 / 2=跟随），同步字段。 */
  private static final EntityDataAccessor<Integer> COMMAND =
      SynchedEntityData.defineId(Parrot.class, EntityDataSerializers.INT);

  protected ParrotMixin(EntityType<? extends TamableAnimal> type, Level level) {
    super(type, level);
  }

  @Inject(
      at = {@At("TAIL")},
      remap = true,
      method = {"defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V"})
  private void di_registerData(SynchedEntityData.Builder builder, CallbackInfo ci) {
    builder.define(COMMAND, 0);
  }

  @Inject(
      at = {@At("TAIL")},
      remap = true,
      method = {"addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"})
  private void di_writeAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
    compoundNBT.putInt(NbtKeys.DI_COMMAND, this.getCommand());
  }

  @Inject(
      at = {@At("TAIL")},
      remap = true,
      method = {"readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"})
  private void di_readAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
    this.setCommand(compoundNBT.getInt(NbtKeys.DI_COMMAND));
  }

  @Inject(
      method = {
        "mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
      },
      remap = true,
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/world/entity/animal/Parrot;setOrderedToSit(Z)V"),
      cancellable = true)
  private void di_onInteract(
      Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
    if (PetHomeConfig.trinaryCommandSystem) {
      this.jumping = false;
      this.navigation.stop();
      this.setTarget((LivingEntity) null);
      player.swing(hand, true);
      cir.setReturnValue(this.playerSetCommand(player, this));
    }
  }

  public int getCommand() {
    return this.entityData.get(COMMAND);
  }

  public void setCommand(int i) {
    this.entityData.set(COMMAND, i);
  }

  @Override
  public void sendCommandMessage(Player owner, int command, Component name) {
    owner.displayClientMessage(
        Component.translatable("message.pet_home.command_" + command, name), true);
  }
}
