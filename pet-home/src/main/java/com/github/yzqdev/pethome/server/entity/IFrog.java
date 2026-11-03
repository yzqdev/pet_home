package com.github.yzqdev.pethome.server.entity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

/**
 * 青蛙交互扩展接口：由 {@code FrogMixin} 实现，让玩家可以用蜘蛛眼驯服青蛙
 * （具体驯服/喂食逻辑在 Mixin 的交互注入里，此处只做类型标记与方法入口）。
 */
public interface IFrog {

    /** 玩家右键青蛙时的扩展交互入口；返回 {@code SUCCESS/CONSUME} 表示已消费本次交互。 */
    boolean onFrogInteract(Player player, InteractionHand hand);
}
