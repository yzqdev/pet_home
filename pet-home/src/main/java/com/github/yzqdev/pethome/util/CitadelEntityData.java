package com.github.yzqdev.pethome.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

/**
 * the remaining citadel
 * 26.1+: backed by a syncable data attachment instead of a synched CompoundTag
 */
public class CitadelEntityData {


    /**
     * Returns a copy of the stored tag: mutations on the returned tag are NOT visible
     * to the attachment until written back through {@link #setCitadelTag}.
     * Handing out the internal instance instead would let callers mutate it in place,
     * and setCitadelTag could then no longer detect no-op writes by comparing content.
     */
    public static CompoundTag getOrCreateCitadelTag(LivingEntity entity) {
        return getCitadelTag(entity).copy();
    }

    public static CompoundTag getCitadelTag(LivingEntity entity) {
        CompoundTag tag = entity.getAttached(PHAttachments.PETHOME_DATA);
        return tag == null ? new CompoundTag() : tag;
    }

    public static void setCitadelTag(LivingEntity entity, CompoundTag tag) {
        CompoundTag current = entity.getAttached(PHAttachments.PETHOME_DATA);
        if (current != null && current.equals(tag)) {
            return;
        }
        entity.setAttached(PHAttachments.PETHOME_DATA, tag);
    }
}
