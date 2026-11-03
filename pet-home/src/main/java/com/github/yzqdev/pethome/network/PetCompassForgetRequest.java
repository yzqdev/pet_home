package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * C2S 罗盘失效档案删除请求（{@code PetCompassScreen} 删除按钮二次确认 → {@code CompassPacket.sendForget}）。服务端 {@link
 * PetCompassTracker#handleForgetRequest} 会再校验一次档案所有权。
 *
 * @param tag 删除载荷（{@code PetId}）
 */
public record PetCompassForgetRequest(CompoundTag tag) implements CustomPacketPayload {
  public static final StreamCodec<RegistryFriendlyByteBuf, PetCompassForgetRequest> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.TRUSTED_COMPOUND_TAG,
          PetCompassForgetRequest::tag,
          PetCompassForgetRequest::new);
  public static final Type<PetCompassForgetRequest> TYPE =
      new Type<>(ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "pet_compass_forget"));

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  /** 服务端接收器：校验档案所有权后移除并补发列表。 */
  public static void handle(PetCompassForgetRequest data, ServerPlayNetworking.Context context) {
    PetCompassTracker.handleForgetRequest(
        context.player(), data.tag() == null ? new CompoundTag() : data.tag());
  }
}
