package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.misc.PetCompassTeleport;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C2S 宠物罗盘传送/召回动作（{@code PetCompassScreen} 按钮 → {@code CompassPacket.sendAction}）。
 * 载荷为 {@code PetId} / {@code Action}（to_pet / to_player）NBT；服务端
 * {@link PetCompassTeleport#handleAction} 内部校验档案所有权与配置。异常 → 断开发送方连接。
 */
public record PetCompassActionPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PetCompassActionPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, PetCompassActionPacket::tag, PetCompassActionPacket::new);
    public static final Type<PetCompassActionPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_compass_action"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 服务端处理（主线程）。异常 → 断连。 */
    public static void handle(final PetCompassActionPacket data, final IPayloadContext context) {
        context.enqueueWork(() ->
                        PetCompassTeleport.handleAction((net.minecraft.server.level.ServerPlayer) context.player(),
                                data.tag() == null ? new CompoundTag() : data.tag()))
                .exceptionally(e -> {
                    context.disconnect(Component.translatable(LangDefinition.network_failed, e.getMessage()));
                    return null;
                });
    }
}
