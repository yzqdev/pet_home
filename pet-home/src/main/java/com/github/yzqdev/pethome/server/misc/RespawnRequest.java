package com.github.yzqdev.pethome.server.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.logging.Level;

/**
 * 一条待宠物床复活的宠物记录：实体类型 id、死亡所在维度、完整实体存档 NBT、绑定的宠物床坐标、
 * 死亡时间戳与显示名。宠物死亡且绑有宠物床时由 {@code ServerEvent.onLivingDie} 写入
 * {@link PHWorldData}，由 {@code PetBedBlockEntity} 在次日清晨消费（按 NBT 原样重建宠物）。
 */
public class RespawnRequest {
    private String entityType;
    private String dimension;
    private CompoundTag entityData;
    private BlockPos bedPosition;
    private long timestamp;
    private String nametag;

    public RespawnRequest(String entityType, String dimension, CompoundTag entityData, BlockPos bedPosition, long timestamp, String nametag) {
        this.entityType = entityType;
        this.dimension = dimension;
        this.entityData = entityData;
        this.bedPosition = bedPosition;
        this.timestamp = timestamp;
        this.nametag = nametag;
    }

    public String getEntityTypeLoc() {
        return this.entityType;
    }

    public EntityType getEntityType() {
        return BuiltInRegistries.ENTITY_TYPE.get( new ResourceLocation(this.entityType));
    }

    public String getDimension() {
        return dimension;
    }

    public CompoundTag getEntityData() {
        return entityData;
    }

    public BlockPos getBedPosition() {
        return bedPosition;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static String processDimension(ResourceKey<Level> key){
        return key.toString();
    }

    public String getNametag() {
        return this.nametag;
    }

}
