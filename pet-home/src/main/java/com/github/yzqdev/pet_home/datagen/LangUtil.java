package com.github.yzqdev.pet_home.datagen;


import com.github.yzqdev.pet_home.PetHomeMod;

/**
 * @author yzqde
 * @date time 2024/12/29 11:19
 * @modified By:
 */
public class LangUtil {
    public static String text(String name) {
        return "text." + PetHomeMod.MODID + "." + name;

    }

    public static String conf(String name) {
        return PetHomeMod.MODID + ".configuration." + name;
    }

    public static String gui(String number) {
        return "gui." + PetHomeMod.MODID + "." + number;
    }

    public static String event(String name) {
        return "event." + PetHomeMod.MODID + "." + name;
    }

    public static String effect(String name) {
        return "effect." + PetHomeMod.MODID + "." + name;
    }



}
