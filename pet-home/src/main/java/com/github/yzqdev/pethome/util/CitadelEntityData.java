package com.github.yzqdev.pethome.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * PETHOME_DATA 附件（{@code PetHomeData} CompoundTag）的读写门面——原分支 Citadel 依赖的等价替代。
 * <p>读走副本改后写回（{@link #getOrCreateCitadelTag} + {@link #setCitadelTag}）；
 * 纯读且需引用比对时用 {@link #getExistingCitadelTagOrNull}。
 */
public class CitadelEntityData {


    /**
     * 取 citadel tag 的<b>副本</b>（未挂附件则返回空 tag）：调用方在副本上的修改不会自动生效，
     * 必须经 {@link #setCitadelTag} 写回——副本语义是 {@code setCitadelTag} 内容判等防 no-op 的前提。
     */
    public static CompoundTag getOrCreateCitadelTag(LivingEntity entity) {
        return getCitadelTag(entity).copy();
    }

    /** 取挂载实例本体（未挂附件返回空 tag，但不写入附件）；不得修改返回对象。 */
    public static CompoundTag getCitadelTag(LivingEntity entity) {
        CompoundTag tag = entity.getExistingDataOrNull(PHAttachments.PETHOME_DATA);
        return tag == null ? new CompoundTag() : tag;
    }


    /** 只读路径：返回当前挂载实例（不拷贝），可能为 {@code null}。供 {@link PetEnchantCache} 做引用比对与缓存查询，调用方不得修改。 */
    @Nullable
    public static CompoundTag getExistingCitadelTagOrNull(LivingEntity entity) {
        return entity.getExistingDataOrNull(PHAttachments.PETHOME_DATA);
    }

    /**
     * 全模组 citadel tag 的唯一写入口。
     * 内容与当前附件相等时直接返回（no-op，不触发同步）；否则 {@code setData} 换成传入实例——
     * NeoForge 对 synced 附件的 setData 会自动把新值广播给所有追踪者（并给新观察者做初始同步）。
     * 副作用：换实例使 {@link PetEnchantCache} 的引用比对失效，下次查询重建缓存。
     * 与 {@link TameableUtils#sync} 的区别：本方法不做客户端→服务端的 C2S 上行。
     */
    public static void setCitadelTag(LivingEntity entity, CompoundTag tag) {
        CompoundTag current = entity.getExistingDataOrNull(PHAttachments.PETHOME_DATA);
        if (current != null && current.equals(tag)) {
            return;
        }
        entity.setData(PHAttachments.PETHOME_DATA, tag);
    }
}
