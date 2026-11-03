package com.github.yzqdev.pethome.platform;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/**
 * 替代 NeoForge ServerLifecycleHooks.getCurrentServer()。
 */
public class ServerRef {
    private static MinecraftServer server;

    /** 挂 {@code SERVER_STARTED}/{@code SERVER_STOPPED} 回调维护缓存（由 {@code PetHomeMod#onInitialize} 调用）。 */
    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);
    }

    /**
     * 当前 {@link MinecraftServer}；仅在 {@code SERVER_STARTED} 到 {@code SERVER_STOPPED} 之间非空。
     * 仅供不能直接拿到 server 的静态工具（如罗盘召回、世界数据访问）替代 {@code ServerLifecycleHooks.getCurrentServer()}。
     */
    public static MinecraftServer get() {
        return server;
    }
}
