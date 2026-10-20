package com.github.yzqdev.pethome.server.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 「经本 mod 改造为可驯服」的 duck 接口（mixin 实现：美西螈/狐狸/兔子/青蛙等，
 * 原 mod 对 Citadel 的驯服能力依赖由此替代）。与 {@link IComandableMob} 配套提供
 * 三态指令与驯服归属；{@link TameableUtils} 的判定族以本接口为第一分支。
 */
public interface ModifedToBeTameable extends OwnableEntity {
    /** 是否已被驯服（mixin 持久化到实体 NBT 的驯服位） */
    boolean isTame();

    /** 设置驯服位（不自动绑定主人，需随后 setTameOwnerUUID） */
    void setTame(boolean value);

    /** 驯服主人 UUID；未驯服为 {@code null} */
    @Nullable
    UUID getTameOwnerUUID();

    void setTameOwnerUUID(@Nullable UUID uuid);

    /** 在线主人实体；离线时为 {@code null}（UUID 仍可经 getTameOwnerUUID 取到） */
    @Nullable
    LivingEntity getTameOwner();

    /** 是否处于停留（stay）状态：三态指令 ==1 且 trinaryCommandSystem 开启 */
    boolean isStayingStill();

    /** 是否处于跟随（follow）状态：三态指令 ==2 且 trinaryCommandSystem 开启 */
    boolean isFollowingOwner();

    /** AI 索敌过滤：目标是否可作为本宠攻击对象（主人/友军应返回 false） */
    boolean isValidAttackTarget(LivingEntity target);

    @Nullable
    default UUID getOwnerUUID() {
        return getTameOwnerUUID();
    }

}
