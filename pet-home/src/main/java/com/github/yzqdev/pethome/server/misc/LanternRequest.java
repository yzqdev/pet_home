package com.github.yzqdev.pethome.server.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * 迷途灯笼召回请求：跟随状态宠物随区块卸载时由 ServerEvent.onEntityLeaveWorld 写入
 * {@link PHWorldData}（含宠物/主人 UUID、最后位置、时间、名字、所在维度）。
 * 灯笼方块实体拉取后到<b>请求维度</b>强载区块找回实体并传回灯笼旁；
 * 实体重新 join 世界时按 petUUID 清除。
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
        return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(this.entityType));
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

    /** 存档字符串 → 维度 ResourceKey（跨维度查找实体/强载区块时用） */
    public ResourceKey<Level> getDimensionKey() {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(this.dimension));
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
