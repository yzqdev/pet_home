package com.github.yzqdev.pethome.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/** 磁力（MAGNETIC 附魔）吸取粒子：短暂存续（1~2 tick）、全亮度发光并快速减速消散的小光点。 */
public class ParticleMagnet extends SimpleAnimatedParticle {

  private ParticleMagnet(
      ClientLevel world,
      double x,
      double y,
      double z,
      double motionX,
      double motionY,
      double motionZ,
      SpriteSet sprites) {
    super(world, x, y, z, sprites, 0.0F);
    this.xd = (float) motionX;
    this.yd = (float) motionY;
    this.zd = (float) motionZ;
    this.quadSize = 0.1F + this.random.nextFloat() * 0.1F;
    this.lifetime = 1 + this.random.nextInt(2);
    this.gravity = 0;
    this.pickSprite(sprites);
  }

  public int getLightColor(float f) {
    return 240;
  }

  public void tick() {
    super.tick();
    this.xd *= 0.2D;
    this.yd *= 0.2D;
    this.zd *= 0.2D;
    this.setAlpha(1F - (this.age / (float) this.lifetime));
  }

  @Override
  public ParticleRenderType getRenderType() {
    return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
  }

  @Environment(EnvType.CLIENT)
  public static class Factory implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet spriteSet;

    public Factory(SpriteSet spriteSet) {
      this.spriteSet = spriteSet;
    }

    public Particle createParticle(
        SimpleParticleType typeIn,
        ClientLevel worldIn,
        double x,
        double y,
        double z,
        double xSpeed,
        double ySpeed,
        double zSpeed) {
      ParticleMagnet p = new ParticleMagnet(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
      return p;
    }
  }
}
