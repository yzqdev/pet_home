package com.github.yzqdev.pethome.client.gui;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;


/**
 * {@code /pet_home_config} 客户端命令：打开配置界面（ModMenu 缺席时的入口）。
 */
@Environment(EnvType.CLIENT)
public final class PetHomeConfigCommand {

    private PetHomeConfigCommand() {
    }

    /** 注册单字面量命令，执行时切到 {@link PetHomeConfigScreen}（无父界面）。 */
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> command = LiteralArgumentBuilder.literal("pet_home_config");
        command.executes(context -> {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.execute(() -> minecraft.setScreen(new PetHomeConfigScreen(null)));
            return 1;
        });
        dispatcher.register(command);
    }
}
