package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C2S 宠物罗盘「打开列表」请求（无载荷，单例）。客户端 {@code PetCompassItem} 右键 /
 * {@code CompassPacket.sendOpen} 发出；服务端 {@link PetCompassTracker#handleOpenRequest} 按主人打包宠物列表回包。
 * 异常 → 断开发送方连接（{@code LangDefinition.network_failed}）。
 */
public record PetCompassOpenPacket() implements CustomPacketPayload {
    public static final PetCompassOpenPacket INSTANCE = new PetCompassOpenPacket();
    public static final StreamCodec<FriendlyByteBuf, PetCompassOpenPacket> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);
    public static final Type<PetCompassOpenPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_compass_open"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 服务端处理（主线程）：打包宠物列表回 S2C {@link PetCompassDataPacket}。异常 → 断连。 */
    public static void handle(final PetCompassOpenPacket data, final IPayloadContext context) {
        context.enqueueWork(() ->
                        PetCompassTracker.handleOpenRequest((net.minecraft.server.level.ServerPlayer) context.player()))
                .exceptionally(e -> {
                    context.disconnect(Component.translatable(LangDefinition.network_failed, e.getMessage()));
                    return null;
                });
    }
}
