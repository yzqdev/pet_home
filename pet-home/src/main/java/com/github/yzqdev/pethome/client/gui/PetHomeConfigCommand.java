package com.github.yzqdev.pethome.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import com.mojang.brigadier.CommandDispatcher;


@Environment(EnvType.CLIENT)
public final class PetHomeConfigCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("pet_home_config")
                .executes(context -> {
                    Minecraft.getInstance().execute(() ->
                            Minecraft.getInstance().setScreen(new PetHomeConfigScreen(null)));
                    return 1;
                }));
    }
}
