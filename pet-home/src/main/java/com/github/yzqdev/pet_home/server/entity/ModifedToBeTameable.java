package com.github.yzqdev.pet_home.server.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public interface ModifedToBeTameable extends OwnableEntity {
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
