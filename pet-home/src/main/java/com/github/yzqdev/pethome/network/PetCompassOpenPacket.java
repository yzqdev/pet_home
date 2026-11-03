package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * C2S 宠物罗盘「打开列表」请求（无载荷，单例）。客户端 {@code PetCompassItem} 右键 / {@code CompassPacket.sendOpen} 发出；服务端
 * {@link PetCompassTracker#handleOpenRequest} 按主人打包宠物列表回 S2C {@link PetCompassDataPacket}。
 */
public record PetCompassOpenPacket() implements CustomPacketPayload {
  public static final PetCompassOpenPacket INSTANCE = new PetCompassOpenPacket();
  public static final StreamCodec<RegistryFriendlyByteBuf, PetCompassOpenPacket> STREAM_CODEC =
      StreamCodec.unit(INSTANCE);
  public static final Type<PetCompassOpenPacket> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "pet_compass_open"));

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  /** 服务端接收器：打包宠物列表回 S2C {@link PetCompassDataPacket}。 */
  public static void handle(PetCompassOpenPacket data, ServerPlayNetworking.Context context) {
    PetCompassTracker.handleOpenRequest(context.player());
  }
}
