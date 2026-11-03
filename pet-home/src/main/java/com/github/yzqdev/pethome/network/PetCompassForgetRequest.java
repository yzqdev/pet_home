package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C2S 罗盘失效档案删除请求（{@code PetCompassScreen} 删除按钮二次确认 →
 * {@code CompassPacket.sendForget}）。载荷为 {@code PetId}；服务端
 * {@link PetCompassTracker#handleForgetRequest} 会再校验一次所有权。
 */
public class PetCompassForgetRequest {
    public final CompoundTag tag;

    public PetCompassForgetRequest(CompoundTag tag) {
        this.tag = tag;
    }

    public static void write(PetCompassForgetRequest msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.tag);
    }

    public static PetCompassForgetRequest read(FriendlyByteBuf buf) {
        return new PetCompassForgetRequest(buf.readNbt());
    }

    /** 服务端处理（主线程）。 */
    public static void handle(PetCompassForgetRequest msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        ctx.get().enqueueWork(() ->
                PetCompassTracker.handleForgetRequest(ctx.get().getSender(),
                        msg.tag == null ? new CompoundTag() : msg.tag));
    }
}
