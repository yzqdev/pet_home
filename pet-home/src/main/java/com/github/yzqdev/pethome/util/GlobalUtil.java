package com.github.yzqdev.pethome.util;


import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.resources.ResourceLocation;

/** 全局小工具：modid 资源路径快捷构造。 */
public class GlobalUtil {
    /** 用 modid {@code pet_home} 拼出资源路径（注册 / datagen 通用）。 */
    public static ResourceLocation res(String name){
        return  new ResourceLocation(PetHomeMod.MODID,name);
    }
}
