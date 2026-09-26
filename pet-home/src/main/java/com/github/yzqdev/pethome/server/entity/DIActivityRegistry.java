package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class DIActivityRegistry {

    public static final Activity AXOLOTL_FOLLOW = Registry.register(BuiltInRegistries.ACTIVITY, new ResourceLocation(PetHomeMod.MODID, "axolotl_follow"), new Activity("axolotl_follow"));
    public static final Activity AXOLOTL_STAY = Registry.register(BuiltInRegistries.ACTIVITY, new ResourceLocation(PetHomeMod.MODID, "axolotl_stay"), new Activity("axolotl_stay"));
    public static final Activity FROG_FOLLOW = Registry.register(BuiltInRegistries.ACTIVITY, new ResourceLocation(PetHomeMod.MODID, "frog_follow"), new Activity("frog_follow"));
    public static final Activity FROG_STAY = Registry.register(BuiltInRegistries.ACTIVITY, new ResourceLocation(PetHomeMod.MODID, "frog_stay"), new Activity("frog_stay"));

    public static void init() {
    }
}
