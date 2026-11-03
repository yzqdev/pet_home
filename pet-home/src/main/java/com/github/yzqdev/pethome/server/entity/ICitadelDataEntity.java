package com.github.yzqdev.pethome.server.entity;

import net.minecraft.nbt.CompoundTag;
/**
 * @author Alexthe666
 * @since 1.7.0
 */
/**
 * 持有 citadel tag 的实体能力接口（由 {@code mixin/LivingEntityMixin} 实现：数据存于
 * SynchedEntityData 的 CompoundTag 通道并随 NBT 键 {@code PetHomeEntityData} 持久化）。
 * 经 {@link CitadelEntityData} 门面访问，不要直接 cast。
 */
public interface ICitadelDataEntity {

    CompoundTag getCitadelEntityData();

    void setCitadelEntityData(CompoundTag nbt);
}