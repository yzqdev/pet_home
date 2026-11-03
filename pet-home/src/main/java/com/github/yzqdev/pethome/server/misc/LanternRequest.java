package com.github.yzqdev.pethome.server.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * 灯笼收容请求条目：宠物随区块卸载消失（{@code ServerEvent.onEntityLeaveWorld} 且
 * {@code shouldUnloadToLantern} 为真）时写入 {@code PHWorldData}，由任意
 * {@code WaywardLanternBlockEntity} 消费——按 {@link #dimension} 去宠物实际所在维度找回。
 * Codec 里 Dimension 为可选字段（{@code PHWorldData.LANTERN_REQUEST_CODEC}），
 * 旧档缺失时按主世界处理。
 */
public class LanternRequest {
    private String entityType;
    private long timestamp;
    private String nametag;

    private UUID petUUID;
    private UUID ownerUUID;

    private BlockPos chunkPosition;

    /** 宠物卸载时所在的维度（如 minecraft:the_nether）：灯笼要到这个维度找实体，而不是在自己维度里空找 */
    private String dimension;

    public LanternRequest(UUID petUUID, String entityType, UUID ownerUUID, BlockPos chunkPosition, long timestamp, String nametag, String dimension) {
        this.petUUID = petUUID;
        this.entityType = entityType;
        this.chunkPosition = chunkPosition;
        this.ownerUUID = ownerUUID;
        this.timestamp = timestamp;
        this.nametag = nametag;
        this.dimension = dimension;
    }

    public UUID getPetUUID() {
        return petUUID;
    }

    public String getEntityTypeLoc() {
        return this.entityType;
    }

    public EntityType getEntityType() {
        return BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(this.entityType));
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getNametag() {
        return this.nametag;
    }

    public String getDimension() {
        return this.dimension;
    }

    /** 收容时的维度 id → {@code ResourceKey<Level>}（灯笼跨维度找宠物用）。 */
    public ResourceKey<Level> getDimensionKey() {
        return ResourceKey.create(Registries.DIMENSION, Identifier.parse(this.dimension));
    }

    public BlockPos getChunkPosition() {
        return chunkPosition;
    }

    public String toString() {
        if (getNametag() == null || getNametag().isEmpty()) {
            return this.entityType;
        } else {
            return getNametag() + "|" + this.entityType;
        }
    }
}
