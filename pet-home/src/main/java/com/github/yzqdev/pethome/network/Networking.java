package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

public class Networking {
    private static final String PROTOCOL_VERSION = Integer.toString(1);
    private static final ResourceLocation PACKET_NETWORK_NAME = ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID,"packet_network_name");
    public static final SimpleChannel NETWORK_WRAPPER = NetworkRegistry.ChannelBuilder
            .named(PACKET_NETWORK_NAME)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();
    public static ServerProxy PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public static <T> void sendMSGToServer(T message) {
        NETWORK_WRAPPER.sendToServer(message);
    }
    public static <T> void sendNonLocal(T t, ServerPlayer player) {
        NETWORK_WRAPPER.sendTo(t, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendMSGToAll(T message) {
        for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            sendNonLocal(message, player);
        }
    }
}
