package com.github.yzqdev.pet_home.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;


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

    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        this.renderTexturedParticle(consumer, camera, partialTick, (rot) -> {
            rot.mul(direction.getRotation());
            rot.mul(Axis.XP.rotation((float) Math.PI * 0.5F));
        });
        this.renderTexturedParticle(consumer, camera, partialTick, (rot) -> {
            rot.mul(direction.getRotation());
            rot.mul(Axis.XP.rotation((float) Math.PI * 0.5F));
            rot.mul(Axis.YP.rotation(-(float)Math.PI));
        });
    }

    private void renderTexturedParticle(VertexConsumer consumer, Camera camera, float partialTick, Consumer<Quaternionf> rot) {
        Vec3 vec3 = camera.getPosition();
        float f = (float)(Mth.lerp(partialTick, this.xo, this.x) - vec3.x());
        float f1 = (float)(Mth.lerp(partialTick, this.yo, this.y) - vec3.y());
        float f2 = (float)(Mth.lerp(partialTick, this.zo, this.z) - vec3.z());
        Vector3f vector3f = new Vector3f(0.5F, 0.5F, 0.5F);
        vector3f.normalize();
        Quaternionf quaternion = new Quaternionf().setAngleAxis(0.0F, vector3f.x(), vector3f.y(), vector3f.z());
        rot.accept(quaternion);
        Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
        vector3f1.rotate(quaternion);
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f3 = this.getQuadSize(partialTick);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f2 = avector3f[i];
            vector3f2.rotate(quaternion);
            vector3f2.mul(f3);
            vector3f2.add(f, f1, f2);
        }

        float f6 = this.getU0();
        float f7 = this.getU1();
        float f4 = this.getV0();
        float f5 = this.getV1();
        int j = this.getLightColor(partialTick);
        consumer.addVertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).setUv(f7, f5).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        consumer.addVertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).setUv(f7, f4).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        consumer.addVertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).setUv(f6, f4).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        consumer.addVertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).setUv(f6, f5).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
    }

    public int getLightColor(float p_172469_) {
        return 240;
    }


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
            Direction from = Direction.from3DDataValue((int)xSpeed);
            return new ParticlePsychicWall(worldIn, x, y, z, from, spriteSet);
        }
    }
}
