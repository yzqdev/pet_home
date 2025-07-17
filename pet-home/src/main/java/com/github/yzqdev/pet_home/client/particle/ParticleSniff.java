package com.github.yzqdev.pet_home.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


public class ParticleSniff extends SimpleAnimatedParticle {
    private float targetX = 0;
    private float targetY = 0;
    private float targetZ = 0;

    private ParticleSniff(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ, SpriteSet sprites) {
        super(world, x, y, z, sprites, 0.0F);
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        float grey = random.nextFloat() * 0.2F;
        this.rCol = 0.5F + grey;
        this.gCol = 0.5F + grey;
        this.bCol = 0.5F + grey;
        this.targetX = (float) motionX;
        this.targetY = (float) motionY;
        this.targetZ = (float) motionZ;
        this.quadSize = 0.3F;
        this.lifetime = 10 + random.nextInt(10);
        this.pickSprite(sprites);
    }

    public void tick() {
        super.tick();
        float speed = 1F / (float) lifetime;
        this.quadSize = 0.3F - 0.2F * (age / (float) lifetime);
        double moveX = targetX - x;
        double moveY = targetY - y;
        double moveZ = targetZ - z;
        this.setAlpha(1F - (age / (float) lifetime));
        this.xd += moveX * speed;
        this.yd += moveY * speed;
        this.zd += moveZ * speed;
        this.xd *= 0.8;
        this.yd *= 0.8;
        this.zd *= 0.8;
    }

    public int getLightColor(float p_107249_) {
        BlockPos blockpos = BlockPos.containing(this.x, this.y, this.z);
        return this.level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor(this.level, blockpos) : 0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ParticleSniff p = new ParticleSniff(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
            return p;
        }
    }
}
