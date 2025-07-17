package com.github.yzqdev.pet_home.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

/**
 * the remaining citadel
 */
public class CitadelEntityData {


    public static CompoundTag getOrCreateCitadelTag(LivingEntity entity) {
        CompoundTag tag = getCitadelTag(entity);
        return tag == null ? new CompoundTag() : tag;
    }

    public static CompoundTag getCitadelTag(LivingEntity entity) {
        return entity instanceof IPetbedDataEntity ? ((IPetbedDataEntity) entity).getCitadelEntityData() : new CompoundTag();
    }

    public static void setCitadelTag(LivingEntity entity, CompoundTag tag) {
        if(entity instanceof IPetbedDataEntity){
            ((IPetbedDataEntity) entity).setCitadelEntityData(tag);
        }
    }
}
