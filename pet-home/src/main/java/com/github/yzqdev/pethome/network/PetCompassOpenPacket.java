package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C2S 宠物罗盘「打开列表」请求（无载荷，单例）。客户端 {@code PetCompassItem} 右键 /
 * {@code CompassPacket.sendOpen} 发出；服务端 {@link PetCompassTracker#handleOpenRequest} 按主人打包宠物列表回包。
 */
public class PetCompassOpenPacket {
    public static final PetCompassOpenPacket INSTANCE = new PetCompassOpenPacket();

    private PetCompassOpenPacket() {}

    public static void write(PetCompassOpenPacket msg, FriendlyByteBuf buf) {
    }

    public static PetCompassOpenPacket read(FriendlyByteBuf buf) {
        return INSTANCE;
    }

    /** 服务端处理（主线程）：打包宠物列表回 S2C {@link PetCompassDataPacket}。 */
    public static void handle(PetCompassOpenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        ctx.get().enqueueWork(() ->
                PetCompassTracker.handleOpenRequest(ctx.get().getSender()));
    }
}
