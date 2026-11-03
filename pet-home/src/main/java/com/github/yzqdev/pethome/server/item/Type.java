package com.github.yzqdev.pethome.server.item;

/**
 * 捕网状态枚举：{@code EMPTY} 空网（物品 id {@code net}）、
 * {@code HAS_MOB} 装着生物（独立物品 id {@code net_has_item}）；{@code INIT} 未实际使用。
 */
public enum Type {

    INIT,
    EMPTY,
    HAS_MOB,
}