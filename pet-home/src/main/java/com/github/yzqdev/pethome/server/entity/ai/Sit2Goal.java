package com.github.yzqdev.pethome.server.entity.ai;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fox.Fox;

import java.util.EnumSet;

/**
 * 宠物"停留" Goal（对应三态指令 1=停留，Goal 版本而非 Brain Behavior）：
 * 已驯服且处于停留态时停止导航原地蹲坐；狐狸额外同步原版 {@code setSitting(true)} 姿态。
 * 启动要求不在水中/雨中且在地面。
 */
public class Sit2Goal extends Goal {
    private final Animal mob;

    public Sit2Goal(Animal animal) {
        this.mob = animal;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    /** 继续条件：仍处于停留态且已驯服。 */
    public boolean canContinueToUse() {
        return ((ModifiedToBeTameable)this.mob).isTame() && ((ModifiedToBeTameable)this.mob).isStayingStill();
    }

    /** 启动条件：已驯服、不在水中/雨中、在地面且处于停留态。 */
    public boolean canUse() {
        if (!((ModifiedToBeTameable)this.mob).isTame()) {
            return false;
        } else if (this.mob.isInWaterOrRain()) {
            return false;
        } else if (!this.mob.onGround()) {
            return false;
        } else {
            return ((ModifiedToBeTameable)this.mob).isStayingStill();
        }
    }

    /** 启动时停止导航；狐狸额外同步原版坐姿。 */
    public void start() {
        this.mob.getNavigation().stop();
        if(this.mob instanceof Fox){
            ((Fox) this.mob).setSitting(true);
        }
    }

    public void stop() {
    }
}
