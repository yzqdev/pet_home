package com.github.yzqdev.pethome.util;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;

/**
 * 每实体附魔解析缓存（挂 {@link PHAttachments#ENCHANT_CACHE} 附件，非同步非持久化）。
 * 失效策略是附件实例<b>引用比对</b>：写入路径必经 {@code setCitadelTag} 换实例，引用一致即缓存有效。
 * 供 mixin/渲染/Jade tooltip 等每 tick、每帧的附魔查询免于重复扫描 NBT。
 */
public class PetEnchantCache {

    @Nullable
    private CompoundTag source;
    private final Map<Identifier, Integer> levels = new HashMap<>();

    /**
     * 取当前 live citadel tag 的附魔等级映射；引用没变时直接复用。
     *
     * @param liveTag 实体上当前挂载的 citadel 附件实例（null = 未挂载），调用方不得修改
     */
    public Map<Identifier, Integer> rebuildIfStale(@Nullable CompoundTag liveTag) {
        if (source == liveTag) {
            return levels;
        }
        levels.clear();
        if (liveTag != null && liveTag.contains(TameableUtils.ENCHANTMENT_TAG)) {
            ListTag listtag = liveTag.getListOrEmpty(TameableUtils.ENCHANTMENT_TAG);
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompoundOrEmpty(i);
                String id = compoundtag.getStringOr("id", "");
                if (!id.isEmpty()) {
                    levels.put(Identifier.parse(id), compoundtag.getIntOr("lvl", 0));
                }
            }
        }
        source = liveTag;
        return levels;
    }
}
