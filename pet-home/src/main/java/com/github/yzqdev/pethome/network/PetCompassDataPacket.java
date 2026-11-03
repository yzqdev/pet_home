package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.PetCompassScreen;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * S2C 罗盘宠物列表数据（服务端 {@code PetCompassTracker} 按主人打包 →
 * {@link PetCompassScreen#handleData} 开屏/原位刷新 GUI，主线程）。
 * 解析与开屏决策在 {@code CompassPacket.parse} / Screen 内。异常 → 断开连接。
 */
public record PetCompassDataPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PetCompassDataPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, PetCompassDataPacket::tag, PetCompassDataPacket::new);
    public static final Type<PetCompassDataPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_compass_data"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 客户端处理（主线程）。异常 → 断连。 */
    public static void handle(final PetCompassDataPacket data, final IPayloadContext context) {
        context.enqueueWork(() -> PetCompassScreen.handleData(data.tag()))
                .exceptionally(e -> {
                    context.disconnect(Component.translatable(LangDefinition.network_failed, e.getMessage()));
                    return null;
                });
    }
}
