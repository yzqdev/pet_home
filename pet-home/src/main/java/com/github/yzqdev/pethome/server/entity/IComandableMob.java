package com.github.yzqdev.pethome.server.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

/**
 * 三态指令能力接口（由驯服相关 Mixin 实现）：0 = 游走（wander）、1 = 停留（stay/sit）、2 = 跟随（follow）。
 * {@code getCommand} 也被灯笼收容判定、拴魂传送等读取。
 */
public interface IComandableMob {

    int getCommand();

    void setCommand(int command);

    /** 指令轮换：服务端把指令 +1 取模 3，对原版宠物同步设置坐下姿态（==1）。 */
    default InteractionResult playerSetCommand(Player owner, Animal ourselves) {
        if (!owner.level().isClientSide()) {
            int command = (getCommand() + 1) % 3;
            this.setCommand(command);
            this.sendCommandMessage(owner, command, ourselves.getName());

            if(ourselves instanceof TamableAnimal){
                ((TamableAnimal)(ourselves)).setOrderedToSit(command == 1);
            }
        }
        return InteractionResult.PASS;
    }

    /** 指令切换的提示消息钩子（各 Mixin 覆写后发送本地化消息，默认无操作）。 */
    default void sendCommandMessage(Player owner, int command, Component name){

    }

}