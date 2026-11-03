package com.github.yzqdev.pethome.client.gui;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

/**
 * 客户端命令 {@code /pet_home_config}：直接打开 {@link PetHomeConfigScreen}（无父界面）， 与 ModMenu 的“配置”按钮等效；经
 * Fabric 客户端命令 API 注册（{@code ClientLifecycleEvents} 由 PetHomeModClient 接线）。
 */
@Environment(EnvType.CLIENT)
public final class PetHomeConfigCommand {

  /** 注册命令本体；setScreen 经 {@code Minecraft.execute} 切回主线程执行。 */
  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        ClientCommandManager.literal("pet_home_config")
            .executes(
                context -> {
                  Minecraft.getInstance()
                      .execute(
                          () -> Minecraft.getInstance().setScreen(new PetHomeConfigScreen(null)));
                  return 1;
                }));
  }
}
