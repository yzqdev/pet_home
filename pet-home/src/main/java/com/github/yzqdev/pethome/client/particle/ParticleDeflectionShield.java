package com.github.yzqdev.pethome.client.particle;

import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** 偏转护盾粒子：渲染护盾物品模型（1.21 侧行为），随淡入/淡出升降与缩放。 */
public class ParticleDeflectionShield extends PhantomParticleGroup.PhantomParticle {

  private final float xRot;
  private final float yRot;

  ParticleDeflectionShield(ClientLevel lvl, double x, double y, double z, float xRot, float yRot) {
    super(lvl, x, y, z);
    this.lifetime = 15 + this.random.nextInt(7);
    this.xRot = xRot;
    this.yRot = yRot;
  }

  @Override
  PhantomParticleGroup.ParticleData extractData(Camera camera, float partialTick) {
    float lerpAge = this.age + partialTick;
    float[] fade = fade(lerpAge, this.lifetime);
    float fadeIn = fade[0];
    float fadeOut = fade[1];

    ItemStackRenderState itemState = new ItemStackRenderState();
    Minecraft.getInstance()
        .getItemModelResolver()
        .updateForTopItem(
            itemState,
            new ItemStack(PHItemRegistry.DEFLECTION_SHIELD.get()),
            ItemDisplayContext.GROUND,
            this.level,
            null,
            0);

    float up = -1F + fadeIn + (float) Math.sin(lerpAge * 0.3F) * 0.03F;
    float down = -1F * fadeOut;
    float scale = (fadeIn - fadeOut) * 1.85F;
    return new Data(
        new Vec3(this.x, this.y, this.z), itemState, this.xRot, this.yRot, up + down, scale);
  }

  private record Data(
      Vec3 pos, ItemStackRenderState itemState, float xRot, float yRot, float yLift, float scale)
      implements PhantomParticleGroup.ParticleData {

    @Override
    public void submit(
        PoseStack poseStack,
        SubmitNodeCollector collector,
        CameraRenderState camera,
        Vec3 cameraPos) {
      if (this.scale <= 0.01F || this.itemState.isEmpty()) {
        return;
      }
      Vec3 rel = this.relative(cameraPos);
      poseStack.pushPose();
      poseStack.translate(rel.x, rel.y + this.yLift, rel.z);
      // 护盾绕 X 轴立起 45°，随淡入/淡出在 0°~45° 间摆动（xRot + 45*(fadeIn-fadeOut)）
      poseStack.mulPose(Axis.YP.rotationDegrees(this.yRot));
      poseStack.mulPose(Axis.XN.rotationDegrees(this.xRot));
      poseStack.scale(this.scale, this.scale, this.scale);
      this.itemState.submit(poseStack, collector, 240, OverlayTexture.NO_OVERLAY, 0);
      poseStack.popPose();
    }
  }

  public record Factory() implements ParticleProvider<SimpleParticleType> {
    @Override
    public ParticleDeflectionShield createParticle(
        SimpleParticleType type,
        ClientLevel world,
        double x,
        double y,
        double z,
        double xSpeed,
        double ySpeed,
        double zSpeed,
        RandomSource random) {
      return new ParticleDeflectionShield(world, x, y, z, (float) xSpeed, (float) ySpeed);
    }
  }
}
