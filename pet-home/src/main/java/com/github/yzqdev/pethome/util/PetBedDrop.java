package com.github.yzqdev.pethome.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

public class PetBedDrop {


    public static boolean hasPetBedPos(EntityMaid maid) {
        return TameableUtils.isTamed(maid) && TameableUtils.getPetBedPos(maid) != null;
    }
}