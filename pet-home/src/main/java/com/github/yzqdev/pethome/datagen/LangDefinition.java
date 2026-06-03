package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;

public  interface LangDefinition {
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

    String has_pet_bed_at_pos = text("has_petbed_at_pos");
         String capturing_text = text("capturing");
         String release_text = text("releasing");
         String health_text = text("health");
         String no_net_entity_text = text("no_net_entity");
         String net_launcher_tip = text("net_launcher_tip");
         String net_launcher_default_only_tamable = text("net_launcher_default_only_tamable");
        String network_failed =text("network_failed") ;
}