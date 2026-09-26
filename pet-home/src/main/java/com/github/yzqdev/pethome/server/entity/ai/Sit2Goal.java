package com.github.yzqdev.pethome.server.entity.ai;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fox.Fox;

import java.util.EnumSet;

public class Sit2Goal extends Goal {
    private final Animal mob;

    public Sit2Goal(Animal animal) {
        this.mob = animal;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    public boolean canContinueToUse() {
        return ((ModifiedToBeTameable)this.mob).isTame() && ((ModifiedToBeTameable)this.mob).isStayingStill();
    }

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

    public void start() {
        this.mob.getNavigation().stop();
        if(this.mob instanceof Fox){
            ((Fox) this.mob).setSitting(true);
        }
    }

    public void stop() {
    }
}
