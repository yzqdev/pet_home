package com.github.yzqdev.pethome.server.entity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

/**
 * 青蛙交互钩子（由 {@code mixin/FrogMixin} 实现）：拦截玩家右键青蛙的自定义交互（喂食驯服等）。
 */
public interface IFrog {

    boolean onFrogInteract(Player player, InteractionHand hand);
}
