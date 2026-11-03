package com.github.yzqdev.pethome.server.entity.ai;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;

/**
 * 两栖宠物（美西螈 / 青蛙）Brain 版"停留"（1）行为：指令为停留时持续清空行走记忆并停住寻路，
 * 让美西螈 / 青蛙停在原地（原版 sit 姿态对这两类生物无效，用"无移动"实现等效停留）。
 */
public class AmphibianStayBehavior<T extends Animal> extends Behavior<T> {

    public AmphibianStayBehavior() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED), 400);
    }

    /** 指令为"停留"（1）时启动并持续运行；期间每 tick 压制一切移动。 */
    protected boolean checkExtraStartConditions(ServerLevel level, T axolotl) {
        return ((ModifiedToBeTameable)axolotl).isStayingStill();
    }


    protected boolean canStillUse(ServerLevel level, T axolotl, long gameTime) {
        return ((ModifiedToBeTameable)axolotl).isStayingStill();

    }

    protected void stop(ServerLevel level, T axolotl, long gameTime) {
    }

    /** 每 tick 清行走记忆并停寻路，保证停留期间不被其他移动行为打断。 */
    protected void tick(ServerLevel level, T axolotl, long gameTime) {
        axolotl.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        axolotl.getNavigation().stop();
    }
}
