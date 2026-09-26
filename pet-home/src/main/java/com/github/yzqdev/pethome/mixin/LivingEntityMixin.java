package com.github.yzqdev.pethome.mixin;


import com.github.yzqdev.pethome.ModConstants;
import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.ICitadelDataEntity;
import com.github.yzqdev.pethome.server.entity.PetSyncDataEntity;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity  implements ICitadelDataEntity, PetSyncDataEntity {
    // 项圈/附魔数据用 SynchedEntityData 槽位存储（普通字段方案会导致数据在客户端读不到，见 ID 878d6f9e 的排查）
    private static final EntityDataAccessor<CompoundTag> PET_HOME_SAVED_DATA = SynchedEntityData.defineId(LivingEntityMixin.class, EntityDataSerializers.COMPOUND_TAG);
    // 高频计时器用独立 int 槽位：每 tick 的增减只同步一个小整数，不触发整个 CompoundTag 重发
    private static final EntityDataAccessor<Integer> PH_HEALING_AURA_TIME = SynchedEntityData.defineId(LivingEntityMixin.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PH_FROZEN_TIME = SynchedEntityData.defineId(LivingEntityMixin.class, EntityDataSerializers.INT);

    /** 附魔解析缓存；tag 引用被替换或 setCitadelEntityData 写入时失效 */
    @Nullable
    private Map<Enchantment, Integer> pet_home$enchantCache;
    @Nullable
    private CompoundTag pet_home$enchantCacheSource;


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
        entityData.define(PH_HEALING_AURA_TIME, 0);
        entityData.define(PH_FROZEN_TIME, 0);
    }

    @Inject(at = @At("TAIL"), remap = true, method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
    private void citadel_writeAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        CompoundTag citadelDat = getCitadelEntityData();
        if (citadelDat != null) {
            // 计时器已迁移到独立数据槽，存档时写回原 NBT 键，保持存档格式与旧版本/其他端一致
            CompoundTag toSave = citadelDat.copy();
            toSave.putInt(TameableUtils.HEALING_AURA_TIME_TAG, entityData.get(PH_HEALING_AURA_TIME));
            toSave.putInt(TameableUtils.FROZEN_TIME_TAG, entityData.get(PH_FROZEN_TIME));
            compoundNBT.put(ModConstants.entitySyncData, toSave);
        }
    }

    @Inject(at = @At("TAIL"), remap = true, method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
    private void citadel_readAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        if (compoundNBT.contains(ModConstants.entitySyncData)) {
            CompoundTag tag = compoundNBT.getCompound(ModConstants.entitySyncData);
            if (tag.contains(TameableUtils.HEALING_AURA_TIME_TAG)) {
                entityData.set(PH_HEALING_AURA_TIME, tag.getInt(TameableUtils.HEALING_AURA_TIME_TAG));
                tag.remove(TameableUtils.HEALING_AURA_TIME_TAG);
            }
            if (tag.contains(TameableUtils.FROZEN_TIME_TAG)) {
                entityData.set(PH_FROZEN_TIME, tag.getInt(TameableUtils.FROZEN_TIME_TAG));
                tag.remove(TameableUtils.FROZEN_TIME_TAG);
            }
            setCitadelEntityData(tag);
        }
    }
    private boolean isLandAndSea(){
        return TameableUtils.hasEnchant(((LivingEntity) (Entity)this), DIEnchantmentRegistry.AMPHIBIOUS);
    }
    @Override
    public CompoundTag getCitadelEntityData() {
        return entityData.get(PET_HOME_SAVED_DATA);
    }

    @Override
    public void setCitadelEntityData(CompoundTag nbt) {
        // 写方传入的通常是「原地改完的同一引用」：SynchedEntityData.set 的内容等值判断
        // 不会发现变化，必须 force 才能让 vanilla 把更新发给追踪该实体的玩家。
        //原先这里配合 PropertiesMessage 全服广播双通道发送，现在只走 vanilla 单通道（仅追踪者收到）
        pet_home$enchantCache = null;
        entityData.set(PET_HOME_SAVED_DATA, nbt, true);
    }

    @Override
    public int ph_getHealingAuraTime() {
        return entityData.get(PH_HEALING_AURA_TIME);
    }

    @Override
    public void ph_setHealingAuraTime(int time) {
        entityData.set(PH_HEALING_AURA_TIME, time);
    }

    @Override
    public int ph_getFrozenTime() {
        return entityData.get(PH_FROZEN_TIME);
    }

    @Override
    public void ph_setFrozenTime(int time) {
        entityData.set(PH_FROZEN_TIME, time);
    }

    @Override
    public Map<Enchantment, Integer> ph_getEnchantCache(@Nullable CompoundTag currentTag) {
        // 引用比对覆盖两条失效路径：setCitadelEntityData 的显式失效（本端原地写），
        // 以及网络同步直接替换 DataItem 值（对端收到的新 tag 是另一个引用）
        if (pet_home$enchantCache == null || pet_home$enchantCacheSource != currentTag) {
            pet_home$enchantCache = currentTag == null ? new HashMap<>() : TameableUtils.buildEnchantCache(currentTag);
            pet_home$enchantCacheSource = currentTag;
        }
        return pet_home$enchantCache;
    }

}
