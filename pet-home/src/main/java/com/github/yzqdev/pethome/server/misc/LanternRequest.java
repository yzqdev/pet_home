package com.github.yzqdev.pethome.server.misc;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

/**
 * 一条"待往生灯笼收容"请求：跟随态宠物随区块卸载消失时由 {@code ServerEvent.onEntityLeaveWorld} 写入 {@link PHWorldData}（记下宠物
 * UUID/类型/主人/最后坐标/时间与名字）， 由迷途灯笼方块实体领取：在灯笼 64 格内主人名下的请求会被处理，把宠物传送回灯笼旁。 与 26.1 侧不同，1.21 版不带维度字段——灯笼按
 * {@link #chunkPosition} 在灯笼所在维度直接找宠物。
 */
public class LanternRequest {
  private String entityType;
  private long timestamp;
  private String nametag;

  private UUID petUUID;
  private UUID ownerUUID;

  private BlockPos chunkPosition;

  public LanternRequest(
      UUID petUUID,
      String entityType,
      UUID ownerUUID,
      BlockPos chunkPosition,
      long timestamp,
      String nametag) {
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
