package com.github.yzqdev.pethome.util;

import net.minecraft.nbt.CompoundTag;

/**
 * 宠物数据载体的注入点接口：由 {@code mixin/LivingEntityMixin} 实现到全部 {@code LivingEntity} 上， 背后是 {@code
 * SynchedEntityData} 的 COMPOUND_TAG 条目（随存档持久化、随实体追踪同步）。 业务代码不要直接调用，统一走 {@link CitadelEntityData}
 * 门面。
 */
public interface IPetbedDataEntity {

  /** 读实体当前的宠物数据 tag；实现方保证永不返回 {@code null}。 */
  CompoundTag getCitadelEntityData();

  /** 整体覆盖写入宠物数据 tag（写 SynchedEntityData 条目即触发客户端同步）。 */
  void setCitadelEntityData(CompoundTag nbt);
}
