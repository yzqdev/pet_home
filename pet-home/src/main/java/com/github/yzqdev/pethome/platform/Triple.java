package com.github.yzqdev.pethome.platform;

/** 简单三元组：替代 Forge 版依赖的 antlr Triple（antlr 在 Fabric 运行时类路径上不保证存在）。 */
public record Triple<A, B, C>(A a, B b, C c) {
}
