package com.github.yzqdev.pethome.server.entity.ai;


import com.github.yzqdev.pethome.server.entity.IComandableMob;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.entity.TameableUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

import java.util.EnumSet;

/**
 * 三态指令的"跟随"寻路目标（原版 {@code FollowOwnerGoal} 的非 {@code TamableAnimal} 版本）：
 * 供狐狸 / 兔子 / 美西螈 / 青蛙等经 {@link ModifiedToBeTameable} 扩展驯服的生物使用。
 * 与原版差异：跟随/停止判定读 Mixin 注入的驯服数据与指令状态；携带"两栖"项圈标签的宠物
 * 在水中也直接向主人寻路，且传送落点允许是水域。
 */
public class FollowOwner2Goal extends Goal {

    /** 承载驯服数据的生物本体，通过 {@link ModifiedToBeTameable} 桥接访问主人与指令状态。 */
    private final Animal tamable;
    private final LevelReader level;
    private final double speedModifier;
    private final PathNavigation navigation;
    private final float stopDistance;
    private final float startDistance;
    private final boolean canFly;
    private LivingEntity owner;
    private int timeToRecalcPath;
    private float oldWaterCost;

    /**
     * @param animal        拥有驯服数据的宠物本体
     * @param speedModifier 寻路速度倍率
     * @param startDistance 与主人超过该距离（方块数）才开始跟随
     * @param stopDistance  距主人小于该距离即视为到达，停止寻路
     * @param flies         传送落点是否允许树叶上方（飞行型宠物为 true）
     */
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

    /** 仅在指令为"跟随"（2）且距离主人超过 startDistance 时启动；三态指令系统关闭时退化为原版行为。 */
    public boolean canUse() {
        LivingEntity livingentity = ((ModifiedToBeTameable) this.tamable).getTameOwner();
        if(tamable instanceof IComandableMob commandableMob && commandableMob.getCommand() != 2 && PetHomeMod.CONFIG.trinaryCommandSystem.get()) {
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

    /** 指令切走、寻路完成或进入停留（1）即终止。 */
    public boolean canContinueToUse() {
        if(tamable instanceof IComandableMob commandableMob && commandableMob.getCommand() != 2 && PetHomeMod.CONFIG.trinaryCommandSystem.get()) {
            return false;
        }else if (this.navigation.isDone()) {
            return false;
        } else if (((ModifiedToBeTameable) this.tamable).isStayingStill()) {
            return false;
        } else {
            return !(this.tamable.distanceToSqr(this.owner) <= (double) (this.stopDistance * this.stopDistance));
        }
    }

    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.tamable.getPathfindingMalus(BlockPathTypes.WATER);
        this.tamable.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.tamable.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }

    /** 每 10 tick 重估一次路径；超过 12 格（144 平方距离）直接传送到主人身边。 */
    public void tick() {
        if (TameableUtils.hasEnchant(tamable, PHEnchantmentRegistry.AMPHIBIOUS) && tamable.isInWaterOrBubble() && this.tamable.distanceToSqr(this.owner) < 144.0D) {
            // 两栖项圈标签：水中时无视常规距离判定，持续向主人寻路
            tamable.getNavigation().moveTo(owner, speedModifier);
        }
        this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float) this.tamable.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if (!this.tamable.isLeashed() && !this.tamable.isPassenger()) {
                if (this.tamable.distanceToSqr(this.owner) >= 144.0D) {
                    this.teleportToOwner();
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }

            }
        }
    }

    /** 在主人周围 7×3×7 范围内随机尝试 10 次找传送落点，找不到则放弃（本 tick 不传送）。 */
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

    /** 落点校验：不能落在主人 2 格内（避免贴脸传送）；通过后执行传送并清除当前寻路。 */
    private boolean maybeTeleportTo(int x, int y, int z) {
        if (Math.abs((double) x - this.owner.getX()) < 2.0D && Math.abs((double) z - this.owner.getZ()) < 2.0D) {
            return false;
        } else if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.tamable.moveTo((double) x + 0.5D, y, (double) z + 0.5D, this.tamable.getYRot(), this.tamable.getXRot());
            this.navigation.stop();
            return true;
        }
    }

    /** 落点必须可站立且无碰撞；两栖项圈标签允许水域落点，非飞行宠物禁树叶落点（防卡树叶）。 */
    private boolean canTeleportTo(BlockPos pos) {
        BlockPathTypes blockpathtypes = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, pos.mutable());
        if (TameableUtils.hasEnchant(tamable, PHEnchantmentRegistry.AMPHIBIOUS) && level.isWaterAt(pos)) {
            return true;
        }
        if (blockpathtypes != BlockPathTypes.WALKABLE) {
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