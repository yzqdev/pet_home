package com.github.yzqdev.pethome.client.gui;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;


@Environment(EnvType.CLIENT)
public final class PetHomeConfigCommand {

    private PetHomeConfigCommand() {
    }

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
