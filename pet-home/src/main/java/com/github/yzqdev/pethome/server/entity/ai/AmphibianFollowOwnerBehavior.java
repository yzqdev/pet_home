package com.github.yzqdev.pethome.server.entity.ai;

import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.util.TameableUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * 两栖宠物（青蛙 / 美西螈）Brain 版"跟随主人"行为，对应陆生宠物的 Goal 版 {@link FollowOwner2Goal}。
 * <p>
 * 三态指令为"跟随"（状态 2）时激活：随水陆环境切换基础移速，并叠加宠物身上
 * "迅捷"（{@code SPEEDSTER}）附魔的等级加成；与主人距离超过 12 格（144 平方距离）时
 * 放弃寻路，改为在主人周围随机寻位传送。运行于服务端 Brain tick。
 */
public class AmphibianFollowOwnerBehavior<T extends Animal> extends Behavior<T> {

    /** 开始跟随的最远距离（格）。 */
    private static final float START_DISTANCE = 10F;
    /** 保持跟随的停止距离（格），距离小于等于该值即结束跟随。 */
    private static final float STOP_DISTANCE = 2F;
    /** 陆地上的基础移速倍率。 */
    private float baseSpeedLand = 1.0F;
    /** 水中的基础移速倍率。 */
    private float baseSpeedWater = 1.0F;
    /** 行为开始时缓存的主人引用，整个跟随过程复用。 */
    private LivingEntity owner;

    public AmphibianFollowOwnerBehavior(float baseSpeedLand, float baseSpeedWater) {
        // 只要求 WALK_TARGET / LOOK_TARGET 处于已注册状态即可启动；行为持续期上限 20 tick
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), 20);
        this.baseSpeedLand = baseSpeedLand;
        this.baseSpeedWater = baseSpeedWater;
   }

    /**
     * 启动条件：实体是本模组驯服生物、主人存活且非旁观模式，且三态指令为"跟随"。
     * 命中时顺带缓存主人引用。
     */
    protected boolean checkExtraStartConditions(ServerLevel level, T axolotl) {
        if(axolotl instanceof ModifiedToBeTameable tamed){
            owner = tamed.getTameOwner();
            if(owner != null && owner.isAlive() && !owner.isSpectator() && tamed.isFollowingOwner()){
                return true;
            }
        }
        return false;
    }

    /**
     * 继续条件：不在繁殖中、主人仍存活、仍处于"跟随"指令且未到达停止距离。
     */
    protected boolean canStillUse(ServerLevel level, T axolotl, long gameTime) {
        if(!axolotl.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET) && owner != null && owner.isAlive()){
            return ((ModifiedToBeTameable)axolotl).isFollowingOwner() && axolotl.distanceTo(owner) > STOP_DISTANCE;
        }
        return false;
    }

    protected void stop(ServerLevel level, T axolotl, long gameTime) {
        axolotl.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    /**
     * 每 tick：距离超过 12 格时直接传送追主，否则写入"走向主人"的寻路 / 视线记忆。
     * 水中与陆地的移速加成系数不同（附魔每级水中 +0.05、陆地 +0.10）。
     */
    protected void tick(ServerLevel level, T axolotl, long gameTime) {
        // 144.0 = 12 格的平方距离：太远说明寻路追不上（两栖地形卡住），降级为传送
        if (axolotl.distanceToSqr(this.owner) >= 144.0D) {
            this.teleportToOwner(axolotl);
        } else{
            // 宠物本体附魔"迅捷"，等级转化为移速加成（附魔载体是宠物而非装备）
            int speedsterLevel = TameableUtils.getEnchantLevel(axolotl, PHEnchantments.SPEEDSTER);
            float speed = axolotl.isInWaterOrRain() ? baseSpeedWater + speedsterLevel * 0.05F : baseSpeedLand + speedsterLevel * 0.1F;
            BehaviorUtils.lookAtEntity(axolotl, owner);
            BehaviorUtils.setWalkAndLookTargetMemories(axolotl, owner, speed, (int)STOP_DISTANCE);
        }
    }


    private int randomIntInclusive(int min, int max) {
        return this.owner.getRandom().nextInt(max - min + 1) + min;
    }

    /**
     * 追主传送：以主人脚下为中心，随机取 ±3 格水平、±1 格垂直的偏移尝试至多 10 次。
     */
    private void teleportToOwner(T axolotl) {
        BlockPos blockpos = this.owner.blockPosition();

        for(int i = 0; i < 10; ++i) {
            int j = this.randomIntInclusive(-3, 3);
            int k = this.randomIntInclusive(-1, 1);
            int l = this.randomIntInclusive(-3, 3);
            boolean flag = this.maybeTeleportTo(axolotl, blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
            if (flag) {
                return;
            }
        }

    }

    /**
     * 校验单次传送落点：不能贴在主人身上（水平 2 格内），且落点须可传送；通过则瞬移并清除寻路记忆。
     *
     * @return true 表示已成功传送，外层循环据此停止重试
     */
    private boolean maybeTeleportTo(T axolotl, int x, int y, int z) {
        if (Math.abs((double)x - this.owner.getX()) < 2.0D && Math.abs((double)z - this.owner.getZ()) < 2.0D) {
            return false;
        } else if (!this.canTeleportTo(axolotl, new BlockPos(x, y, z))) {
            return false;
        } else {
            axolotl.snapTo((double)x + 0.5D, (double)y, (double)z + 0.5D, axolotl.getYRot(), axolotl.getXRot());
            axolotl.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            return true;
        }
    }

    /**
     * 落点可行性判定：落点若是水源方块直接放行（两栖宠物可入水），
     * 否则要求是可行走方块且移动后包围盒不与他物碰撞。
     */
    private boolean canTeleportTo(T axolotl, BlockPos pos) {
        PathType blockpathtypes = WalkNodeEvaluator.getPathTypeStatic(axolotl, pos);
        // 先查水：落点是水则无需地面判定
        if(axolotl.level().getFluidState(pos).is(Fluids.WATER)){
            return true;
        }else if (blockpathtypes != PathType.WALKABLE) {
            return false;
        } else {
            BlockPos blockpos = pos.subtract(axolotl.blockPosition());
            return axolotl.level().noCollision(axolotl, axolotl.getBoundingBox().move(blockpos));
        }
    }
}
