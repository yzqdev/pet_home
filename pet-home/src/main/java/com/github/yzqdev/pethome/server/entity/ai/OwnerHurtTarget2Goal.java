package com.github.yzqdev.pethome.server.entity.ai;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;

import java.util.EnumSet;

/**
 * 宠物"帮主人进攻"目标选择 Goal（Goal 版本而非 Brain Behavior）：
 * 主人主动攻击某生物后，把该目标设为宠物的攻击目标（与 {@code OwnerHurtByTarget2Goal}
 * 的"为主人报仇"互补）；停留态不启动，目标须经 {@code isValidAttackTarget} 校验。
 */
public class OwnerHurtTarget2Goal extends TargetGoal {
    private final Animal tameAnimal;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public OwnerHurtTarget2Goal(Animal tameAnimal) {
        super(tameAnimal, false);
        this.tameAnimal = tameAnimal;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    /** 启动条件：已驯服、未停留，主人新近攻击过某生物（时间戳变化）且该生物可被列为攻击目标。 */
    public boolean canUse() {
        if (((ModifiedToBeTameable)this.tameAnimal).isTame() && !((ModifiedToBeTameable)this.tameAnimal).isStayingStill()) {
            LivingEntity livingentity = ((ModifiedToBeTameable)this.tameAnimal).getTameOwner();
            if (livingentity == null) {
                return false;
            } else {
                this.ownerLastHurt = livingentity.getLastHurtMob();
                int i = livingentity.getLastHurtMobTimestamp();
                return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && ((ModifiedToBeTameable)this.tameAnimal).isValidAttackTarget(this.ownerLastHurt);
            }
        } else {
            return false;
        }
    }

    /** 启动：把主人攻击的目标设为宠物攻击目标，并记录时间戳防止同一次攻击重复触发。 */
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        LivingEntity livingentity = ((ModifiedToBeTameable)this.tameAnimal).getTameOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtMobTimestamp();
        }

        super.start();
    }
}