package com.github.yzqdev.pethome.network;


import com.github.yzqdev.pethome.PetHomeMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class Networking {

    public static void initServer() {
        PayloadTypeRegistry.playC2S().register(PropertiesMessage.TYPE, PropertiesMessage.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PropertiesMessage.TYPE, PropertiesMessage.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PropertiesMessage.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = player.getServer();
            if (server != null) {
                server.execute(() -> PropertiesMessage.handleServer(payload, player));
            }
        });
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(PropertiesMessage.TYPE, (payload, context) -> {
            context.client().execute(() -> PropertiesMessage.handleClient(payload));
        });
    }

    public static void sendMSGToServer(PropertiesMessage message) {
        ClientPlayNetworking.send(message);
    }

    public static void sendNonLocal(PropertiesMessage message, ServerPlayer player) {
        ServerPlayNetworking.send(player, message);
    }

    public static void sendMSGToAll(PropertiesMessage message) {
        MinecraftServer server = com.github.yzqdev.pethome.platform.ServerRef.get();
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                sendNonLocal(message, player);
            }
        }
    }
}
