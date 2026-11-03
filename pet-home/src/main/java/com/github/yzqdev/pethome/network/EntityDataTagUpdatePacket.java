package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.server.entity.CitadelEntityData;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 双向宠物 citadel tag 同步（{@code PetDataSync.sync} 唯一出口）。
 * C2S：客户端把本地预测改动回送服务端，服务端有鉴权——目标实体的主人 UUID 必须等于发包玩家，
 * 否则丢弃（防伪造包改任意实体 NBT）。S2C：客户端本地写数据槽（经 {@link Networking#PROXY}，
 * 避免专用服务器加载客户端类）。
 */
public class EntityDataTagUpdatePacket {
    public final int entityID;
    public final CompoundTag tag;

    public EntityDataTagUpdatePacket(int entityID, CompoundTag tag) {
        this.entityID = entityID;
        this.tag = tag;
    }

    public static void write(EntityDataTagUpdatePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityID);
        buf.writeNbt(msg.tag);
    }

    public static EntityDataTagUpdatePacket read(FriendlyByteBuf buf) {
        return new EntityDataTagUpdatePacket(buf.readInt(), buf.readNbt());
    }

    /**
     * 双端分发（主线程）：服务端鉴权后写 citadel tag（仅目标实体主人 = 发包玩家才放行）；
     * 客户端经 PROXY 本地写数据槽（渲染/tooltip 用，信任服务端已校验）。
     */
    public static void handle(EntityDataTagUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                Networking.PROXY.handleEntityTagUpdate(msg.tag, msg.entityID);
                return;
            }
            Entity e = ctx.get().getSender().level().getEntity(msg.entityID);
            if (e instanceof LivingEntity) {
                // 鉴权：客户端只能写入自己的宠物，否则伪造包可给任意实体刷附魔/篡改归属
                UUID ownerUUID = TameableUtils.getOwnerUUIDOf(e);
                if (ownerUUID != null && ownerUUID.equals(ctx.get().getSender().getUUID())) {
                    CitadelEntityData.setCitadelTag((LivingEntity) e, msg.tag);
                }
            }
        });
    }
}
