package com.github.yzqdev.pethome.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class ParticleIntimidation extends SimpleAnimatedParticle {

    ParticleIntimidation(ClientLevel lvl, double x, double y, double z, SpriteSet set) {
        super(lvl, x, y, z, set, 0.0F);
        this.setSize(1, 1);
        this.gravity = 0.0F;
        this.lifetime = 22 + random.nextInt(7);
        this.setSpriteFromAge(set);
    }

    @Override
    public int getLightCoords(float partialTick) {
        BlockPos blockpos = BlockPos.containing(this.x, this.y, this.z);
        return this.level.isLoaded(blockpos) ? LevelRenderer.getLightCoords(this.level, blockpos) : 0;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new ParticleIntimidation(world, x, y, z, spriteSet);
        }
    }
}