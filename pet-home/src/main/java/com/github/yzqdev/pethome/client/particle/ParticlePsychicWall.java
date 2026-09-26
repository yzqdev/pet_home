package com.github.yzqdev.pethome.client.particle;

import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;

public class ParticlePsychicWall extends SimpleAnimatedParticle {
    private final Direction direction;

    ParticlePsychicWall(ClientLevel lvl, double x, double y, double z, Direction direction, SpriteSet set) {
        super(lvl, x, y, z, set, 0F);
        this.setSize(1, 1);
        this.gravity = 0.0F;
        this.direction = direction;
        this.lifetime = 5 + this.random.nextInt(7);
        this.quadSize = 0.15F + this.random.nextFloat() * 0.35F;
        this.setFadeColor(0xFFFFFF);
        this.setSpriteFromAge(set);
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTick) {
        float size = this.getQuadSize(partialTick);
        Quaternionf rot1 = new Quaternionf(direction.getRotation());
        rot1.mul(Axis.XP.rotation((float) Math.PI * 0.5F));
        this.extractRotatedQuad(state, camera, rot1, size);

        Quaternionf rot2 = new Quaternionf(direction.getRotation());
        rot2.mul(Axis.XP.rotation((float) Math.PI * 0.5F));
        rot2.mul(Axis.YP.rotation((float) Math.PI));
        this.extractRotatedQuad(state, camera, rot2, size);
    }

    @Override
    public int getLightCoords(float partialTick) {
        return 240;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            Direction from = Direction.from3DDataValue((int) xSpeed);
            return new ParticlePsychicWall(world, x, y, z, from, spriteSet);
        }
    }
}