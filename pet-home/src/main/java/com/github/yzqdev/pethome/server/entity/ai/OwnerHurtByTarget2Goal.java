package com.github.yzqdev.pethome.server.entity.ai;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;

import java.util.EnumSet;

/**
 * "主人被谁打了，宠物就反击谁"目标：响应主人 {@code getLastHurtByMob} 的受击来源，
 * 供经 {@link ModifiedToBeTameable} 扩展驯服的生物使用（原版同名 Goal 只认 {@code TamableAnimal}）。
 */
public class OwnerHurtByTarget2Goal extends TargetGoal {
    private final Animal tameAnimal;
    /** 主人最近一次受击的攻击者；canUse 探测时暂存，start 时设为宠物当前攻击目标。 */
    private LivingEntity ownerLastHurt;
    /** 上次触发时的时间戳（取主人 {@code getLastHurtMobTimestamp}），只在变化时重新索敌。 */
    private int timestamp;

    public OwnerHurtByTarget2Goal(Animal tameAnimal) {
        super(tameAnimal, false);
        this.tameAnimal = tameAnimal;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    /** 主人已驯服宠物、未停留、刚被新目标攻击（时间戳变化）且该攻击者可被宠物攻击时触发反击。 */
    public boolean canUse() {
        if (((ModifiedToBeTameable)this.tameAnimal).isTame() && !((ModifiedToBeTameable)this.tameAnimal).isStayingStill()) {
            LivingEntity livingentity = ((ModifiedToBeTameable)this.tameAnimal).getTameOwner();
            if (livingentity == null) {
                return false;
            } else {
                this.ownerLastHurt = livingentity.getLastHurtByMob();
                int i = livingentity.getLastHurtMobTimestamp();
                return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && ((ModifiedToBeTameable)this.tameAnimal).isValidAttackTarget(this.ownerLastHurt);
            }
        } else {
            return false;
        }
    }

    /** 把暂存的攻击者设为宠物当前目标，并记录时间戳防止重复触发。 */
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        LivingEntity livingentity = ((ModifiedToBeTameable)this.tameAnimal).getTameOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtMobTimestamp();
        }

        super.start();
    }
}