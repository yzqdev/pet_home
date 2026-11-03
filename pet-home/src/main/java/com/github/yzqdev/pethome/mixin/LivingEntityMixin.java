package com.github.yzqdev.pethome.mixin;


import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.ICitadelDataEntity;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 宠物持久化数据通道：实现 {@link ICitadelDataEntity}——在 {@code defineSynchedData} 注入一个
 * CompoundTag 数据通道（随 NBT 键 {@code PetHomeEntityData} 持久化），即全模组宠物数据
 * （附魔表、冷却、宠物床等）的存储位置；{@code getWaterSlowDown} 支持 AMPHIBIOUS 水中免减速。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity  implements ICitadelDataEntity {
    private static final EntityDataAccessor<CompoundTag> PET_HOME_SAVED_DATA = SynchedEntityData.defineId(LivingEntityMixin.class, EntityDataSerializers.COMPOUND_TAG);


    public LivingEntityMixin(EntityType<?> entityType, Level lvl) {
        super(entityType, lvl);
    }

    @Inject(
            method = {"getWaterSlowDown()F"},
            remap = true,
            at = @At(value = "TAIL"),
            cancellable = true
    )
    private void di_getWaterSlowdown(CallbackInfoReturnable<Float> cir) {
        if(TameableUtils.isTamed(this) && isLandAndSea()){
            cir.setReturnValue(0.98F);
        }
    }
    @Inject(at = @At("TAIL"), remap = true, method = "defineSynchedData()V")
    private void citadel_registerData(CallbackInfo ci) {
        entityData.define(PET_HOME_SAVED_DATA, new CompoundTag());
    }

    @Inject(at = @At("TAIL"), remap = true, method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
    private void citadel_writeAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        CompoundTag citadelDat = getCitadelEntityData();
        if (citadelDat != null) {
            compoundNBT.put(PHConstants.entitySyncData, citadelDat);
        }
    }

    @Inject(at = @At("TAIL"), remap = true, method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
    private void citadel_readAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        if (compoundNBT.contains(PHConstants.entitySyncData)) {
            setCitadelEntityData(compoundNBT.getCompound(PHConstants.entitySyncData));
        }
    }
    private boolean isLandAndSea(){
        return TameableUtils.hasEnchant(((LivingEntity) (Entity)this), PHEnchantmentRegistry.AMPHIBIOUS);
    }
    @Override
    public CompoundTag getCitadelEntityData() {
        return entityData.get(PET_HOME_SAVED_DATA);
    }

    @Override
    public void setCitadelEntityData(CompoundTag nbt) {
        entityData.set(PET_HOME_SAVED_DATA, nbt);
    }

}
