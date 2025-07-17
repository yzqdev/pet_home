package com.github.yzqdev.pet_home.server.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;


import java.util.UUID;
import java.util.logging.Level;

public class LanternRequest {
    private String entityType;
    private long timestamp;
    private String nametag;

    private UUID petUUID;
    private UUID ownerUUID;

    private BlockPos chunkPosition;

    public LanternRequest(UUID petUUID, String entityType, UUID ownerUUID, BlockPos chunkPosition, long timestamp, String nametag) {
        this.petUUID = petUUID;
        this.entityType = entityType;
        this.chunkPosition = chunkPosition;
        this.ownerUUID = ownerUUID;
        this.timestamp = timestamp;
        this.nametag = nametag;
    }

    public UUID getPetUUID() {
        return petUUID;
    }

    public String getEntityTypeLoc() {
        return this.entityType;
    }

    public EntityType getEntityType() {
        return BuiltInRegistries.ENTITY_TYPE.get( ResourceLocation.parse(this.entityType));
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
