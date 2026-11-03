package com.github.yzqdev.pethome.server.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

/**
 * @author Alexthe666
 * @since 1.7.0
 *
 * CitadelTag is a datasynced tag for LivingEntity provided by citadel to be used by various mods.
 */
public class CitadelEntityData {

    /**
     * 取 citadel tag（实体没有数据时返回新的空 tag）。1.20 侧实现为 SynchedEntityData 的
     * CompoundTag 槽位，返回的是<b>活实例</b>（与 26.1 的「副本 + setCitadelTag 写回」不同）：
     * 调用方原地修改后必须调用 {@link #setCitadelTag} force 写回，否则不会同步给追踪者。
     */
    public static CompoundTag getOrCreateCitadelTag(LivingEntity entity) {
        CompoundTag tag = getCitadelTag(entity);
        return tag == null ? new CompoundTag() : tag;
    }

    /** 只读取 citadel tag；实体未实现 {@link ICitadelDataEntity}（如玩家/非生物）时返回空 tag 而非 null。 */
    public static CompoundTag getCitadelTag(LivingEntity entity) {
        return entity instanceof ICitadelDataEntity ? ((ICitadelDataEntity) entity).getCitadelEntityData() : new CompoundTag();
    }

    /**
     * 写回 citadel tag（由 LivingEntityMixin 实现）：底层 {@code entityData.set(..., force=true)}——
     * 因为调用方通常原地修改的是同一引用，vanilla 的内容判等发现不了变化，必须 force 才会
     * 把更新发给追踪该实体的玩家；同时使该实体的附魔缓存失效。
     */
    public static void setCitadelTag(LivingEntity entity, CompoundTag tag) {
        if(entity instanceof ICitadelDataEntity){
            ((ICitadelDataEntity) entity).setCitadelEntityData(tag);
        }
    }
}