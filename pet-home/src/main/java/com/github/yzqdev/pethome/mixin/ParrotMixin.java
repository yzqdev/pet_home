package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.util.PHEntityDataAccessors;
import com.github.yzqdev.pethome.server.NbtKeys;


import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.util.TameableUtils;
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
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Parrot.class)
public abstract class ParrotMixin extends TamableAnimal implements IComandableMob {

    protected ParrotMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    // 26.1：defineId 必须在 <clinit> 完成（实体构造时 Builder 容量=已注册访问器数，晚注册会 AIOOBE）；
    // 且调用者类必须是实体类、实体类上不能有 @MixinMerged 的访问器字段——
    // 故注入 <clinit> 调用 defineId，访问器统一存到外部类 PHEntityDataAccessors
    @Inject(
            method = {"<clinit>()V"},
            at = {@At("TAIL")},
            remap = false
    )
    private static void di_defineAccessors(CallbackInfo ci) {
        PHEntityDataAccessors.parrotCommand = SynchedEntityData.defineId(Parrot.class, EntityDataSerializers.INT);
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V"}
    )
    private void di_registerData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(PHEntityDataAccessors.parrotCommand, 0);
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V"}
    )
    private void di_writeAdditional(net.minecraft.world.level.storage.ValueOutput compoundNBT, CallbackInfo ci) {
        compoundNBT.putInt(NbtKeys.DI_COMMAND, this.getCommand());
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V"}
    )
    private void di_readAdditional(net.minecraft.world.level.storage.ValueInput compoundNBT, CallbackInfo ci) {
        this.setCommand(compoundNBT.getIntOr(NbtKeys.DI_COMMAND, 0));
    }

    // 26.1：Parrot.mobInteract 结构重构（原 setOrderedToSit INVOKE 点不存在），改为方法 HEAD 直接处理三态指令
    @Inject(
            method = {"mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"},
            remap = true,
            at = @At("HEAD"),
            cancellable = true
    )
    private void di_onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if(PetHomeConfig.trinaryCommandSystem && this.isTame() && TameableUtils.isPetOf(player, this)){
            this.jumping = false;
            this.navigation.stop();
            this.setTarget((LivingEntity)null);
            player.swing(hand, true);
            cir.setReturnValue(this.playerSetCommand(player, this));
        }
    }

    public int getCommand(){
        return this.entityData.get(PHEntityDataAccessors.parrotCommand);
    }

    public void setCommand(int i){
        this.entityData.set(PHEntityDataAccessors.parrotCommand, i);
    }

    @Override
    public void sendCommandMessage(Player owner, int command, Component name) {
        owner.sendOverlayMessage(Component.translatable("message.pet_home.command_" + command, name));
    }
}
