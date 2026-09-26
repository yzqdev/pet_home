package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PetHomeMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * 单通道网络（{@code pet_home:packet_network_name}）。
 *
 * 收包侧直接调用 {@link PropertiesMessage#handleServer} / {@link PropertiesMessage#handleClient}。</p>
 */
public class Networking {
    private static final ResourceLocation PACKET_NETWORK_NAME = new ResourceLocation(PetHomeMod.MODID, "packet_network_name");

    public static void initServer() {
        ServerPlayNetworking.registerGlobalReceiver(PACKET_NETWORK_NAME, (server, player, handler, buf, responseSender) -> {
            PropertiesMessage message = PropertiesMessage.read(buf);
            server.execute(() -> PropertiesMessage.handleServer(message, player));
        });
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(PACKET_NETWORK_NAME, (client, handler, buf, responseSender) -> {
            PropertiesMessage message = PropertiesMessage.read(buf);
            client.execute(() -> PropertiesMessage.handleClient(message));
        });
    }

    public static void sendMSGToServer(PropertiesMessage message) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        PropertiesMessage.write(message, buf);
        ClientPlayNetworking.send(PACKET_NETWORK_NAME, buf);
    }

    public static void sendNonLocal(PropertiesMessage message, ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        PropertiesMessage.write(message, buf);
        ServerPlayNetworking.send(player, PACKET_NETWORK_NAME, buf);
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
