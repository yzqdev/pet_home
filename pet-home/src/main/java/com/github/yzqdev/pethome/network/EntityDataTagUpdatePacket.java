package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.util.CitadelEntityData;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 双向宠物 citadel tag 同步（{@code PetDataSync.sync} 唯一出口）。
 * C2S：客户端把本地预测改动回送服务端，服务端有鉴权——目标实体的主人 UUID 必须等于发包玩家，
 * 否则丢弃（防伪造包改任意实体 NBT）。S2C：客户端本地写附件（渲染/tooltip 用）。
 * 处理在主线程执行，异常 → 断开发送方连接（{@code LangDefinition.network_failed}）。
 */
public record EntityDataTagUpdatePacket(int entityID, CompoundTag tag) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, EntityDataTagUpdatePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, EntityDataTagUpdatePacket::entityID,
                    ByteBufCodecs.COMPOUND_TAG, EntityDataTagUpdatePacket::tag,
                    EntityDataTagUpdatePacket::new);
    public static final Type<EntityDataTagUpdatePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_entity_tag_update"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * C2S 分发（主线程）：鉴权后写 citadel tag——仅当目标实体主人 UUID 与发包玩家一致才放行。
     * 异常 → 断开该玩家连接。
     */
    public static void handleServer(final EntityDataTagUpdatePacket data, final IPayloadContext context) {
        context.enqueueWork(() -> {
                    var level = context.player().level();
                    Entity e = level.getEntity(data.entityID());
                    if (e instanceof LivingEntity) {
                        java.util.UUID ownerUUID = TameableUtils.getOwnerUUIDOf(e);
                        if (ownerUUID != null && ownerUUID.equals(context.player().getUUID())) {
                            CitadelEntityData.setCitadelTag((LivingEntity) e, data.tag());
                        }
                    }
                })
                .exceptionally(e -> {
                    context.disconnect(Component.translatable(LangDefinition.network_failed, e.getMessage()));
                    return null;
                });
    }

    /** S2C 分发（客户端主线程）：本地写实体附件（渲染/tooltip 用，信任服务端已校验）。异常 → 断连。 */
    public static void handleClient(final EntityDataTagUpdatePacket data, final IPayloadContext context) {
        context.enqueueWork(() -> {
                    CompoundTag tag = data.tag();
                    if (tag != null && Minecraft.getInstance().level != null) {
                        Entity entity = Minecraft.getInstance().level.getEntity(data.entityID());
                        if (entity instanceof LivingEntity) {
                            CitadelEntityData.setCitadelTag((LivingEntity) entity, tag);
                        }
                    }
                })
                .exceptionally(e -> {
                    context.disconnect(Component.translatable(LangDefinition.network_failed, e.getMessage()));
                    return null;
                });
    }
}
