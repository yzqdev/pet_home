package com.github.yzqdev.pet_home.util;

import com.github.yzqdev.pet_home.PetHomeMod;
import net.minecraft.resources.ResourceLocation;

public class GlobalUtil {
    public static ResourceLocation res(String name){
        return  ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID,name);
    }
}
