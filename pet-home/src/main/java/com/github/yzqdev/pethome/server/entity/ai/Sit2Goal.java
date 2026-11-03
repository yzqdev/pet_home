package com.github.yzqdev.pethome.server.entity.ai;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;

import java.util.EnumSet;

/**
 * 三态指令的"停留"（1）目标：让经 {@link ModifiedToBeTameable} 扩展驯服的非坐骑生物停在原地
 * （原版 sit 姿态只认 {@code TamableAnimal}，这里用"清空寻路"实现等效停留；狐狸额外同步其原版坐下动画）。
 */
public class Sit2Goal extends Goal {
    private final Animal mob;

    /** @param animal 拥有驯服数据的宠物本体 */
    public Sit2Goal(Animal animal) {
        this.mob = animal;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    public boolean canContinueToUse() {
        return ((ModifiedToBeTameable)this.mob).isTame() && ((ModifiedToBeTameable)this.mob).isStayingStill();
    }

    /** 已驯服、在地面且处于停留指令时保持运行；水中/空中强制中断以防溺亡或悬空。 */
    public boolean canUse() {
        if (!((ModifiedToBeTameable)this.mob).isTame()) {
            return false;
        } else if (this.mob.isInWaterOrBubble()) {
            return false;
        } else if (!this.mob.onGround()) {
            return false;
        } else {
            return ((ModifiedToBeTameable)this.mob).isStayingStill();
        }
    }

    /** 启动时清空寻路实现"停住"；狐狸是原版实体，需同步其 sitting 标记才有坐下姿势。 */
    public void start() {
        this.mob.getNavigation().stop();
        if(this.mob instanceof Fox){
            ((Fox) this.mob).setSitting(true);
        }
    }

    public void stop() {
    }
}
