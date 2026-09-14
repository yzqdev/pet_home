package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 自 1.20 移植：驯服美西螈/青蛙的 follow/stay 活动 */
public class PHActivityRegistry {

    public static final DeferredRegister<Activity> DEF_REG = DeferredRegister.create(BuiltInRegistries.ACTIVITY, PetHomeMod.MODID);
    public static final DeferredHolder<Activity, Activity> AXOLOTL_FOLLOW = DEF_REG.register("axolotl_follow", () -> new Activity("axolotl_follow"));
    public static final DeferredHolder<Activity, Activity> AXOLOTL_STAY = DEF_REG.register("axolotl_stay", () -> new Activity("axolotl_stay"));
    public static final DeferredHolder<Activity, Activity> FROG_FOLLOW = DEF_REG.register("frog_follow", () -> new Activity("frog_follow"));
    public static final DeferredHolder<Activity, Activity> FROG_STAY = DEF_REG.register("frog_stay", () -> new Activity("frog_stay"));
}
