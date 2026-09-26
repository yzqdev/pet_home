package com.github.yzqdev.pethome.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * the remaining citadel
 */
public class CitadelEntityData {


    public static CompoundTag getOrCreateCitadelTag(LivingEntity entity) {
        return getCitadelTag(entity).copy();
    }

    public static CompoundTag getCitadelTag(LivingEntity entity) {
        CompoundTag tag = entity.getExistingDataOrNull(PHAttachments.PETHOME_DATA);
        return tag == null ? new CompoundTag() : tag;
    }


    @Nullable
    public static CompoundTag getExistingCitadelTagOrNull(LivingEntity entity) {
        return entity.getExistingDataOrNull(PHAttachments.PETHOME_DATA);
    }

    public static void setCitadelTag(LivingEntity entity, CompoundTag tag) {
        CompoundTag current = entity.getExistingDataOrNull(PHAttachments.PETHOME_DATA);
        if (current != null && current.equals(tag)) {
            return;
        }
        entity.setData(PHAttachments.PETHOME_DATA, tag);
    }
}
