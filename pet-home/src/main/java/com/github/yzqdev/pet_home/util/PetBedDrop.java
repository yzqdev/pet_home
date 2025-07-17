package com.github.yzqdev.pet_home.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

public class PetBedDrop {
    private static final String DOMESTICATION_INNOVATION = "pet_home";

    public static boolean hasPetBedPos(EntityMaid maid) {

            return TameableUtils.isTamed(maid) && TameableUtils.getPetBedPos(maid) != null;


    }
}