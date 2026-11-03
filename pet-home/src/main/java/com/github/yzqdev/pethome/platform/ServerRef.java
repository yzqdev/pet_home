package com.github.yzqdev.pethome.platform;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/**
 * 替代 Forge ServerLifecycleHooks.getCurrentServer()。
 */
public class ServerRef {
    private static MinecraftServer server;

    /** 注册服务器生命周期回调以维护缓存；须在 {@code onInitialize} 最先调用，之后各处才能安全取用。 */
    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);
    }

    /** 返回缓存的当前服务器；服务器未启动或已停止时为 {@code null}，调用方需判空。 */
    public static MinecraftServer get() {
        return server;
    }
}
