package com.github.yzqdev.pethome.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;


public class EventGetOutlineColor {
    private Entity entityIn;
    private int color;
    protected Result result = Result.DEFAULT;

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

    public Result getResult() {
        return this.result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public enum Result {
        /**
         * Forcibly allows the custom color to be used.
         */
        ALLOW,

        /**
         * The default logic in {@link Mob#checkDespawn()} will be used to determine if the despawn may occur.
         */
        DEFAULT,

        /**
         * Forcibly prevents the despawn from occurring.
         */
        DENY
    }
}
