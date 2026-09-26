package com.github.yzqdev.pethome.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/** 问号粒子（混乱之脑：混乱的怪物头顶显示），自 1.21 移植 */
public class ParticleQuestionMark extends SimpleAnimatedParticle {
    protected ParticleQuestionMark(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ, SpriteSet sprites) {
        super(world, x, y, z, sprites, 0.0F);
        this.lifetime = 100;

        this.quadSize = 0.1F + this.random.nextFloat() * 0.1F;
        this.lifetime = 1 + this.random.nextInt(2);
        this.gravity = 0;
        this.pickSprite(sprites);

        this.hasPhysics = true;
    }

    public int getLightColor(float f) {
        return 240;
    }


    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }


    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ParticleQuestionMark(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
