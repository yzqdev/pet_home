package com.github.yzqdev.pethome.network;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.util.CitadelEntityData;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

/**
 * 双向宠物 citadel tag 同步（{@code PetDataSync.sync} 唯一出口）。
 * <ul>
 *   <li>C2S：客户端把本地预测改动回送服务端，服务端有鉴权——目标实体的主人 UUID
 *       必须等于发包玩家，否则丢弃（防伪造包改任意实体 NBT）；</li>
 *   <li>S2C：客户端本地写附件（渲染/tooltip 用，信任服务端已校验）。</li>
 * </ul>
 * Fabric play 接收器已在主线程执行，处理逻辑直接分发（与 NF 的 enqueueWork 语义一致）。
 *
 * @param entityID 目标实体网络 id
 * @param tag      citadel tag 数据
 */
public record EntityDataTagUpdatePacket(int entityID, CompoundTag tag) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityDataTagUpdatePacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, EntityDataTagUpdatePacket::entityID,
                    ByteBufCodecs.TRUSTED_COMPOUND_TAG, EntityDataTagUpdatePacket::tag,
                    EntityDataTagUpdatePacket::new);
    public static final Type<EntityDataTagUpdatePacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_entity_tag_update"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 服务端接收器：鉴权后写 citadel tag——仅当目标实体主人 UUID 与发包玩家一致才放行
     * （防伪造包给任意实体刷附魔/篡改归属）。
     */
    public static void handleServer(EntityDataTagUpdatePacket data, ServerPlayNetworking.Context context) {
        var level = context.player().level();
        Entity e = level.getEntity(data.entityID());
        if (e instanceof LivingEntity living) {
            java.util.UUID ownerUUID = TameableUtils.getOwnerUUIDOf(e);
            if (ownerUUID != null && ownerUUID.equals(context.player().getUUID())) {
                CitadelEntityData.setCitadelTag(living, data.tag());
            }
        }
    }

    /** 客户端接收器：本地写实体附件（渲染/tooltip 用，信任服务端已校验）。 */
    @Environment(EnvType.CLIENT)
    public static void handleClient(EntityDataTagUpdatePacket data, ClientPlayNetworking.Context context) {
        CompoundTag tag = data.tag();
        if (tag != null && Minecraft.getInstance().level != null) {
            Entity entity = Minecraft.getInstance().level.getEntity(data.entityID());
            if (entity instanceof LivingEntity living) {
                CitadelEntityData.setCitadelTag(living, tag);
            }
        }
    }
}
