package com.github.yzqdev.pethome.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

/**
 * 原 Citadel API 的等价门面：1.21 侧宠物数据存在 LivingEntity 的
 * {@code PET_HOME_SAVED_DATA} CompoundTag 数据槽（由 {@code LivingEntityMixin} 实现
 * {@link IPetbedDataEntity}），本类只是对该槽的静态读写入口（26.1 侧同名为附件实现）。
 * <p>
 * 本侧语义：读取返回的是数据槽内的<b>同一实例</b>（非副本），调用方通常原地改完后
 * 经 {@link #setCitadelTag} force 写回以触发同步；写入方 {@code setCitadelEntityData}
 * 会同步失效实体级附魔缓存（{@link PetSyncDataEntity#ph_getEnchantCache}）。
 */
public class CitadelEntityData {


    /**
     * 返回宠物数据 tag（可能与数据槽同一实例）；实体不是 {@link IPetbedDataEntity}
     * （如非 LivingEntity）时返回<b>脱离实体的一次性空 tag</b>——对它的修改不会生效。
     */
    public static CompoundTag getOrCreateCitadelTag(LivingEntity entity) {
        CompoundTag tag = getCitadelTag(entity);
        return tag == null ? new CompoundTag() : tag;
    }

    /** 同 {@link #getOrCreateCitadelTag}，但不为非数据槽实体构造空 tag */
    public static CompoundTag getCitadelTag(LivingEntity entity) {
        return entity instanceof IPetbedDataEntity ? ((IPetbedDataEntity) entity).getCitadelEntityData() : new CompoundTag();
    }

    /** 写回数据槽（force set：原地改同一引用时普通 set 的等值判断不生效，必须 force 才广播给追踪者） */
    public static void setCitadelTag(LivingEntity entity, CompoundTag tag) {
        if (entity instanceof IPetbedDataEntity) {
            ((IPetbedDataEntity) entity).setCitadelEntityData(tag);
        }
    }
}
