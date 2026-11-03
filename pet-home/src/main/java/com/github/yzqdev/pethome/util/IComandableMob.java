package com.github.yzqdev.pethome.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

/**
 * 三态指令能力接口（0=游走 / 1=停留 / 2=跟随），由各驯服 Mixin（狼/猫/鹦鹉/狐/兔/螈/蛙等）实现。 指令值存于实体的
 * SynchedEntityData，往生灯笼收容判定（{@code shouldUnloadToLantern}） 与跟随/停留 AI 都消费它。
 */
public interface IComandableMob {

  /** 当前指令状态：0 游走、1 停留、2 跟随。 */
  int getCommand();

  /** 设置指令状态（服务端调用）。 */
  void setCommand(int command);

  /**
   * 玩家右键切换指令：在服务端把指令按 {@code (当前+1) % 3} 循环，并让原版宠物同步 {@code setOrderedToSit}（停留=坐下）；固定返回 {@code
   * PASS} 让交互链继续。
   */
  default InteractionResult playerSetCommand(Player owner, Animal ourselves) {
    if (!owner.level().isClientSide()) {
      int command = (getCommand() + 1) % 3;
      this.setCommand(command);
      this.sendCommandMessage(owner, command, ourselves.getName());

      if (ourselves instanceof TamableAnimal) {
        ((TamableAnimal) (ourselves)).setOrderedToSit(command == 1);
      }
    }
    return InteractionResult.PASS;
  }

  /** 切换指令后的提示钩子（默认空实现），各 Mixin 覆写为向主人发本地化消息。 */
  default void sendCommandMessage(Player owner, int command, Component name) {}
}
