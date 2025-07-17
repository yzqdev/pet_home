package com.github.yzqdev.pet_home.mixin;

import com.github.yzqdev.pet_home.ModConstants;
import com.github.yzqdev.pet_home.util.IPetbedDataEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin extends Entity implements IPetbedDataEntity {
    @Unique
    private static final EntityDataAccessor<CompoundTag>  PET_HOME_SAVED_DATA = SynchedEntityData.defineId(LivingEntityMixin.class, EntityDataSerializers.COMPOUND_TAG);

    protected LivingEntityMixin(EntityType<? extends Entity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(
        at = {@At("TAIL")},
        remap = true,
        method = {"defineSynchedData"}
    )
    private void citadel_registerData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(PET_HOME_SAVED_DATA, new CompoundTag());
    }

    @Inject(
        at = {@At("TAIL")},
        remap = true,
        method = {"addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"}
    )
    private void citadel_writeAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        CompoundTag citadelDat = this.getCitadelEntityData();
        if (citadelDat != null) {
            compoundNBT.put(ModConstants.entitySyncData, citadelDat);
        }

    }

    @Inject(
        at = {@At("TAIL")},
        remap = true,
        method = {"Lnet/minecraft/world/entity/LivingEntity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"}
    )
    private void citadel_readAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        if (compoundNBT.contains(ModConstants.entitySyncData)) {
            this.setCitadelEntityData(compoundNBT.getCompound(ModConstants.entitySyncData));
        }

    }

    public CompoundTag getCitadelEntityData() {
        return (CompoundTag)this.entityData.get(PET_HOME_SAVED_DATA);
    }

    public void setCitadelEntityData(CompoundTag nbt) {
        this.entityData.set(PET_HOME_SAVED_DATA, nbt);
    }


}
