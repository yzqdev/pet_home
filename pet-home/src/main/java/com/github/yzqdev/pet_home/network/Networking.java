package com.github.yzqdev.pet_home.network;


import com.github.yzqdev.pet_home.PetHomeMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = PetHomeMod.MODID,bus = EventBusSubscriber.Bus.MOD)
public class Networking {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar( PetHomeMod.MODID);
        registrar.playBidirectional(
               PropertiesMessage.TYPE,
                PropertiesMessage.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ClientPayloadHandler::handleData,
                        ServerPayloadHandler::handleData
                )
        );


    }

}
