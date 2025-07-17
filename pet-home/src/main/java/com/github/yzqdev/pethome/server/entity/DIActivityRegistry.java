package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DIActivityRegistry {

    public static final DeferredRegister<Activity> DEF_REG = DeferredRegister.create(ForgeRegistries.ACTIVITIES, PetHomeMod.MODID);
    public static final RegistryObject<Activity> AXOLOTL_FOLLOW = DEF_REG.register("axolotl_follow", () -> new Activity("axolotl_follow"));
    public static final RegistryObject<Activity> AXOLOTL_STAY = DEF_REG.register("axolotl_stay", () -> new Activity("axolotl_stay"));
    public static final RegistryObject<Activity> FROG_FOLLOW = DEF_REG.register("frog_follow", () -> new Activity("frog_follow"));
    public static final RegistryObject<Activity> FROG_STAY = DEF_REG.register("frog_stay", () -> new Activity("frog_stay"));
}
