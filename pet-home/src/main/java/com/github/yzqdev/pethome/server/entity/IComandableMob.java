package com.github.yzqdev.pethome.server.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

/**
 * 三态指令 duck 接口：由各驯服动物 Mixin 注入，0=待着（stay）、1=坐下、2=跟随（follow）。
 * 指挥鼓、迷途灯笼召回判定（仅跟随态召回）与罗盘传送都按该值分支。
 */
public interface IComandableMob {

    /** 当前指令状态（0/1/2，语义见类注释）。 */
    int getCommand();

    void setCommand(int command);

    /**
     * 玩家右键切换三态指令（循环 0→1→2→0）：仅服务端实际生效，返回 PASS 让原版继续处理交互。
     * 切到坐下（1）时同步置 TamableAnimal#setOrderedToSit，保证原版坐下 AI 一致。
     */
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

    /** 指令切换成功后向主人发送状态提示消息，由各实现类覆写（默认空实现）。 */
    default void sendCommandMessage(Player owner, int command, Component name){

    }

}