package com.github.yzqdev.pethome.server.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * 一条迷途灯笼收容请求：宠物（按实体 UUID）随区块卸载离开世界时由 {@code ServerEvent.onEntityLeaveWorld}
 * 写入 {@link PHWorldData}，等待 64 格内的迷途灯笼找回。除类型 / 时间戳 / 名字外，还记录
 * 宠物与主人 UUID、卸载时的坐标与<strong>所在维度</strong>（{@link #getDimensionKey()}，灯笼跨维度定位实体用）。
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
        return BuiltInRegistries.ENTITY_TYPE.get( new ResourceLocation(this.entityType));
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

    public ResourceKey<Level> getDimensionKey() {
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation(this.dimension));
    }

    public BlockPos getChunkPosition() {
        return chunkPosition;
    }

    public String toString(){
        if(getNametag() == null || getNametag().isEmpty()){
            return this.entityType;
        }else{
            return getNametag() + "|" + this.entityType;
        }
    }
}
