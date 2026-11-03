package com.github.yzqdev.pethome.server.entity.ai;


import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.util.TameableUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

import java.util.EnumSet;

/**
 * 陆生宠物 Goal 版"跟随主人"，移植自原版 {@code TamableAnimal.followOwner} 逻辑并按本模组扩展：
 * <ul>
 *   <li>接入三态指令：开启 {@link PetHomeConfig#trinaryCommandSystem} 时只有"跟随"（状态 2）才追主；</li>
 *   <li>"两栖"（{@code AMPHIBIOUS}）附魔让宠物在水中也可向主人寻路、并允许传送到水面落点；</li>
 *   <li>距离超过 12 格时降级为传送（与原版狼 / 猫一致）。</li>
 * </ul>
 * 两栖宠物（青蛙 / 美西螈）的 Brain 版实现见 {@link AmphibianFollowOwnerBehavior}。
 */
public class FollowOwner2Goal extends Goal {

    private final Animal tamable;
    private final LevelReader level;
    private final double speedModifier;
    private final PathNavigation navigation;
    /** 停止跟随的距离（格）：到达后目标达成。 */
    private final float stopDistance;
    /** 开始跟随的距离（格）：主人在此范围内不启动。 */
    private final float startDistance;
    /** 飞行宠物（鹦鹉等）：传送落点允许在树叶上。 */
    private final boolean canFly;
    private LivingEntity owner;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public FollowOwner2Goal(Animal animal, double speedModifier, float startDistance, float stopDistance, boolean flies) {
        this.tamable = animal;
        this.level = animal.level();
        this.speedModifier = speedModifier;
        this.navigation = animal.getNavigation();
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.canFly = flies;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /**
     * 启动条件：指令允许跟随（或未启用三态指令）、主人存在且非旁观、宠物未"停留"、
     * 且与主人距离超过 {@code startDistance}。
     */
    public boolean canUse() {
        LivingEntity livingentity = ((ModifiedToBeTameable) this.tamable).getTameOwner();
        // 三态指令开启时，只有"跟随"（getCommand() == 2）才追主；0=游走 / 1=停留 均不跟随
        if(tamable instanceof IComandableMob commandableMob && commandableMob.getCommand() != 2 && PetHomeConfig.trinaryCommandSystem) {
            return false;
        }else if (livingentity == null) {
            return false;
        } else if (livingentity.isSpectator()) {
            return false;
        } else if (((ModifiedToBeTameable) this.tamable).isStayingStill()) {
            return false;
        } else if (this.tamable.distanceToSqr(livingentity) < (double) (this.startDistance * this.startDistance)) {
            return false;
        } else {
            this.owner = livingentity;
            return true;
        }
    }

    /**
     * 继续条件：指令仍为"跟随"、寻路未结束、未"停留"且尚未进入停止距离。
     */
    public boolean canContinueToUse() {
        if(tamable instanceof IComandableMob commandableMob && commandableMob.getCommand() != 2 && PetHomeConfig.trinaryCommandSystem) {
            return false;
        }else if (this.navigation.isDone()) {
            return false;
        } else if (((ModifiedToBeTameable) this.tamable).isStayingStill()) {
            return false;
        } else {
            return !(this.tamable.distanceToSqr(this.owner) <= (double) (this.stopDistance * this.stopDistance));
        }
    }

    /**
     * 启动时清零寻路重算计时，并把寻路的水域惩罚临时清零（追主时允许下水）。
     */
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.tamable.getPathfindingMalus(PathType.WATER);
        this.tamable.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    /**
     * 结束时清空主人引用、停止导航，并恢复原水域寻路惩罚。
     */
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.tamable.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    /**
     * 每 tick 面向主人；每 10 tick 重算一次路线——距离超过 12 格时传送追主，
     * 否则正常寻路。带"两栖"附魔且身处水中时始终向主人寻路（不受 12 格限制）。
     */
    public void tick() {
        // 两栖附魔：水中像鱼一样直接游向主人（仅 12 格内，避免无限游）
        if (TameableUtils.hasEnchant(tamable, PHEnchantments.AMPHIBIOUS) && tamable.isInWaterOrRain() && this.tamable.distanceToSqr(this.owner) < 144.0D) {
            tamable.getNavigation().moveTo(owner, speedModifier);
        }
        this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float) this.tamable.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            // 被拴绳或骑乘时不自主移动
            if (!this.tamable.isLeashed() && !this.tamable.isPassenger()) {
                // 144.0 = 12 格平方距离：过远即传送，防止地形卡死
                if (this.tamable.distanceToSqr(this.owner) >= 144.0D) {
                    this.teleportToOwner();
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }

            }
        }
    }

    /**
     * 传送追主：以主人脚下为中心，随机取 ±3 格水平、±1 格垂直偏移尝试至多 10 次。
     */
    private void teleportToOwner() {
        BlockPos blockpos = this.owner.blockPosition();

        for (int i = 0; i < 10; ++i) {
            int j = this.randomIntInclusive(-3, 3);
            int k = this.randomIntInclusive(-1, 1);
            int l = this.randomIntInclusive(-3, 3);
            boolean flag = this.maybeTeleportTo(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
            if (flag) {
                return;
            }
        }

    }

    private boolean maybeTeleportTo(int x, int y, int z) {
        if (Math.abs((double) x - this.owner.getX()) < 2.0D && Math.abs((double) z - this.owner.getZ()) < 2.0D) {
            return false;
        } else if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.tamable.snapTo((double) x + 0.5D, (double) y, (double) z + 0.5D, this.tamable.getYRot(), this.tamable.getXRot());
            this.navigation.stop();
            return true;
        }
    }

    /**
     * 传送落点判定：带"两栖"附魔时允许水面落点；普通情况要求可行走、
     * 不可落在树叶上（飞行宠物除外），且移动后包围盒无碰撞。
     */
    private boolean canTeleportTo(BlockPos pos) {
        PathType blockpathtypes = WalkNodeEvaluator.getPathTypeStatic(this.tamable, pos);
        // 两栖附魔的专属落点：水面也可传送
        if (TameableUtils.hasEnchant(tamable, PHEnchantments.AMPHIBIOUS) && level.isWaterAt(pos)) {
            return true;
        }
        if (blockpathtypes != PathType.WALKABLE) {
            return false;
        } else {
            BlockState blockstate = this.level.getBlockState(pos.below());
            if (!this.canFly && blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockpos = pos.subtract(this.tamable.blockPosition());
                return this.level.noCollision(this.tamable, this.tamable.getBoundingBox().move(blockpos));
            }
        }
    }

    private int randomIntInclusive(int min, int max) {
        return this.tamable.getRandom().nextInt(max - min + 1) + min;
    }
}
