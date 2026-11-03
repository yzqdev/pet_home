package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.resources.ResourceLocation;

/** 全局杂项工具。 */
public class GlobalUtil {
  /** 拼出 {@code pet_home:<name>} 资源 ID 的快捷工厂。 */
  public static ResourceLocation res(String name) {
    return ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, name);
  }
}
