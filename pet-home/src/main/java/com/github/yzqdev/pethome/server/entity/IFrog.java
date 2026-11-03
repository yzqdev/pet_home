package com.github.yzqdev.pethome.server.entity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

/**
 * 青蛙交互扩展接口（由 {@code FrogMixin} 实现）：把"驯服青蛙/喂食"等自定义交互挂进 原版青蛙的交互链，由 {@code
 * PlayerInteractEntityHandler} 经 {@code instanceof} 调用。
 */
public interface IFrog {

  /** 玩家对青蛙的 mod 侧交互入口；返回是否已消费本次交互。 */
  boolean onFrogInteract(Player player, InteractionHand hand);
}
