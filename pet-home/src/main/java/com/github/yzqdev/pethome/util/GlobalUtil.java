package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.resources.Identifier;

public class GlobalUtil {
    public static Identifier res(String name) {
        return Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name);
    }
}
