package com.github.yzqdev.pethome.platform;

/**
 * 3-tuple with public fields，替代 antlr 的 Triple（NeoForge 传递依赖）。
 */
public class Triple<A, B, C> {
    public final A a;
    public final B b;
    public final C c;

    public Triple(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}
