package com.github.yzqdev.pethome.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

/**
 * 三态指令 duck 接口（mixin 实现到本 mod 改造的可驯服动物上）：
 * {@code 0=徘徊(wander)}、{@code 1=停留(stay，原版坐下)}、{@code 2=跟随(follow)}。
 * 指令系统可经 {@code trinaryCommandSystem} 关闭；跟随相关逻辑（FollowOwnerGoal、
 * 灯笼召回、随主人传送）都以 {@code getCommand()==2} 为准。
 */
public interface IComandableMob {

    int getCommand();

    void setCommand(int command);

    /**
     * 玩家右键切换指令：0→1→2→0 循环（服务端执行），停留态同步原版
     * {@code setOrderedToSit(true)}；返回恒为 PASS（事件由调用方决定是否取消）。
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

    /** 切换指令后向主人发送提示；默认空实现（本 mod 改造动物各自覆写发聊天消息） */
    default void sendCommandMessage(Player owner, int command, Component name) {

    }

}
