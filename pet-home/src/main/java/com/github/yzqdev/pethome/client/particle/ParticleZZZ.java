package com.github.yzqdev.pethome.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/** “Zzz” 睡觉粒子：宠物停留/睡眠时头顶冒出，绕出生点螺旋上升并随年龄增亮。 */
public class ParticleZZZ extends SimpleAnimatedParticle {

  /** 螺旋相位与方向的随机化参数（让多个 Z 各自错开）。 */
  private float sinOffset;

  private float cosOffset;
  private float rotationDir;

  /** 螺旋圆心（出生位置）。 */
  private float initialX;

  private float initialZ;

  private ParticleZZZ(
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
    this.lifetime = 16 + this.random.nextInt(6);
    this.gravity = -0.08F;
    this.sinOffset = this.random.nextFloat();
    this.cosOffset = this.random.nextFloat();
    this.rotationDir = this.random.nextBoolean() ? 1 : -1;
    this.initialX = (float) x;
    this.initialZ = (float) z;
    this.pickSprite(sprites);
  }

  /** 每 tick 朝螺旋目标点收束速度（正弦/余弦圆周轨迹）并淡出。 */
  public void tick() {
    super.tick();
    float xTarget =
        initialX + (float) Math.cos(this.age * 0.3F + this.cosOffset * 4) * 0.2F * rotationDir;
    float zTarget =
        initialZ + (float) Math.sin(this.age * 0.3F + this.sinOffset * 4) * 0.2F * rotationDir;
    this.xd = 0.3F * (xTarget - x);
    this.zd = 0.3F * (zTarget - z);
    this.setAlpha(1F - (this.age / (float) this.lifetime));
  }

  public int getLightColor(float partialTicks) {
    float f = ((float) this.age + partialTicks) / (float) this.lifetime;
    f = Mth.clamp(f, 0.0F, 1.0F);
    int i = super.getLightColor(partialTicks);
    int j = i & 255;
    int k = i >> 16 & 255;
    j += (int) (f * 15.0F * 16.0F);
    if (j > 240) {
      j = 240;
    }
    return j | k << 16;
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
      return new ParticleZZZ(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
    }
  }
}
