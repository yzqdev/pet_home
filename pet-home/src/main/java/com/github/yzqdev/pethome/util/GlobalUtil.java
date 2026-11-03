package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.resources.Identifier;

/** 模组命名空间 {@code Identifier} 便捷构造。 */
public class GlobalUtil {
    public static Identifier res(String name) {
        return Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name);
    }
}
