package com.github.yzqdev.pethome.server.entity.ai;

import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
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
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * 两栖宠物（美西螈 / 青蛙）Brain 版"跟随主人"行为：指令为"跟随"（2）时挂到 follow Activity 上运行。
 * 与 Goal 版 {@link FollowOwner2Goal} 逻辑对齐——陆上/水中速度分开配置（"迅捷"项圈标签按等级加速），
 * 距离超 12 格直接传送；水中传送落点天然允许（水域即合法落点）。
 */
public class AmphibianFollowOwnerBehavior<T extends Animal> extends Behavior<T> {

    /** 与主人距离超过 10 格才开始跟随。 */
    private static final float START_DISTANCE = 10F;
    /** 距主人 2 格内视为到达，停止跟随。 */
    private static final float STOP_DISTANCE = 2F;
    private float baseSpeedLand = 1.0F;
    private float baseSpeedWater = 1.0F;
    /** checkExtraStartConditions 时缓存的主人，供 tick/canStillUse 复用。 */
    private LivingEntity owner;

    /**
     * @param baseSpeedLand 陆地跟随速度
     * @param baseSpeedWater 水中跟随速度
     */
    public AmphibianFollowOwnerBehavior(float baseSpeedLand, float baseSpeedWater) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), 20);
        this.baseSpeedLand = baseSpeedLand;
        this.baseSpeedWater = baseSpeedWater;
   }

    /** 主人存活、非旁观者且指令为"跟随"（2）时启动。 */
    protected boolean checkExtraStartConditions(ServerLevel level, T axolotl) {
        if(axolotl instanceof ModifiedToBeTameable tamed){
            owner = tamed.getTameOwner();
            if(owner != null && owner.isAlive() && !owner.isSpectator() && tamed.isFollowingOwner()){
                return true;
            }
        }
        return false;
    }


    /** 进入繁殖（繁殖优先于跟随）或主人失效 / 距离足够近时停止。 */
    protected boolean canStillUse(ServerLevel level, T axolotl, long gameTime) {
        if(!axolotl.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET) && owner != null && owner.isAlive()){
            return ((ModifiedToBeTameable)axolotl).isFollowingOwner() && axolotl.distanceTo(owner) > STOP_DISTANCE;
        }
        return false;
    }

    protected void stop(ServerLevel level, T axolotl, long gameTime) {
        axolotl.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    /** 超过 12 格传送；否则按陆/水基速 + "迅捷"项圈标签加成设置行走与注视目标记忆。 */
    protected void tick(ServerLevel level, T axolotl, long gameTime) {
        if (axolotl.distanceToSqr(this.owner) >= 144.0D) {
            this.teleportToOwner(axolotl);
        } else{
            int speedsterLevel = TameableUtils.getEnchantLevel(axolotl, PHEnchantmentRegistry.SPEEDSTER);
            float speed = axolotl.isInWaterOrBubble() ? baseSpeedWater + speedsterLevel * 0.05F : baseSpeedLand + speedsterLevel * 0.1F;
            BehaviorUtils.lookAtEntity(axolotl, owner);
            BehaviorUtils.setWalkAndLookTargetMemories(axolotl, owner, speed, (int)STOP_DISTANCE);
        }
    }


    private int randomIntInclusive(int min, int max) {
        return this.owner.getRandom().nextInt(max - min + 1) + min;
    }

    /** 在主人周围 7×3×7 范围内随机尝试 10 次找传送落点，找不到则放弃（本 tick 不传送）。 */
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

    /** 落点不能在主人 2 格内；通过校验后传送并清空行走记忆，让 Brain 停止旧寻路。 */
    private boolean maybeTeleportTo(T axolotl, int x, int y, int z) {
        if (Math.abs((double)x - this.owner.getX()) < 2.0D && Math.abs((double)z - this.owner.getZ()) < 2.0D) {
            return false;
        } else if (!this.canTeleportTo(axolotl, new BlockPos(x, y, z))) {
            return false;
        } else {
            axolotl.moveTo((double)x + 0.5D, (double)y, (double)z + 0.5D, axolotl.getYRot(), axolotl.getXRot());
            axolotl.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            return true;
        }
    }

    /** 落点为水（两栖宠物天然合法）或可站立无碰撞的方块时可用。 */
    private boolean canTeleportTo(T axolotl, BlockPos pos) {
        BlockPathTypes blockpathtypes = WalkNodeEvaluator.getBlockPathTypeStatic(axolotl.level(), pos.mutable());
        if(axolotl.level().getFluidState(pos).is(Fluids.WATER)){
            return true;
        }else if (blockpathtypes != BlockPathTypes.WALKABLE) {
            return false;
        } else {
            BlockPos blockpos = pos.subtract(axolotl.blockPosition());
            return axolotl.level().noCollision(axolotl, axolotl.getBoundingBox().move(blockpos));
        }
    }
}