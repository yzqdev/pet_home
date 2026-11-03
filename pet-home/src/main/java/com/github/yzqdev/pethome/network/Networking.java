package com.github.yzqdev.pethome.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;


/**
 * 网络注册中心：每类消息一个强类型 payload——
 * <ul>
 *   <li>C2S：{@link PetCompassOpenPacket}（罗盘打开）、{@link PetCompassActionPacket}（传送/召回）、
 *       {@link PetCompassForgetRequest}（删除档案）；</li>
 *   <li>S2C：{@link PetCompassDataPacket}（罗盘宠物列表）；</li>
 *   <li>双向：{@link EntityDataTagUpdatePacket}（宠物 citadel tag 同步，服务端有所有权鉴权）。</li>
 * </ul>
 * payload 双向注册；服务端接收器在此全局注册，客户端接收器须在客户端入口
 * （{@code PetHomeModClient}）注册。服务端→客户端的常规宠物数据同步不走自定义包，
 * 而是附件 {@code PETHOME_DATA} 的 {@code syncWith} 自动广播。
 */
public final class Networking {

    private Networking() {
    }

    /**
     * 注册全部 payload 的编解码与服务端接收器（由 {@code PetHomeMod#onInitialize} 调用）。
     * C2S 类型注册到 serverbound，S2C 类型注册到 clientbound，双向类型两个方向都注册。
     */
    public static void registerPayloadTypes() {
        PayloadTypeRegistry.serverboundPlay().register(PetCompassOpenPacket.TYPE, PetCompassOpenPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PetCompassActionPacket.TYPE, PetCompassActionPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PetCompassForgetRequest.TYPE, PetCompassForgetRequest.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PetCompassDataPacket.TYPE, PetCompassDataPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(EntityDataTagUpdatePacket.TYPE, EntityDataTagUpdatePacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EntityDataTagUpdatePacket.TYPE, EntityDataTagUpdatePacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PetCompassOpenPacket.TYPE, PetCompassOpenPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(PetCompassActionPacket.TYPE, PetCompassActionPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(PetCompassForgetRequest.TYPE, PetCompassForgetRequest::handle);
        ServerPlayNetworking.registerGlobalReceiver(EntityDataTagUpdatePacket.TYPE, EntityDataTagUpdatePacket::handleServer);
    }

    /**
     * 注册客户端接收器（仅客户端，由 {@code PetHomeModClient#onInitializeClient} 调用）。
     * 处理 S2C 的罗盘数据开屏与 citadel 数据写回本地附件。
     */
    @Environment(EnvType.CLIENT)
    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(PetCompassDataPacket.TYPE, PetCompassDataPacket::handle);
        ClientPlayNetworking.registerGlobalReceiver(EntityDataTagUpdatePacket.TYPE, EntityDataTagUpdatePacket::handleClient);
    }
}
