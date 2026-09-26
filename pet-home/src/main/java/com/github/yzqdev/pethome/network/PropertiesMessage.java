package com.github.yzqdev.pethome.network;



import com.github.yzqdev.pethome.ModConstants;
import com.github.yzqdev.pethome.server.entity.CitadelEntityData;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PropertiesMessage {
    private String propertyID;
    private CompoundTag compound;
    private int entityID;

    public PropertiesMessage(String propertyID, CompoundTag compound, int entityID) {
        this.propertyID = propertyID;
        this.compound = compound;
        this.entityID = entityID;
    }

    public static void write(PropertiesMessage message, FriendlyByteBuf packetBuffer) {
        packetBuffer.writeUtf(message.propertyID);
        packetBuffer.writeNbt(message.compound);
        packetBuffer.writeInt(message.entityID);
    }

    public static PropertiesMessage read(FriendlyByteBuf packetBuffer) {
        return new PropertiesMessage(packetBuffer.readUtf(), packetBuffer.readNbt(), packetBuffer.readInt());
    }

    public static class Handler {

        public static void handle(final PropertiesMessage message, Supplier<NetworkEvent.Context> context) {
            context.get().setPacketHandled(true);
            context.get().enqueueWork(() -> {
                if (context.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                    // 宠物罗盘：服务端打包的宠物列表 → 打开/刷新 GUI
                    if (ModConstants.petCompassData.equals(message.propertyID)) {
                        com.github.yzqdev.pethome.client.PetCompassScreen.handleData(message.compound);
                        return;
                    }
                   Networking.PROXY.handlePropertiesPacket(message.propertyID, message.compound, message.entityID);
                } else {
                    // 宠物罗盘：打开请求 / 传送动作（各自在处理方法里验证所有权与配置）
                    if (ModConstants.petCompassOpen.equals(message.propertyID)) {
                        com.github.yzqdev.pethome.server.misc.PetCompassTracker.handleOpenRequest(context.get().getSender());
                        return;
                    }
                    if (ModConstants.petCompassAction.equals(message.propertyID)) {
                        com.github.yzqdev.pethome.server.misc.PetCompassTeleport.handleAction(context.get().getSender(),
                                message.compound == null ? new CompoundTag() : message.compound);
                        return;
                    }
                    Entity e = context.get().getSender().level().getEntity(message.entityID);
                    if (e instanceof LivingEntity && (  message.propertyID.equals(ModConstants.entityDataTagUpdate))) {
                        // 鉴权：客户端只能写入自己的宠物，否则伪造包可给任意实体刷附魔/篡改归属
                        UUID ownerUUID = TameableUtils.getOwnerUUIDOf(e);
                        if (ownerUUID != null && ownerUUID.equals(context.get().getSender().getUUID())) {
                            CitadelEntityData.setCitadelTag((LivingEntity) e, message.compound);
                        }
                    }
                }
            });
        }
    }
}