package com.github.yzqdev.pethome.network;


import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.util.CitadelEntityData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;


public record PropertiesMessage(String propertyID, CompoundTag compound, int entityID) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, PropertiesMessage> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, PropertiesMessage::propertyID,
                    ByteBufCodecs.TRUSTED_COMPOUND_TAG, PropertiesMessage::compound,
                    ByteBufCodecs.INT, PropertiesMessage::entityID, PropertiesMessage::new);
    public static final Type<PropertiesMessage> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_entity_tag"));


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(PropertiesMessage data, ServerPlayNetworking.Context context) {
        // 宠物罗盘：打开请求 / 传送动作（各自在处理方法里验证所有权与配置）
        if (PHConstants.petCompassOpen.equals(data.propertyID())) {
            com.github.yzqdev.pethome.server.misc.PetCompassTracker.handleOpenRequest(context.player());
            return;
        }
        if (PHConstants.petCompassAction.equals(data.propertyID())) {
            com.github.yzqdev.pethome.server.misc.PetCompassTeleport.handleAction(context.player(),
                    data.compound() == null ? new CompoundTag() : data.compound());
            return;
        }
        context.server()
                .execute(() -> {
                    var level = context.player()
                            .level();
                    Entity e = level.getEntity(data.entityID());
                    if (e instanceof LivingEntity living && (data.propertyID()
                            .equals(PHConstants.entityDataTagUpdate))) {
                        // 鉴权：客户端只能写入自己的宠物，否则伪造包可给任意实体刷附魔/篡改归属
                        java.util.UUID ownerUUID = com.github.yzqdev.pethome.util.TameableUtils.getOwnerUUIDOf(e);
                        if (ownerUUID != null && ownerUUID.equals(context.player().getUUID())) {
                            CitadelEntityData.setCitadelTag(living, data.compound());
                        }
                    }
                });

    }

    public static void handleClient(PropertiesMessage data, ClientPlayNetworking.Context context) {
        // 宠物罗盘：服务端打包的宠物列表 → 打开/刷新 GUI
        if (PHConstants.petCompassData.equals(data.propertyID())) {
            com.github.yzqdev.pethome.client.PetCompassScreen.handleData(data.compound());
            return;
        }
        context.client().execute(() -> {
            var compound = data.compound();
            var entityID = data.entityID();
            var propertyID = data.propertyID();
            if (compound != null && Minecraft.getInstance().level != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(entityID);
                if ((propertyID.equals(PHConstants.entityDataTagUpdate)) && entity instanceof LivingEntity living) {
                    CitadelEntityData.setCitadelTag(living, compound);
                }

            }
        });
    }
}
