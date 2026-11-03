package com.github.yzqdev.pethome.server.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

/**
 * @author Alexthe666
 * @since 1.7.0
 *
 * CitadelTag is a datasynced tag for LivingEntity provided by citadel to be used by various mods.
 */
/**
 * Citadel 遗产的 1.20.1 形态：宠物持久化数据的读写门面。
 * 数据实际存放在 {@code mixin/LivingEntityMixin} 注入的 SynchedEntityData CompoundTag 通道
 * （实现 {@link ICitadelDataEntity}；随实体 NBT 键 {@code PetHomeEntityData} 持久化，
 * {@code set} 时自动同步给追踪该实体的客户端）。注意 {@link #getOrCreateCitadelTag} 返回的是
 * <strong>底层引用</strong>而非副本——原地改键有效，但仍需 {@link #setCitadelTag} + 业务层
 * {@code TameableUtils.sync} 才能触发网络同步。
 */
public class CitadelEntityData {


    /** 返回宠物的 citadel tag（底层引用）；实体尚未初始化时返回空 tag。 */
    public static CompoundTag getOrCreateCitadelTag(LivingEntity entity) {
        CompoundTag tag = getCitadelTag(entity);
        return tag == null ? new CompoundTag() : tag;
    }

    /** 非 ICitadelDataEntity 实体（理论上不存在）返回空 tag 而非 null。 */
    public static CompoundTag getCitadelTag(LivingEntity entity) {
        return entity instanceof ICitadelDataEntity ? ((ICitadelDataEntity) entity).getCitadelEntityData() : new CompoundTag();
    }

    /** 整体替换 tag 引用（触发 SynchedEntityData 同步）；网络广播由 {@code TameableUtils.sync} 负责。 */
    public static void setCitadelTag(LivingEntity entity, CompoundTag tag) {
        if(entity instanceof ICitadelDataEntity){
            ((ICitadelDataEntity) entity).setCitadelEntityData(tag);
        }
    }
}