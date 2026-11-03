package com.github.yzqdev.pethome.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

/**
 * 三态指令 duck 接口（由数据槽 mixin 挂到狼/猫/鹦鹉/狐狸/兔子/青蛙等改造实体上）。
 * 指令编号（三侧约定，存 citadel tag 的 {@code DICommand} 键）：
 * <ul>
 *   <li>0 = 游走（自由行动，保留仇恨）</li>
 *   <li>1 = 停留（原地不动，TamableAnimal 同步置 {@code orderedToSit}）</li>
 *   <li>2 = 跟随（跟随主人，跟随类 AI 与传送/灯笼召回都以 2 为准）</li>
 * </ul>
 */
public interface IComandableMob {

    /** 当前指令编号（0/1/2，见类注释）。 */
    int getCommand();

    /** 设置指令编号；仅写状态，不发声不发消息。 */
    void setCommand(int command);

    /** 玩家右键切换指令：循环 0→1→2→0（服务端执行），停留态联动 {@code setOrderedToSit}；返回 PASS 不拦截后续交互。 */
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

    /** 切换指令后给玩家发提示的钩子；默认空实现（罗盘/鼓/驯服流程按需覆写）。 */
    default void sendCommandMessage(Player owner, int command, Component name) {

    }

}
