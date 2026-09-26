package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.util.PHEntityDataAccessors;
import com.github.yzqdev.pethome.server.NbtKeys;



import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.entity.ai.FollowOwner2Goal;
import com.github.yzqdev.pethome.server.entity.ai.OwnerHurtTarget2Goal;
import com.github.yzqdev.pethome.server.entity.ai.Sit2Goal;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@Mixin(Fox.class)
public abstract class FoxMixin extends Animal implements ModifiedToBeTameable, IComandableMob {

    @Shadow @Final private static EntityDataAccessor<Optional<EntityReference<LivingEntity>>> DATA_TRUSTED_ID_0;
    @Shadow @Final private static EntityDataAccessor<Optional<EntityReference<LivingEntity>>> DATA_TRUSTED_ID_1;
    @Shadow abstract void addTrustedEntity(EntityReference<LivingEntity> reference);
    @Shadow public abstract void setSitting(boolean p_28611_);
    @Shadow abstract void setSleeping(boolean p_28627_);

    // 26.1：defineId 必须在 <clinit> 完成（实体构造时 Builder 容量=已注册访问器数，晚注册会 AIOOBE）；
    // 且调用者类必须是实体类、实体类上不能有 @MixinMerged 的访问器字段——
    // 故注入 <clinit> 调用 defineId，访问器统一存到外部类 PHEntityDataAccessors
    @Inject(
            method = {"<clinit>()V"},
            at = {@At("TAIL")},
            remap = false
    )
    private static void di_defineAccessors(CallbackInfo ci) {
        PHEntityDataAccessors.foxCommand = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.INT);
    }

    protected FoxMixin(EntityType<? extends Animal> foxType, Level level) {
        super(foxType, level);
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"registerGoals()V"}
    )
    private void di_registerGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(1, new Sit2Goal(this));
        this.goalSelector.addGoal(2, new FollowOwner2Goal(this, 1.0D, 10.0F, 3.0F, false));
        this.targetSelector.addGoal(1, new OwnerHurtTarget2Goal(this));
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V"}
    )
    private void di_registerData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(PHEntityDataAccessors.foxCommand, 0);
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

    public int getCommand(){
        return this.entityData.get(PHEntityDataAccessors.foxCommand);
    }

    public void setCommand(int i){
        this.entityData.set(PHEntityDataAccessors.foxCommand, i);
    }

    public boolean isTame(){
        return (this.entityData.get(DATA_TRUSTED_ID_0).isPresent() || this.entityData.get(DATA_TRUSTED_ID_1).isPresent()) && PetHomeConfig.tameableFox;
    }

    public void setTame(boolean value){

    }

    @Inject(
            at = @At(
                    shift = At.Shift.BEFORE,
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = true,
            method = {"aiStep()V"}
    )
    private void di_aiStep(CallbackInfo ci) {
        ItemStack stack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        var food = stack.get(net.minecraft.core.component.DataComponents.FOOD);
            if (food != null) {
                this.heal(food.nutrition() * 2);
            }
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"aiStep()V"}
    )
    private void di_aiStep_2(CallbackInfo ci) {
        if(this.isFollowingOwner()){
            this.setSleeping(false);
            this.setSitting(false);
        }
    }

    @Nullable
    public UUID getTameOwnerUUID(){
        if (this.entityData.get(DATA_TRUSTED_ID_0).isPresent()) {
            return this.entityData.get(DATA_TRUSTED_ID_0).get().getUUID();
        } else {
            return this.entityData.get(DATA_TRUSTED_ID_1).map(EntityReference::getUUID).orElse(null);
        }
    }

    public void setTameOwnerUUID(@Nullable UUID uuid){
        addTrustedEntity(EntityReference.of(uuid));
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

    public boolean isFollowingOwner(){
        return this.getCommand() == 2 && PetHomeConfig.trinaryCommandSystem;
    }

    public boolean isStayingStill(){
        return this.getCommand() == 1 && PetHomeConfig.trinaryCommandSystem;
    }

    public boolean isValidAttackTarget(LivingEntity target){
        return true;
    }

    @Override
    public void sendCommandMessage(Player owner, int command, Component name) {
        owner.sendOverlayMessage(Component.translatable("message.pet_home.command_" + command, name));
    }
}
