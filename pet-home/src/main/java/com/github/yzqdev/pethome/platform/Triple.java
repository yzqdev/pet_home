package com.github.yzqdev.pethome.platform;

/**
 * Simple 3-tuple replacing the antlr Triple used by the Forge version (antlr is not
 * guaranteed on the Fabric runtime classpath).
 */
public record Triple<A, B, C>(A a, B b, C c) {
}
