package com.github.yzqdev.pethome.server.entity.ai;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;

import java.util.EnumSet;

/**
 * "主人攻击了谁，宠物就打谁"目标：响应主人 {@code getLastHurtMob} 的攻击对象，
 * 供经 {@link ModifiedToBeTameable} 扩展驯服的生物使用（原版同名 Goal 只认 {@code TamableAnimal}）。
 */
public class OwnerHurtTarget2Goal extends TargetGoal {
    private final Animal tameAnimal;
    /** 主人最近一次攻击的目标实体；canUse 探测时暂存，start 时设为宠物当前攻击目标。 */
    private LivingEntity ownerLastHurt;
    /** 上次触发时主人受击时间戳，用于只在"新事件"时重新评估，避免每 tick 重复索敌。 */
    private int timestamp;

    public OwnerHurtTarget2Goal(Animal tameAnimal) {
        super(tameAnimal, false);
        this.tameAnimal = tameAnimal;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    /** 主人已驯服宠物、未停留、且刚攻击了新目标（时间戳变化）且该目标可被宠物攻击时触发。 */
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

    /** 把暂存的主人攻击目标设为宠物当前目标，并记录时间戳防止重复触发。 */
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        LivingEntity livingentity = ((ModifiedToBeTameable)this.tameAnimal).getTameOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtMobTimestamp();
        }

        super.start();
    }
}