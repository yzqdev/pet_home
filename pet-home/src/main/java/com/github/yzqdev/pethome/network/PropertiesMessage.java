package com.github.yzqdev.pethome.network;



import com.github.yzqdev.pethome.ModConstants;
import com.github.yzqdev.pethome.server.entity.CitadelEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

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

    public static void handleServer(final PropertiesMessage message, ServerPlayer sender) {
        // 宠物罗盘：打开请求 / 传送动作（各自在处理方法里验证所有权与配置）
        if (ModConstants.petCompassOpen.equals(message.propertyID)) {
            com.github.yzqdev.pethome.server.misc.PetCompassTracker.handleOpenRequest(sender);
            return;
        }
        if (ModConstants.petCompassAction.equals(message.propertyID)) {
            com.github.yzqdev.pethome.server.misc.PetCompassTeleport.handleAction(sender,
                    message.compound == null ? new CompoundTag() : message.compound);
            return;
        }
        Entity e = sender.level().getEntity(message.entityID);
        if (e instanceof LivingEntity && (message.propertyID.equals(ModConstants.entityDataTagUpdate))) {
            // 鉴权：客户端只能写入自己的宠物，否则伪造包可给任意实体刷附魔/篡改归属
            java.util.UUID ownerUUID = com.github.yzqdev.pethome.server.entity.TameableUtils.getOwnerUUIDOf(e);
            if (ownerUUID != null && ownerUUID.equals(sender.getUUID())) {
                CitadelEntityData.setCitadelTag((LivingEntity) e, message.compound);
            }
        }
    }


    public static void handleClient(final PropertiesMessage message) {
        // 宠物罗盘：服务端打包的宠物列表 → 打开/刷新 GUI
        if (ModConstants.petCompassData.equals(message.propertyID)) {
            com.github.yzqdev.pethome.client.PetCompassScreen.handleData(message.compound);
            return;
        }
        if (message.compound == null) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Entity entity = player.level().getEntity(message.entityID);
        if (ModConstants.entityDataTagUpdate.equals(message.propertyID) && entity instanceof LivingEntity living) {
            CitadelEntityData.setCitadelTag(living, message.compound);
        }
    }
}
