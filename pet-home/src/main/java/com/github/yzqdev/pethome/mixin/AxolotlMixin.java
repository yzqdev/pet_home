package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.NbtKeys;



import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.util.PHEntityDataAccessors;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.util.ProblemReporter;
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
import net.minecraft.world.level.storage.TagValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.jetbrains.annotations.Nullable;
import java.util.UUID;

@Mixin(Axolotl.class)
public abstract class AxolotlMixin extends Animal implements ModifiedToBeTameable, IComandableMob {



    @Inject(
            method = {"<clinit>()V"},
            at = {@At("TAIL")},
            remap = false
    )
    private static void di_defineAccessors(CallbackInfo ci) {
        PHEntityDataAccessors.axolotlOwner = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.STRING);
        PHEntityDataAccessors.axolotlCommand = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.INT);
        PHEntityDataAccessors.axolotlTamed = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.BOOLEAN);
    }

    protected AxolotlMixin(EntityType<? extends Animal> type, Level lvl) {
        super(type, lvl);
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V"}
    )
    private void di_registerData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(PHEntityDataAccessors.axolotlOwner, "");
        builder.define(PHEntityDataAccessors.axolotlCommand, 0);
        builder.define(PHEntityDataAccessors.axolotlTamed, false);
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V"}
    )
    private void di_writeAdditional(net.minecraft.world.level.storage.ValueOutput compoundNBT, CallbackInfo ci) {
        compoundNBT.putInt(NbtKeys.DI_COMMAND, this.getCommand());
        compoundNBT.putBoolean(NbtKeys.TAMED, this.isTame());
        if (this.getTameOwnerUUID() != null) {
            compoundNBT.store(NbtKeys.OWNER, UUIDUtil.CODEC, this.getTameOwnerUUID());
        }
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V"}
    )
    private void di_readAdditional(net.minecraft.world.level.storage.ValueInput compoundNBT, CallbackInfo ci) {
        this.setCommand(compoundNBT.getIntOr(NbtKeys.DI_COMMAND, 0));
        this.setTame(compoundNBT.getBooleanOr(NbtKeys.TAMED, false));
        UUID uuid;
        if (compoundNBT.read(NbtKeys.OWNER, UUIDUtil.CODEC).isPresent()) {
            uuid = compoundNBT.read(NbtKeys.OWNER, UUIDUtil.CODEC).orElse(null);
        } else {
            String s = compoundNBT.getStringOr(NbtKeys.OWNER, "");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.level().getServer(), s);
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
            method = {"saveToBucketTag(Lnet/minecraft/world/item/ItemStack;)V"}
    )
    private void di_writeAdditionalBucket(ItemStack stack, CallbackInfo ci) {
        CompoundTag compoundNBT = stack.getOrDefault(net.minecraft.core.component.DataComponents.BUCKET_ENTITY_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        compoundNBT.putInt(NbtKeys.DI_COMMAND, this.getCommand());
        compoundNBT.putBoolean(NbtKeys.TAMED, this.isTame());
        if (this.getTameOwnerUUID() != null) {
            compoundNBT.store(NbtKeys.OWNER, UUIDUtil.CODEC, this.getTameOwnerUUID());
        }
        stack.set(net.minecraft.core.component.DataComponents.BUCKET_ENTITY_DATA, net.minecraft.world.item.component.CustomData.of(compoundNBT));
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"loadFromBucketTag(Lnet/minecraft/nbt/CompoundTag;)V"}
    )
    private void di_readAdditionalBucket(CompoundTag compoundNBT, CallbackInfo ci) {
        this.load(TagValueInput.create(ProblemReporter.DISCARDING, this.registryAccess(), compoundNBT));
        this.setCommand(compoundNBT.getIntOr(NbtKeys.DI_COMMAND, 0));
        this.setTame(compoundNBT.getBooleanOr(NbtKeys.TAMED, false));
        UUID uuid;
        if (compoundNBT.contains(NbtKeys.OWNER)) {
            uuid = compoundNBT.read(NbtKeys.OWNER, UUIDUtil.CODEC).orElse(null);
        } else {
            String s = compoundNBT.getStringOr(NbtKeys.OWNER, "");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.level().getServer(), s);
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
            method = {"mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"},
            remap = true,
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void di_onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if(PetHomeConfig.tameableAxolotl){
            ItemStack itemStack = player.getItemInHand(hand);
            if(!this.isTame() && this.isFish(itemStack)){
                this.usePlayerItem(player, hand, itemStack);
                this.heal(2);
                this.playSound(net.minecraft.sounds.SoundEvents.GENERIC_EAT.value(), this.getSoundVolume(), this.getVoicePitch());
                if(!this.level().isClientSide()){
                    if(this.getRandom().nextInt(4) == 0){
                        this.spawnTamingParticles(true);
                    }else{
                        this.spawnTamingParticles(false);
                        this.setTame(true);
                        this.setTameOwnerUUID(player.getUUID());
                        if (player instanceof ServerPlayer) {
                            CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer)player, this);
                        }
                    }
                }
                cir.setReturnValue(InteractionResult.SUCCESS);
            }else if(isTame() && itemStack.getItem() != Items.WATER_BUCKET){
                if(this.isFish(itemStack) && this.getHealth() < this.getMaxHealth()){
                    this.heal(2);
                    this.playSound(net.minecraft.sounds.SoundEvents.GENERIC_EAT.value(), this.getSoundVolume(), this.getVoicePitch());
                    cir.setReturnValue(InteractionResult.SUCCESS);
                }else if(super.mobInteract(player, hand) == InteractionResult.PASS && PetHomeConfig.trinaryCommandSystem){
                    player.swing(hand, true);
                    cir.setReturnValue(this.playerSetCommand(player, this));
                }
            }
        }
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"customServerAiStep(Lnet/minecraft/server/level/ServerLevel;)V"}
    )
    private void di_customServerAiStep(net.minecraft.server.level.ServerLevel level, CallbackInfo ci) {
        if(this.isTame() && this.getTameOwner() != null){
            if(this.getTameOwner().getLastHurtMob() != null && this.getTameOwner().getLastHurtMob().isAlive() && !TameableUtils.hasSameOwnerAs(this, this.getTameOwner().getLastHurtMob())){
                this.setTarget(this.getTameOwner().getLastHurtMob());
            }
            if(this.getTameOwner().getLastHurtByMob() != null && this.getTameOwner().getLastHurtByMob().isAlive() && !TameableUtils.hasSameOwnerAs(this, this.getTameOwner().getLastHurtByMob())){
                this.setTarget(this.getTameOwner().getLastHurtByMob());
            }
        }
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"removeWhenFarAway(D)Z"},
            cancellable = true)
    private void di_removeWhenFarAway(double dist, CallbackInfoReturnable<Boolean> cir) {
        if(this.isTame()){
            cir.setReturnValue(false);
        }
    }

    private void spawnTamingParticles(boolean smoke){
        if(!level().isClientSide()){
            ParticleOptions particleoptions = smoke ? ParticleTypes.SMOKE : ParticleTypes.HEART;
            for(int i = 0; i < 7; ++i) {
                double d0 = this.getRandom().nextGaussian() * 0.02D;
                double d1 = this.getRandom().nextGaussian() * 0.02D;
                double d2 = this.getRandom().nextGaussian() * 0.02D;
                ((ServerLevel)this.level()).sendParticles(particleoptions, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 3, d0, d1, d2, 0.03F);
            }
        }
    }
    public int getCommand(){
        return this.entityData.get(PHEntityDataAccessors.axolotlCommand);
    }

    public void setCommand(int i){
        this.entityData.set(PHEntityDataAccessors.axolotlCommand, i);
    }

    public boolean isTame(){
        return this.entityData.get(PHEntityDataAccessors.axolotlTamed);
    }

    public void setTame(boolean b){
        this.entityData.set(PHEntityDataAccessors.axolotlTamed, b);
    }

    private boolean isFish(ItemStack stack){
        return isFood(stack) || stack.getItem() == Items.TROPICAL_FISH;
    }

    @Nullable
    public UUID getTameOwnerUUID() {
        if (!PetHomeConfig.tameableAxolotl) {
            return null;
        }
        try {
            String s = this.entityData.get(PHEntityDataAccessors.axolotlOwner);
            return s.isEmpty() ? null : UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setTameOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(PHEntityDataAccessors.axolotlOwner, uuid == null ? "" : uuid.toString());
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
        if(this.isAlliedTo(target)){
           return false;
        }
        if(this.getTameOwner() != null && this.getTameOwner().getLastHurtMob() != null && this.getTameOwner().getLastHurtMob().equals(target)){
            return !TameableUtils.hasSameOwnerAs(this, target) && !this.isAlliedTo(target);
        }
        if(this.getTameOwner() != null && this.getTameOwner().getLastHurtByMob() != null && this.getTameOwner().getLastHurtByMob().equals(target)){
            return !TameableUtils.hasSameOwnerAs(this, target) && !this.isAlliedTo(target);
        }
        return false;
    }

    @Override
    public void sendCommandMessage(Player owner, int command, Component name) {
    if (owner instanceof  ServerPlayer serverPlayer) {   serverPlayer.sendOverlayMessage(Component.translatable("message.pet_home.command_" + command, name) );}
    }
}
