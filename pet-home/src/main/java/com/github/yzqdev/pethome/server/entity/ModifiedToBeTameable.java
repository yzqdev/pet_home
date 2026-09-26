package com.github.yzqdev.pethome.server.entity;

import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;

import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public interface ModifiedToBeTameable extends OwnableEntity {
    boolean isTame();

    void setTame(boolean value);

    @Nullable
    UUID getTameOwnerUUID();

    void setTameOwnerUUID(@Nullable UUID uuid);

    @Nullable
    LivingEntity getTameOwner();

    boolean isStayingStill();

    boolean isFollowingOwner();

    boolean isValidAttackTarget(LivingEntity target);

    @Nullable
    default UUID getOwnerUUID() {
        return getTameOwnerUUID();
    }

    // 26.1 OwnableEntity 新增了抽象方法 getOwnerReference()；Rabbit/Frog/Axolotl/Fox 等原生不实现它，
    // mixin 让目标实体实现本接口后若不补上，实体类会带上抽象方法，第三方模组 instanceof OwnableEntity
    // 后调用即 AbstractMethodError 崩溃（Xaero 小地图雷达实测）。default 实现从驯服 owner UUID 构造。
    @Override
    @Nullable
    default EntityReference<LivingEntity> getOwnerReference() {
        UUID uuid = getTameOwnerUUID();
        return uuid == null ? null : EntityReference.of(uuid);
    }

}
