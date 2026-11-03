package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C2S 罗盘失效档案删除请求（{@code PetCompassScreen} 删除按钮二次确认 →
 * {@code CompassPacket.sendForget}）。载荷为 {@code PetId}；服务端
 * {@link PetCompassTracker#handleForgetRequest} 会再校验一次所有权。异常 → 断开发送方连接。
 */
public record PetCompassForgetRequest(CompoundTag tag) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PetCompassForgetRequest> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, PetCompassForgetRequest::tag, PetCompassForgetRequest::new);
    public static final Type<PetCompassForgetRequest> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "pet_compass_forget"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 服务端处理（主线程）。异常 → 断连。 */
    public static void handle(final PetCompassForgetRequest data, final IPayloadContext context) {
        context.enqueueWork(() ->
                        PetCompassTracker.handleForgetRequest((net.minecraft.server.level.ServerPlayer) context.player(),
                                data.tag() == null ? new CompoundTag() : data.tag()))
                .exceptionally(e -> {
                    context.disconnect(Component.translatable(LangDefinition.network_failed, e.getMessage()));
                    return null;
                });
    }
}
