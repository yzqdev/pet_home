package com.github.yzqdev.pethome.network;


import com.github.yzqdev.pethome.PetHomeMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = PetHomeMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Networking {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PetHomeMod.MODID);
        registrar.playBidirectional(
                PropertiesMessage.TYPE,
                PropertiesMessage.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        PropertiesMessage::handleClient,
                        PropertiesMessage::handleServer
                )
        );


    }

}
