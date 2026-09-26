package com.github.yzqdev.pethome.client.gui;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;

/** /pet_home_config 命令：游戏内直接打开驯养革新设置界面 */
public final class PetHomeConfigCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("pet_home_config")
                .requires(source -> source.hasPermission(0))
                .executes(context -> {
                    Minecraft.getInstance().execute(() ->
                            Minecraft.getInstance().setScreen(new PetHomeConfigScreen(null)));
                    return 1; // Command#SUCCESS
                });
    }
}
