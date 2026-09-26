package com.github.yzqdev.pethome.server.misc;

import net.minecraft.core.particles.SimpleParticleType;

/**
 * SimpleParticleType 构造器在 vanilla 中为 protected（NeoForge 开放为 public）。
 */
public class PHParticleType extends SimpleParticleType {
    public PHParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }
}
