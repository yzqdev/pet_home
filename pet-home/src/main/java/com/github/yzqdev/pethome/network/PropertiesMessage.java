package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.PetCompassScreen;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.misc.PetCompassTeleport;
import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import com.github.yzqdev.pethome.util.CitadelEntityData;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author yzqde
 * @date time 2025/1/9 11:32
 * @modified By:
 *
 */
public record PropertiesMessage(String propertyID, CompoundTag compound, int entityID) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PropertiesMessage> STREAM_CODEC  = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PropertiesMessage::propertyID,
            ByteBufCodecs.COMPOUND_TAG, PropertiesMessage::compound,
            ByteBufCodecs.VAR_INT, PropertiesMessage::entityID,
            PropertiesMessage::new
    );
    public static final Type<PropertiesMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_entity_tag"));



    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void handleServer(final PropertiesMessage data, final IPayloadContext context) {

        context.enqueueWork(() -> {
                    // 宠物罗盘：打开请求 / 传送动作（各自在处理方法里验证所有权与配置）
                    if (PHConstants.petCompassOpen.equals(data.propertyID())) {
                        PetCompassTracker.handleOpenRequest((net.minecraft.server.level.ServerPlayer) context.player());
                        return;
                    }
                    if (PHConstants.petCompassAction.equals(data.propertyID())) {
                        PetCompassTeleport.handleAction((net.minecraft.server.level.ServerPlayer) context.player(),
                                data.compound() == null ? new CompoundTag() : data.compound());
                        return;
                    }
                    var level = context.player().level();
                    Entity e = level.getEntity(data.entityID());
                    if (e instanceof LivingEntity && (data.propertyID().equals(PHConstants.entityDataTagUpdate))) {
                        // 鉴权：客户端只能写入自己的宠物，否则伪造包可给任意实体刷附魔/篡改归属
                        java.util.UUID ownerUUID = TameableUtils.getOwnerUUIDOf(e);
                        if (ownerUUID != null && ownerUUID.equals(context.player().getUUID())) {
                            CitadelEntityData.setCitadelTag((LivingEntity) e, data.compound());
                        }
                    }
                })
                .exceptionally(e -> {
                    // Handle exception
                    context.disconnect(Component.translatable(LangDefinition.network_failed, e.getMessage()));
                    return null;
                });
    }
    public static void handleClient(final PropertiesMessage data, final IPayloadContext context) {

        context.enqueueWork(() -> {
                    // 宠物罗盘：服务端打包的宠物列表 → 打开/刷新 GUI
                    if (PHConstants.petCompassData.equals(data.propertyID())) {
                       PetCompassScreen.handleData(data.compound());
                        return;
                    }
                    var compound = data.compound();
                    var entityID = data.entityID();
                    var propertyID = data.propertyID();
                    if (compound != null && Minecraft.getInstance().level != null) {
                        Entity entity = Minecraft.getInstance().level.getEntity(entityID);
                        if ((propertyID.equals(PHConstants.entityDataTagUpdate)) && entity instanceof LivingEntity) {
                            CitadelEntityData.setCitadelTag((LivingEntity) entity, compound);
                        }

                    }
                })
                .exceptionally(e -> {
                    // Handle exception
                    context.disconnect(Component.translatable(LangDefinition.network_failed, e.getMessage()));
                    return null;
                });
    }
}
