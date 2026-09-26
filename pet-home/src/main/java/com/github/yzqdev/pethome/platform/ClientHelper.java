package com.github.yzqdev.pethome.platform;

import net.minecraft.client.Minecraft;

/**
 * 仅在客户端环境加载的工具类；common 代码通过环境判断后才允许引用。
 */
public class ClientHelper {

    public static net.minecraft.core.RegistryAccess getRegistryAccess() {
        return Minecraft.getInstance().level.registryAccess();
    }
}
