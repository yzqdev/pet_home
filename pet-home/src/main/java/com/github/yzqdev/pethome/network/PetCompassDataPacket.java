package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.client.PetCompassScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C 罗盘宠物列表数据（服务端 {@code PetCompassTracker} 按主人打包 →
 * {@link PetCompassScreen#handleData} 开屏/原位刷新 GUI，主线程）。
 * 与原 PropertiesMessage 一致：直接引用客户端 Screen 类（原 Handler 已有此先例）。
 */
public class PetCompassDataPacket {
    public final CompoundTag tag;

    public PetCompassDataPacket(CompoundTag tag) {
        this.tag = tag;
    }

    public static void write(PetCompassDataPacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.tag);
    }

    public static PetCompassDataPacket read(FriendlyByteBuf buf) {
        return new PetCompassDataPacket(buf.readNbt());
    }

    /** 客户端处理（主线程）。 */
    public static void handle(PetCompassDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        ctx.get().enqueueWork(() -> PetCompassScreen.handleData(msg.tag));
    }
}
