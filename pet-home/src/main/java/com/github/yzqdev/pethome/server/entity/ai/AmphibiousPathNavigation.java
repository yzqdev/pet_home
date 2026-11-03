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
 * 水陆两栖宠物的寻路导航实现（继承水中寻路）：使用 {@code AmphibiousNodeEvaluator}
 * 让水路与陆路都能寻径；目的地判定同时接受实心地面与水面。
 */
public class AmphibiousPathNavigation extends WaterBoundPathNavigation {

    public AmphibiousPathNavigation(Mob entitylivingIn, Level worldIn) {
        super(entitylivingIn, worldIn);
    }

    /** 使用两栖节点评估器（AmphibiousNodeEvaluator）创建寻路器，水路与陆路均可寻径。 */
    protected PathFinder createPathFinder(int i) {
        this.nodeEvaluator = new AmphibiousNodeEvaluator(true);
        return new PathFinder(this.nodeEvaluator, i);
    }

    /** 恒可更新路径：两栖导航不依赖实体是否入水，陆地/水中都能重算路径。 */
    protected boolean canUpdatePath() {
        return true;
    }

    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), this.mob.getY(0.5D), this.mob.getZ());
    }

    protected boolean canMoveDirectly(Vec3 posVec31, Vec3 posVec32) {
        Vec3 vector3d = new Vec3(posVec32.x, posVec32.y + (double) this.mob.getBbHeight() * 0.5D, posVec32.z);
        return this.level.clip(new ClipContext(posVec31, vector3d, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.mob)).getType() == HitResult.Type.MISS;
    }

    /** 目的地稳定判定：下方为实心方块或水均接受（水陆通用的落脚点）。 */
    public boolean isStableDestination(BlockPos pos) {
        return !this.level.getBlockState(pos.below()).isAir() || this.level.isWaterAt(pos.below());
    }

    /** 刻意留空：禁用原版"游到水面漂浮"开关，两栖宠物的上浮/下沉由自身游动逻辑控制。 */
    public void setCanFloat(boolean canSwim) {
    }
}
