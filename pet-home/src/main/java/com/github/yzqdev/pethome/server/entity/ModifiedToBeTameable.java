package com.github.yzqdev.pethome.server.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;

import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Mixin 扩展驯服能力接口（注意拼写沿用原版）：为狐狸 / 兔子 / 美西螈 / 青蛙 / 马类等
 * 非原版宠物实现完整驯服——驯服状态、主人 UUID/实体、三态指令姿态（{@link OwnableEntity} 提供 owner 桥接）。
 * {@code TameableUtils} 的归属判定据此走与 {@code TamableAnimal} 平行的分支。
 */
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
    default UUID getOwnerUUID(){
        return getTameOwnerUUID();
    }

}
