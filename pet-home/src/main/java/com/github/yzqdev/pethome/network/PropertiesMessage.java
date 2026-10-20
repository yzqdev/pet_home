package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author yzqde
 * @date time 2025/1/9 11:32
 * @modified By:
 *
 */
/**
 * 全 mod 唯一的双向 payload：{@code propertyID} 分发 4 个逻辑通道——
 * entityDataTagUpdate（宠物 tag 客户端↔服务端互写）/ petCompassOpen / petCompassAction（C2S）/
 * petCompassData（S2C 罗盘列表）。{@code entityID} 仅 entityDataTagUpdate 使用。
 */
public record PropertiesMessage(String propertyID, CompoundTag compound, int entityID) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PropertiesMessage> STREAM_CODEC  = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PropertiesMessage::propertyID,
            ByteBufCodecs.COMPOUND_TAG, PropertiesMessage::compound,
            ByteBufCodecs.VAR_INT, PropertiesMessage::entityID,
            PropertiesMessage::new
    );
    public static final Type<PropertiesMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "pet_entity_tag"));



    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    /**
     * C2S 分发（enqueueWork 主线程执行）：罗盘开/动作两通道各自校验所有权与配置；
     * entityDataTagUpdate 必须校验实体归属为发包玩家后才写 citadel tag（防伪造包篡改任意实体）。
     * 任一异常经 exceptionally 断开连接（LangDefinition.network_failed）。
     */
    public static void handleServer(final PropertiesMessage data, final IPayloadContext context) {

        context.enqueueWork(() -> {
                    // 宠物罗盘：打开请求 / 传送动作（各自在处理方法里验证所有权与配置）
                    if (PHConstants.petCompassOpen.equals(data.propertyID())) {
                        com.github.yzqdev.pethome.server.misc.PetCompassTracker.handleOpenRequest((net.minecraft.server.level.ServerPlayer) context.player());
                        return;
                    }
                    if (PHConstants.petCompassAction.equals(data.propertyID())) {
                        com.github.yzqdev.pethome.server.misc.PetCompassTeleport.handleAction((net.minecraft.server.level.ServerPlayer) context.player(),
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
                    // 吞掉反序列化异常：损坏的旧档数据按无数据处理
                    context.disconnect(Component.translatable(LangDefinition.network_failed, e.getMessage()));
                    return null;
                });
    }
    /**
     * S2C 分发：petCompassData 交给 PetCompassScreen 开/刷 GUI；
     * entityDataTagUpdate 在客户端本地写实体数据槽（渲染/tooltip 用，服务端不可信其即时性）。
     * 异常同样断连（保持两端语义一致）。
     */
    public static void handleClient(final PropertiesMessage data, final IPayloadContext context) {

        context.enqueueWork(() -> {
                    // 宠物罗盘：服务端打包的宠物列表 → 打开/刷新 GUI
                    if (PHConstants.petCompassData.equals(data.propertyID())) {
                        com.github.yzqdev.pethome.client.PetCompassScreen.handleData(data.compound());
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
                    // 吞掉反序列化异常：损坏的旧档数据按无数据处理
                    context.disconnect(Component.translatable(LangDefinition.network_failed, e.getMessage()));
                    return null;
                });
    }
}
