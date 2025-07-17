package com.github.yzqdev.pethome.util;


import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.resources.ResourceLocation;

public class GlobalUtil {
    public static ResourceLocation res(String name){
        return  ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID,name);
    }
}
