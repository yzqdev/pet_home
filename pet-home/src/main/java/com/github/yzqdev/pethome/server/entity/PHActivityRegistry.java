package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.schedule.Activity;

/** 自 1.20 移植：驯服美西螈/青蛙的 follow/stay 活动。 注册：Fabric 原生 {@code Registry.register}。 */
public class PHActivityRegistry {

  private static ResourceLocation id(String name) {
    return ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, name);
  }

  /** 美西螈"跟随"活动（三态指令 = 跟随）。 */
  public static final Activity AXOLOTL_FOLLOW =
      Registry.register(
          BuiltInRegistries.ACTIVITY, id("axolotl_follow"), new Activity("axolotl_follow"));

  /** 美西螈"停留"活动（三态指令 = 停留）。 */
  public static final Activity AXOLOTL_STAY =
      Registry.register(
          BuiltInRegistries.ACTIVITY, id("axolotl_stay"), new Activity("axolotl_stay"));

  /** 青蛙"跟随"活动（三态指令 = 跟随）。 */
  public static final Activity FROG_FOLLOW =
      Registry.register(BuiltInRegistries.ACTIVITY, id("frog_follow"), new Activity("frog_follow"));

  /** 青蛙"停留"活动（三态指令 = 停留）。 */
  public static final Activity FROG_STAY =
      Registry.register(BuiltInRegistries.ACTIVITY, id("frog_stay"), new Activity("frog_stay"));

  /** 空实现：由主入口调用以触发类加载、执行上方静态注册。 */
  public static void init() {}
}
