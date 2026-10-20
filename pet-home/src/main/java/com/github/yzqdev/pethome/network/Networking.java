package com.github.yzqdev.pethome.network;


import com.github.yzqdev.pethome.PetHomeMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络注册：全 mod 仅一个双向 payload（{@link PropertiesMessage}），
 * 按 propertyID 字符串复用为 4 个逻辑通道；C2S/S2C 各自的 handleServer/handleClient 分发。
 * 新增网络功能沿用该模式，不新增 payload id。
 */
@EventBusSubscriber(modid = PetHomeMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Networking {
    /** MOD bus 注册期把 PropertiesMessage 以 DirectionalPayloadHandler 双向挂到 registrar */
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
