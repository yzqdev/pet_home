package com.github.yzqdev.pet_home.datagen;


import com.github.yzqdev.pet_home.PetHomeMod;

/**
 * @author yzqde
 * @date time 2024/12/29 11:19
 * @modified By:
 */
public class LangDefinition {
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


    public static class ConstantMsg{
        public static String has_pet_bed_at_pos= LangDefinition.text("has_petbed_at_pos");
        public static String capturing_text= LangDefinition.text("capturing");
        public static String release_text= LangDefinition.text("releasing");
        public static String health_text= LangDefinition.text("health");
        public static String no_net_entity_text= LangDefinition.text("no_net_entity");
        public static String net_launcher_tip= LangDefinition.text("net_launcher_tip");
        public static String net_launcher_default_only_tamable= LangDefinition.text("net_launcher_default_only_tamable");
    }
}
