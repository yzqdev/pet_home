package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.misc.PetCompassTeleport;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * C2S 宠物罗盘传送/召回动作（{@code PetCompassScreen} 按钮 → {@code CompassPacket.sendAction}）。
 * 载荷为 {@code PetId} / {@code Action}（to_pet / to_player）NBT；服务端
 * {@link PetCompassTeleport#handleAction} 内部校验档案所有权与配置。
 *
 * @param tag 动作载荷
 */
public record PetCompassActionPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, PetCompassActionPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.TRUSTED_COMPOUND_TAG, PetCompassActionPacket::tag,
                    PetCompassActionPacket::new);
    public static final Type<PetCompassActionPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_compass_action"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 服务端接收器：{@link PetCompassTeleport#handleAction} 内部校验所有权与配置。 */
    public static void handle(PetCompassActionPacket data, ServerPlayNetworking.Context context) {
        PetCompassTeleport.handleAction(context.player(),
                data.tag() == null ? new CompoundTag() : data.tag());
    }
}
