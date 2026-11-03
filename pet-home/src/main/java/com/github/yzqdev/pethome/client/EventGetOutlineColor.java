package com.github.yzqdev.pethome.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;


/**
 * 实体轮廓色取值上下文（对应 NeoForge 的 {@code GetOutlineColorEvent}）：携带实体与
 * 默认轮廓色，{@code ClientModEvents} 里用于给指令鼓波及的宠物等实体染高亮描边。
 */
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
