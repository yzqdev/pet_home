package com.github.yzqdev.pethome.server.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 两栖宠物（美西螈 / 青蛙）的路径导航：在水下用水路节点、陆上用陆地节点（{@code AmphibiousNodeEvaluator(true)}），
 * 使宠物能跨水陆连续寻路跟随主人。
 */
public class AmphibiousPathNavigation extends WaterBoundPathNavigation {

    /** @param entitylivingIn 挂载该导航的宠物 @param worldIn 所在世界 */
    public AmphibiousPathNavigation(Mob entitylivingIn, Level worldIn) {
        super(entitylivingIn, worldIn);
    }

    protected PathFinder createPathFinder(int i) {
        this.nodeEvaluator = new AmphibiousNodeEvaluator(true);
        return new PathFinder(this.nodeEvaluator, i);
    }

    /** 始终允许更新路径：两栖宠物水陆都移动，没有"仅水中寻路"的限制。 */
    protected boolean canUpdatePath() {
        return true;
    }

    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), this.mob.getY(0.5D), this.mob.getZ());
    }

    /** 目标点即两落点间视线无碰撞时允许直线移动（检测线抬到实体半高，避免贴地射线被地面挡住）。 */
    protected boolean canMoveDirectly(Vec3 posVec31, Vec3 posVec32) {
        Vec3 vector3d = new Vec3(posVec32.x, posVec32.y + (double) this.mob.getBbHeight() * 0.5D, posVec32.z);
        return this.level.clip(new ClipContext(posVec31, vector3d, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.mob)).getType() == HitResult.Type.MISS;
    }

    /** 水面或任何下方不是空气的方块都可作为目的地（放宽原版"必须可站立"约束）。 */
    public boolean isStableDestination(BlockPos pos) {
        return !this.level.getBlockState(pos.below()).isAir() || this.level.isWaterAt(pos.below());
    }

    public void setCanFloat(boolean canSwim) {
    }
}
