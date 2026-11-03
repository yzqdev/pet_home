package com.github.yzqdev.pethome.server.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 「被本模组改造为可驯服」duck 接口：由美西螈/狐狸/兔子/青蛙等原版动物的 Mixin 注入，
 * 在不继承 TamableAnimal 的前提下补上驯服归属体系；{@link TameableUtils} 的判定/归属
 * 方法全部同时兼容本接口与 TamableAnimal。
 */
public interface ModifiedToBeTameable extends OwnableEntity {
    boolean isTame();
    void setTame(boolean value);
    /** 主人 UUID（不要求在线）；通过 OwnableEntity#getOwnerUUID 暴露给团队/保护判定。 */
    @Nullable
    UUID getTameOwnerUUID();
    void setTameOwnerUUID(@Nullable UUID uuid);
    /** 在线主人实体，离线时为 null。 */
    @Nullable
    LivingEntity getTameOwner();

    /** 是否处于「待着」（stay）状态。 */
    boolean isStayingStill();
    /** 是否处于「跟随」状态（迷途灯笼召回只认该状态）。 */
    boolean isFollowingOwner();

    /** 攻击目标合法性校验（防御类 AI 用；如不攻击自家宠物/主人）。 */
    boolean isValidAttackTarget(LivingEntity target);

    @Nullable
    default UUID getOwnerUUID(){
        return getTameOwnerUUID();
    }

}
