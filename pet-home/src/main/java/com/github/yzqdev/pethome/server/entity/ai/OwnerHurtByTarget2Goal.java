package com.github.yzqdev.pethome.server.entity.ai;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;

import java.util.EnumSet;

/**
 * 宠物"为主人报仇"目标选择 Goal（Goal 版本而非 Brain Behavior）：
 * 主人被生物伤害后把伤害源设为宠物的攻击目标；停留态不启动。
 * 目标须经 {@code isValidAttackTarget} 校验（友军伤害保护拦截主人/队友）。
 */
public class OwnerHurtByTarget2Goal extends TargetGoal {
    private final Animal tameAnimal;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public OwnerHurtByTarget2Goal(Animal tameAnimal) {
        super(tameAnimal, false);
        this.tameAnimal = tameAnimal;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    /** 启动条件：已驯服、未停留，主人新近被生物伤害（时间戳变化）且该生物可被列为攻击目标。 */
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

    /** 启动：把伤害源设为攻击目标，并记录主人受伤时间戳防止同一次受伤重复触发。 */
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        LivingEntity livingentity = ((ModifiedToBeTameable)this.tameAnimal).getTameOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtMobTimestamp();
        }

        super.start();
    }
}