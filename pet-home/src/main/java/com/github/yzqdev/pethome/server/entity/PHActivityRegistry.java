package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.schedule.Activity;

/**
 * 自 1.20 移植：驯服美西螈/青蛙的 follow/stay 活动。
 * 注册：Fabric 原生 {@code Registry.register}。
 */
public class PHActivityRegistry {

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name);
    }

    public static final Activity AXOLOTL_FOLLOW = Registry.register(BuiltInRegistries.ACTIVITY, id("axolotl_follow"), new Activity("axolotl_follow"));
    public static final Activity AXOLOTL_STAY = Registry.register(BuiltInRegistries.ACTIVITY, id("axolotl_stay"), new Activity("axolotl_stay"));
    public static final Activity FROG_FOLLOW = Registry.register(BuiltInRegistries.ACTIVITY, id("frog_follow"), new Activity("frog_follow"));
    public static final Activity FROG_STAY = Registry.register(BuiltInRegistries.ACTIVITY, id("frog_stay"), new Activity("frog_stay"));

    public static void init() {
    }
}
