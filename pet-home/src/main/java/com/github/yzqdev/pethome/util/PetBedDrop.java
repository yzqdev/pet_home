package com.github.yzqdev.pethome.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.yzqdev.pethome.server.entity.TameableUtils;

public class PetBedDrop {
    private static final String DOMESTICATION_INNOVATION = "pet_home";

    public static boolean hasPetBedPos(EntityMaid maid) {

            return TameableUtils.isTamed(maid) && TameableUtils.getPetBedPos(maid) != null;


    }
}