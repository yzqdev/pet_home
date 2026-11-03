package com.github.yzqdev.pethome.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

/**
 * 宠物数据（"citadel 数据"）的读写门面。实际存储由 {@link IPetbedDataEntity} 提供—— {@code LivingEntityMixin} 给所有 {@code
 * LivingEntity} 注入一个 SynchedEntityData 的 COMPOUND_TAG 条目并实现该接口，因此数据随存档持久化、随实体追踪同步到客户端。 本类对所有非
 * LivingEntity 的实体静默退化为空 tag（不写入）。
 */
public class CitadelEntityData {

  /** 取实体的 citadel tag，无则返回空 {@link CompoundTag}（调用方修改后必须 {@link #setCitadelTag} 写回才生效）。 */
  public static CompoundTag getOrCreateCitadelTag(LivingEntity entity) {
    CompoundTag tag = getCitadelTag(entity);
    return tag == null ? new CompoundTag() : tag;
  }

  /** 取实体的 citadel tag；实体不是注入目标（非 {@code LivingEntity} 或不可转换）时返回新空 tag。 */
  public static CompoundTag getCitadelTag(LivingEntity entity) {
    return entity instanceof IPetbedDataEntity
        ? ((IPetbedDataEntity) entity).getCitadelEntityData()
        : new CompoundTag();
  }

  /** 整体写回 citadel tag（覆盖语义）；写 SynchedEntityData 条目会自动同步给追踪该实体的客户端。 */
  public static void setCitadelTag(LivingEntity entity, CompoundTag tag) {
    if (entity instanceof IPetbedDataEntity) {
      ((IPetbedDataEntity) entity).setCitadelEntityData(tag);
    }
  }
}
