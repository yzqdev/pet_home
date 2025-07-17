package com.github.yzqdev.pet_home.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;

@OnlyIn(Dist.CLIENT)
//@Event.HasResult


public class EventGetOutlineColor extends Event {
    private Entity entityIn;
    private int color;
    protected  Result result =  Result.DEFAULT;
    public EventGetOutlineColor(Entity entityIn, int color) {
        this.entityIn = entityIn;
        this.color = color;
    }

    public Entity getEntityIn() {
        return entityIn;
    }

    public void setEntityIn(Entity entityIn) {
        this.entityIn = entityIn;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
    public  Result getResult() {
        return this.result;
    }
    public void setResult( Result result) {
        this.result = result;
    }

    public static enum Result {
        /**
         * Forcibly allows the despawn to occur.
         */
        ALLOW,

        /**
         * The default logic in {@link Mob#checkDespawn()} will be used to determine if the despawn may occur.
         */
        DEFAULT,

        /**
         * Forcibly prevents the despawn from occurring.
         */
        DENY;
    }

}