package com.github.yzqdev.pethome.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;


public final class Networking {

    private Networking() {
    }

    public static void registerPayloadTypes() {
        PayloadTypeRegistry.serverboundPlay().register(PropertiesMessage.TYPE, PropertiesMessage.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PropertiesMessage.TYPE, PropertiesMessage.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PropertiesMessage.TYPE, PropertiesMessage::handleServer);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(PropertiesMessage.TYPE, PropertiesMessage::handleClient);
    }
}
