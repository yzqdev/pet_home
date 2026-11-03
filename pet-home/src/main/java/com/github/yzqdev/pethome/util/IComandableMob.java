package com.github.yzqdev.pethome.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

/**
 * 三态指令能力接口（0=游走 / 1=停留 / 2=跟随），由各驯服 Mixin（狼/猫/鹦鹉/狐/兔/螈/蛙）
 * 实现。指令值本身存两处：实体同步数据（{@code PHEntityDataAccessors.*Command}，供客户端渲染）
 * 与 NBT 持久化；灯笼收容判定 {@code shouldUnloadToLantern} 也读它。
 */
public interface IComandableMob {

    /** 当前三态指令（0 游走 / 1 停留 / 2 跟随）。 */
    int getCommand();

    void setCommand(int command);

    /**
     * 玩家右键切换指令：循环 {@code (cur+1)%3} 并通知；原版 {@code TamableAnimal}
     * 同步设置 {@code setOrderedToSit}（停留==坐下）。仅服务端执行，恒返回
     * {@code PASS}（结果由调用方的 Mixin 决定，避免吞掉原版交互）。
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

    default void sendCommandMessage(Player owner, int command, Component name) {

    }

}
