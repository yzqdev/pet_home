package com.github.yzqdev.pethome.util;

import net.minecraft.nbt.CompoundTag;

/**
 * 宠物大块数据（附魔/项圈/床坐标/指令等）的载体接口，由 {@code LivingEntityMixin}
 * 实现到全部 LivingEntity 上（存储为 {@code PET_HOME_SAVED_DATA} 数据槽）；
 * {@link CitadelEntityData} 是它的静态门面，勿直接调用。
 */
public interface IPetbedDataEntity {

    /** 当前数据槽内容（1.21 返回槽内同一实例，可原地修改后经 set 写回） */
    CompoundTag getCitadelEntityData();

    /** 整体替换数据槽内容（force 写以触发向追踪者的同步，并失效附魔缓存） */
    void setCitadelEntityData(CompoundTag nbt);
}
