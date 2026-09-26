package com.github.yzqdev.pethome.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * 由 {@code LivingEntityMixin} 实现的宠物高频数据访问接口：
 * 治疗光环/冰冻计时器走独立 int 数据槽（vanilla 只同步变化的小整数，
 * 不再为每 tick 的计数器改动重发整个 citadel CompoundTag）；
 * 附魔等级走实体级缓存，热路径不再反复扫描 NBT 列表。
 */
public interface PetSyncDataEntity {

    int ph_getHealingAuraTime();

    void ph_setHealingAuraTime(int time);

    int ph_getFrozenTime();

    void ph_setFrozenTime(int time);

    /**
     * 取该实体当前 tag 的附魔等级缓存；实现方负责在 tag 变化时重建。
     *
     * @param currentTag 调用方取到的当前 citadel tag（用于引用比对判断是否失效）
     */
    Map<ResourceLocation, Integer> ph_getEnchantCache(@Nullable CompoundTag currentTag);
}
