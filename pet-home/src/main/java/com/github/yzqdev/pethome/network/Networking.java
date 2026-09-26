package com.github.yzqdev.pethome.network;


import com.github.yzqdev.pethome.PetHomeMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = PetHomeMod.MODID)
public class Networking {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PetHomeMod.MODID);
        // 26.1: DirectionalPayloadHandler 已删，双向包直接分别传 server/client handler
        registrar.playBidirectional(
                PropertiesMessage.TYPE,
                PropertiesMessage.STREAM_CODEC,
                PropertiesMessage::handleServer,
                PropertiesMessage::handleClient
        );


    }

}