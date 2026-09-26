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

public class ParticleMagnet extends SimpleAnimatedParticle {
    private ParticleMagnet(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ, SpriteSet set) {
        super(world, x, y, z, set, 0.0F);
        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;
        this.quadSize = 0.25F;
        this.lifetime = 30;
        this.setSpriteFromAge(set);
        this.hasPhysics = true;
    }

    @Override
    public int getLightCoords(float partialTick) {
        BlockPos blockpos = BlockPos.containing(this.x, this.y, this.z);
        return this.level.isLoaded(blockpos) ? LevelRenderer.getLightCoords(this.level, blockpos) : 0;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet set) {
            this.sprites = set;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new ParticleMagnet(world, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}