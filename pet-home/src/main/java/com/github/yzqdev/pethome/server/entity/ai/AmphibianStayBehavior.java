package com.github.yzqdev.pethome.server.entity.ai;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;

/**
 * 水陆两栖宠物（驯服美西螈/青蛙）的"停留" Brain Behavior（对应三态指令 1=停留）：
 * 宠物处于停留态期间持续擦除行走目标并停止导航，实现原地停留；行为最长持续 400 tick。
 * 由 {@code AxolotlAiMixin} / {@code FrogAiMixin} 注入对应 Activity 的行为集。
 */
public class AmphibianStayBehavior<T extends Animal> extends Behavior<T> {

    public AmphibianStayBehavior() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED), 400);
    }

    /** 启动条件：宠物处于停留态（三态指令 1=停留）。 */
    protected boolean checkExtraStartConditions(ServerLevel level, T axolotl) {
        return ((ModifiedToBeTameable)axolotl).isStayingStill();
    }


    /** 继续条件：宠物仍处于停留态。 */
    protected boolean canStillUse(ServerLevel level, T axolotl, long gameTime) {
        return ((ModifiedToBeTameable)axolotl).isStayingStill();

    }

    protected void stop(ServerLevel level, T axolotl, long gameTime) {
    }

    /** 每 tick 擦除行走目标并停止导航，保证宠物原地停留。 */
    protected void tick(ServerLevel level, T axolotl, long gameTime) {
        axolotl.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        axolotl.getNavigation().stop();
    }
}
