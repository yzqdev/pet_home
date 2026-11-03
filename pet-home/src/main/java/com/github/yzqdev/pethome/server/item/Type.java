package com.github.yzqdev.pethome.server.item;

/** 捕网（net）内状态的三值枚举：INIT=刚合成/未开封，EMPTY=空网可捕捉，HAS_MOB=已装着生物。 */
public enum Type {
  INIT,
  EMPTY,
  HAS_MOB,
}
