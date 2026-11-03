package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.misc.PetCompassTeleport;
import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * 五通道网络（对齐 NeoForge 1b8ca907 拆分）：C2S 三通道（罗盘开/动作/删档）+ S2C 一通道（罗盘数据）
 * + 双向一通道（citadel tag 单实体更新，保留主人 UUID 鉴权）。
 * 各包负载统一为 {@code CompoundTag}（1.20.1 fabric-api 无 payload 机制，tag 内字段即包语义）；
 * 收包侧直接调用对应 handler（PetCompassTracker / PetCompassTeleport / PetDataTagUpdate）。
 */
public class Networking {

    /** 注册服务端接收器×4（罗盘开/动作/删档/citadel tag 更新）；由 {@code onInitialize} 调用。 */
    public static void initServer() {
        ServerPlayNetworking.registerGlobalReceiver(PHNetworkIds.PET_COMPASS_OPEN, (server, player, handler, buf, rs) ->
                server.execute(() -> PetCompassTracker.handleOpenRequest(player)));
        ServerPlayNetworking.registerGlobalReceiver(PHNetworkIds.PET_COMPASS_ACTION, (server, player, handler, buf, rs) -> {
            CompoundTag tag = readTag(buf);
            server.execute(() -> PetCompassTeleport.handleAction(player, tag));
        });
        ServerPlayNetworking.registerGlobalReceiver(PHNetworkIds.PET_COMPASS_FORGET, (server, player, handler, buf, rs) -> {
            CompoundTag tag = readTag(buf);
            server.execute(() -> PetCompassTracker.handleForgetRequest(player, tag));
        });
        ServerPlayNetworking.registerGlobalReceiver(PHNetworkIds.ENTITY_DATA_TAG_UPDATE, (server, player, handler, buf, rs) -> {
            CompoundTag tag = readTag(buf);
            server.execute(() -> PropertiesMessage.handleEntityTagUpdateFromClient(player, tag));
        });
    }

    /** 注册客户端接收器×2（罗盘数据 / citadel tag 回写；仅客户端环境调用）。 */
    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(PHNetworkIds.PET_COMPASS_DATA, (client, handler, buf, rs) -> {
            CompoundTag tag = readTag(buf);
            client.execute(() -> PropertiesMessage.handleCompassDataFromServer(tag));
        });
        ClientPlayNetworking.registerGlobalReceiver(PHNetworkIds.ENTITY_DATA_TAG_UPDATE, (client, handler, buf, rs) -> {
            CompoundTag tag = readTag(buf);
            client.execute(() -> PropertiesMessage.handleEntityTagUpdateFromServer(tag));
        });
    }

    /** C2S：罗盘打开请求（PetCompassItem 右键触发）。 */
    public static void sendOpenRequest() {
        ClientPlayNetworking.send(PHNetworkIds.PET_COMPASS_OPEN, PacketByteBufs.create());
    }

    /** C2S：罗盘动作（tag：PetId/Action）。 */
    public static void sendCompassAction(CompoundTag tag) {
        ClientPlayNetworking.send(PHNetworkIds.PET_COMPASS_ACTION, writeTag(tag));
    }

    /** C2S：罗盘删除档案（tag：PetId）。 */
    public static void sendCompassForget(CompoundTag tag) {
        ClientPlayNetworking.send(PHNetworkIds.PET_COMPASS_FORGET, writeTag(tag));
    }

    /** C2S：客户端本地改完 citadel tag 后请求服务端校验写回（服务端有所有权校验）。 */
    public static void sendEntityTagUpdate(CompoundTag tag) {
        ClientPlayNetworking.send(PHNetworkIds.ENTITY_DATA_TAG_UPDATE, writeTag(tag));
    }

    /** S2C：罗盘列表数据下发（服务端权威）。 */
    public static void sendCompassData(CompoundTag tag, ServerPlayer player) {
        ServerPlayNetworking.send(player, PHNetworkIds.PET_COMPASS_DATA, writeTag(tag));
    }

    /** S2C：citadel tag 单实体更新向全部在线玩家广播（旧 sendMSGToAll）。 */
    public static void sendEntityTagUpdateToAll(CompoundTag tag) {
        MinecraftServer server = com.github.yzqdev.pethome.platform.ServerRef.get();
        if (server != null) {
            FriendlyByteBuf buf = writeTag(tag);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, PHNetworkIds.ENTITY_DATA_TAG_UPDATE, buf);
            }
        }
    }

    private static FriendlyByteBuf writeTag(CompoundTag tag) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeNbt(tag);
        return buf;
    }

    private static CompoundTag readTag(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        return tag == null ? new CompoundTag() : tag;
    }
}
