package com.github.yzqdev.pethome.network;


import com.github.yzqdev.pethome.PetHomeMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络注册（RegisterPayloadHandlersEvent）：每类消息一个强类型 payload——
 * <ul>
 *   <li>C2S：{@link PetCompassOpenPacket}（罗盘打开）、{@link PetCompassActionPacket}（传送/召回）、
 *       {@link PetCompassForgetRequest}（删除档案）；
 *   <li>S2C：{@link PetCompassDataPacket}（罗盘宠物列表）；
 *   <li>双向：{@link EntityDataTagUpdatePacket}（宠物数据槽同步，服务端有所有权鉴权）。
 * </ul>
 * 新增网络功能按同模式新增独立 payload，不再复用单包分发。
 */
@EventBusSubscriber(modid = PetHomeMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Networking {
    /** MOD bus 注册期把 5 个强类型 payload 按方向挂到 registrar。 */
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PetHomeMod.MODID);
        registrar.playToServer(PetCompassOpenPacket.TYPE, PetCompassOpenPacket.STREAM_CODEC, PetCompassOpenPacket::handle);
        registrar.playToServer(PetCompassActionPacket.TYPE, PetCompassActionPacket.STREAM_CODEC, PetCompassActionPacket::handle);
        registrar.playToServer(PetCompassForgetRequest.TYPE, PetCompassForgetRequest.STREAM_CODEC, PetCompassForgetRequest::handle);
        registrar.playToClient(PetCompassDataPacket.TYPE, PetCompassDataPacket.STREAM_CODEC, PetCompassDataPacket::handle);
        // 1.21 的 playBidirectional 无双 handler 重载，双向包仍用 DirectionalPayloadHandler
        registrar.playBidirectional(EntityDataTagUpdatePacket.TYPE, EntityDataTagUpdatePacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        EntityDataTagUpdatePacket::handleClient, EntityDataTagUpdatePacket::handleServer));
    }

}
