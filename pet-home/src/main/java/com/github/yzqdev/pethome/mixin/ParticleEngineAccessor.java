package com.github.yzqdev.pethome.mixin;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * {@link ParticleEngine} 的 Accessor：暴露私有 {@code particles} 组表（render type → group，
 * identity map），供 {@code client.ParticleEngineExtractMixin} 在 extract 尾部补提 PH_PHANTOM 组。
 */
@Mixin(ParticleEngine.class)
public interface ParticleEngineAccessor {

    @Accessor("particles")
    Map<ParticleRenderType, ParticleGroup<?>> pet_home$getParticles();
}
