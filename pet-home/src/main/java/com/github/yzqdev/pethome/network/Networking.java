package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Forge SimpleChannel 网络通道（协议版本 2）：每类消息一个强类型消息——
 * C2S {@link PetCompassOpenPacket}/{@link PetCompassActionPacket}/{@link PetCompassForgetRequest}，
 * S2C {@link PetCompassDataPacket}，双向 {@link EntityDataTagUpdatePacket}（服务端有所有权鉴权）。
 * PROXY 经 {@link DistExecutor} 在客户端实例化为 ClientProxy（S2C tag 写数据槽用，避免专用服务器加载客户端类）。
 */
public class Networking {
    private static final String PROTOCOL_VERSION = Integer.toString(2);
    private static final ResourceLocation PACKET_NETWORK_NAME = ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID,"packet_network_name");
    public static final SimpleChannel NETWORK_WRAPPER = NetworkRegistry.ChannelBuilder
            .named(PACKET_NETWORK_NAME)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();
    public static ServerProxy PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    /** FMLCommonSetup 阶段注册全部消息（discriminator 顺序即消息表顺序，新增只能追加）。 */
    public static void register() {
        int id = 0;
        NETWORK_WRAPPER.registerMessage(id++, EntityDataTagUpdatePacket.class, EntityDataTagUpdatePacket::write, EntityDataTagUpdatePacket::read, EntityDataTagUpdatePacket::handle);
        NETWORK_WRAPPER.registerMessage(id++, PetCompassOpenPacket.class, PetCompassOpenPacket::write, PetCompassOpenPacket::read, PetCompassOpenPacket::handle);
        NETWORK_WRAPPER.registerMessage(id++, PetCompassActionPacket.class, PetCompassActionPacket::write, PetCompassActionPacket::read, PetCompassActionPacket::handle);
        NETWORK_WRAPPER.registerMessage(id++, PetCompassForgetRequest.class, PetCompassForgetRequest::write, PetCompassForgetRequest::read, PetCompassForgetRequest::handle);
        NETWORK_WRAPPER.registerMessage(id++, PetCompassDataPacket.class, PetCompassDataPacket::write, PetCompassDataPacket::read, PetCompassDataPacket::handle);
    }

    /** C2S：客户端把消息发往服务端（仅客户端安全调用）。 */
    public static <T> void sendMSGToServer(T message) {
        NETWORK_WRAPPER.sendToServer(message);
    }
    /** S2C 定向发送给单个玩家（罗盘 GUI 数据等回包走这里）。 */
    public static <T> void sendNonLocal(T t, ServerPlayer player) {
        NETWORK_WRAPPER.sendTo(t, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    /** S2C 广播给全服所有在线玩家（1.20 侧遗留的实体 tag 全服广播出口，TameableUtils.sync 已不再使用）。 */
    public static <T> void sendMSGToAll(T message) {
        for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            sendNonLocal(message, player);
        }
    }
}
