package com.github.yzqdev.pethome.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Citadel 遗产的 26.1 形态：宠物数据（{@code PETHOME_DATA} 附件）的读写门面。
 * 所有宠物状态键（附魔列表、宠物床、冷却等，见 {@code TameableUtils}）都经由本类存取。
 *
 * <p><b>副本语义</b>：{@link #getOrCreateCitadelTag} 返回的是附件内容的<b>副本</b>，
 * 原地修改不生效，改完必须调 {@link #setCitadelTag} 写回（写回时按内容相等去重，
 * 避免无效写入触发无谓的附件同步）。不要为省一次拷贝去用 {@link #getCitadelTag}
 * 直接改内部实例——那会绕过去重并破坏同步语义。纯读且需引用比对时用 {@link #getExistingCitadelTagOrNull}。</p>
 */
public class CitadelEntityData {


    /**
     * Returns a copy of the stored tag: mutations on the returned tag are NOT visible
     * to the attachment until written back through {@link #setCitadelTag}.
     * Handing out the internal instance instead would let callers mutate it in place,
     * and setCitadelTag could then no longer detect no-op writes by comparing content.
     */
    public static CompoundTag getOrCreateCitadelTag(LivingEntity entity) {
        return getCitadelTag(entity).copy();
    }

    /** 附件内部的 tag 实例（未挂附件时返回新的空 tag，此时不会挂到实体上）；只读用途。 */
    public static CompoundTag getCitadelTag(LivingEntity entity) {
        CompoundTag tag = entity.getAttached(PHAttachments.PETHOME_DATA);
        return tag == null ? new CompoundTag() : tag;
    }

    /** 只读路径：返回当前挂载实例（不拷贝），可能为 {@code null}。供 {@link PetEnchantCache} 做引用比对与缓存查询，调用方不得修改。 */
    @Nullable
    public static CompoundTag getExistingCitadelTagOrNull(LivingEntity entity) {
        return entity.getAttached(PHAttachments.PETHOME_DATA);
    }

    /** 写回附件；与现有内容相等时跳过（{@code setAttached} 不变也等于一次无意义的同步广播）。 */
    public static void setCitadelTag(LivingEntity entity, CompoundTag tag) {
        CompoundTag current = entity.getAttached(PHAttachments.PETHOME_DATA);
        if (current != null && current.equals(tag)) {
            return;
        }
        entity.setAttached(PHAttachments.PETHOME_DATA, tag);
    }
}
