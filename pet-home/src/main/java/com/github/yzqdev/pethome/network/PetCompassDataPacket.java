package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.PetCompassScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * S2C 罗盘宠物列表数据（服务端 {@code PetCompassTracker} 按主人打包 →
 * {@link PetCompassScreen#handleData} 开屏/原位刷新 GUI）。
 * 解析与开屏决策在 {@code CompassPacket.parse} / Screen 内。
 *
 * @param tag 宠物列表载荷
 */
public record PetCompassDataPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, PetCompassDataPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.TRUSTED_COMPOUND_TAG, PetCompassDataPacket::tag,
                    PetCompassDataPacket::new);
    public static final Type<PetCompassDataPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_compass_data"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 客户端接收器：开屏/原位刷新罗盘 GUI。 */
    @Environment(EnvType.CLIENT)
    public static void handle(PetCompassDataPacket data, ClientPlayNetworking.Context context) {
        PetCompassScreen.handleData(data.tag());
    }
}
