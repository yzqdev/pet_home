package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.server.misc.PetCompassTeleport;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C2S 宠物罗盘传送/召回动作（{@code PetCompassScreen} 按钮 → {@code CompassPacket.sendAction}）。
 * 载荷为 {@code PetId} / {@code Action}（to_pet / to_player）NBT；服务端
 * {@link PetCompassTeleport#handleAction} 内部校验档案所有权与配置。
 */
public class PetCompassActionPacket {
    public final CompoundTag tag;

    public PetCompassActionPacket(CompoundTag tag) {
        this.tag = tag;
    }

    public static void write(PetCompassActionPacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.tag);
    }

    public static PetCompassActionPacket read(FriendlyByteBuf buf) {
        return new PetCompassActionPacket(buf.readNbt());
    }

    /** 服务端处理（主线程）。 */
    public static void handle(PetCompassActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        ctx.get().enqueueWork(() ->
                PetCompassTeleport.handleAction(ctx.get().getSender(),
                        msg.tag == null ? new CompoundTag() : msg.tag));
    }
}
