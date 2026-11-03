package com.github.yzqdev.pethome.server.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;



/**
 * 宠物床复活请求：宠物死亡且绑定宠物床时由 ServerEvents 生成，存入 {@link PHWorldData}，
 * 携带完整实体 NBT（含原 UUID 与 PetId），次日白天由 PetBedBlockEntity 用 {@code create+load} 复活。
 * 1.20/1.21 手写 POJO（26.1 为 record + Codec）。
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

    /** 按注册名解析实体类型；注册名非法时返回 null（调用方需判空）。 */
    public EntityType getEntityType() {
        return ForgeRegistries.ENTITY_TYPES.getValue( ResourceLocation.parse(this.entityType));
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
