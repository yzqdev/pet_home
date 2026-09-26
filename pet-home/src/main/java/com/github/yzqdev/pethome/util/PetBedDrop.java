package com.github.yzqdev.pethome.util;

import net.minecraft.world.entity.LivingEntity;

public class PetBedDrop {


    public static boolean hasPetBedPos(LivingEntity maid) {
        return TameableUtils.isTamed(maid) && TameableUtils.getPetBedPos(maid) != null;
    }
}
