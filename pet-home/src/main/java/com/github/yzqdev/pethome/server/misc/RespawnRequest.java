package com.github.yzqdev.pethome.server.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.logging.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

/**
 * 一条"待宠物床复活"请求：宠物死亡时由 {@code ServerEvent.onLivingDie} 把完整实体 NBT （{@link #entityData}，含原 Entity
 * UUID，复活后罗盘靠它重新绑定档案）、实体类型、宠物床坐标 （{@link #bedPosition}，含维度字符串）与死亡时间存入 {@link PHWorldData}； 次日清晨由
 * {@code PetBedBlockEntity} 消费并按 NBT 重建实体。
 *
 * @param entityType 实体类型的字符串 id（如 {@code minecraft:wolf}）
 * @param dimension 宠物床所在维度的字符串 id
 * @param entityData 完整实体存档 NBT（复活流据此还原宠物）
 * @param nametag 自定义名（无则为空串）
 */
public record RespawnRequest(
    String entityType,
    String dimension,
    CompoundTag entityData,
    BlockPos bedPosition,
    long timestamp,
    String nametag) {

  public static final Codec<RespawnRequest> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.STRING.fieldOf("entity_type").forGetter(RespawnRequest::entityType),
                      Codec.STRING.fieldOf("dimension").forGetter(RespawnRequest::dimension),
                      CompoundTag.CODEC
                          .fieldOf("entity_data")
                          .forGetter(RespawnRequest::entityData),
                      BlockPos.CODEC.fieldOf("bed_position").forGetter(RespawnRequest::bedPosition),
                      Codec.LONG.fieldOf("timestamp").forGetter(RespawnRequest::timestamp),
                      Codec.STRING.fieldOf("nametag").forGetter(RespawnRequest::nametag))
                  .apply(instance, RespawnRequest::new));

  /** 解析 {@link #entityType} 为实体类型（id 非法时回退为猪）。 */
  public EntityType<?> getEntityType() {
    return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(this.entityType));
  }

  /** 维度注册键 → 字符串 id 的统一序列化辅助。 */
  public static String processDimension(ResourceKey<Level> key) {
    return key.location().toString();
  }
}
