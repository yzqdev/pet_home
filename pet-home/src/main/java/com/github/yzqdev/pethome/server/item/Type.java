package com.github.yzqdev.pethome.server.item;

/** 捕网物品的状态档位：{@code INIT} 注册占位、{@code EMPTY} 空网、{@code HAS_MOB} 装有生物的网（分别对应两个注册物品）。 */
public enum Type {

    INIT,
    EMPTY,
    HAS_MOB,
}