package com.github.yzqdev.pethome.platform;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/**
 * 当前 {@link MinecraftServer} 的静态引用，替代 NeoForge 的 {@code ServerLifecycleHooks.getCurrentServer()}。
 * 在集成服务器（单人存档局域网）与专用服务器上都由生命周期回调维护。
 */
public class ServerRef {
  private static MinecraftServer server;

  /** 挂 {@code SERVER_STARTED}/{@code SERVER_STOPPED} 回调维护引用；由 PetHomeMod.onInitialize 调用一次。 */
  public static void init() {
    ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);
    ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);
  }

  /** 当前运行中的服务端实例；服务器未启动或已停止（以及物理客户端的主菜单）时为 {@code null}。 */
  public static MinecraftServer get() {
    return server;
  }
}
