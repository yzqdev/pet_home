package com.github.yzqdev.pethome.compat.geckolib;

import com.github.yzqdev.pethome.client.render.PHRenderTypes;
import com.github.yzqdev.pethome.client.render.PetOverlayRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * GeckoLib 生物的宠物 overlay 适配层（4.x 即时渲染架构）。
 *
 * <p>4.x 的 {@code GeoRenderer.defaultRender} 在 actuallyRender 的 push/pop 帧<strong>之外</strong>
 * 调 {@code applyRenderLayers}，layer 调用点的 poseStack 只处于「实体脚底原点、Y-up、无 body 旋转、
 * 无翻转」状态（withScale 缩放除外）。本层在此基准上正向重放原版 {@code LivingEntityRenderer.render}
 * 的 layer 前变换链（nativeScale → SLEEPING 平移 → setupRotations → flip(-1,-1,1) →
 * translate(0,-1.501,0)），之后绘制与原版路径共用的 {@link PetOverlayRenderer}。
 *
 * <p>泛型：T 同时 extends Entity 与 GeoAnimatable（GeoEntityRenderer）或为替换动画对象
 * （GeoReplacedEntityRenderer，实体经 {@code getCurrentEntity()} 取），本层只消费 LivingEntity。
 */
@SuppressWarnings({"unchecked", "rawtypes"})
final class GeoPetOverlayLayer extends GeoRenderLayer {

  private static java.lang.reflect.Field scaleWidthField;
  private static java.lang.reflect.Field scaleHeightField;

  GeoPetOverlayLayer(GeoRenderer renderer) {
    super(renderer);
  }

  /** scaleWidth/scaleHeight 是 GeoEntityRenderer/GeoReplacedEntityRenderer 的 protected 字段，反射读；失败按 1（无缩放）处理。 */
  private float readScale(String fieldName) {
    try {
      if ("scaleWidth".equals(fieldName)) {
        if (scaleWidthField == null) {
          scaleWidthField = this.renderer.getClass().getSuperclass().getDeclaredField("scaleWidth");
          scaleWidthField.setAccessible(true);
        }
        return scaleWidthField.getFloat(this.renderer);
      }
      if (scaleHeightField == null) {
        scaleHeightField = this.renderer.getClass().getSuperclass().getDeclaredField("scaleHeight");
        scaleHeightField.setAccessible(true);
      }
      return scaleHeightField.getFloat(this.renderer);
    } catch (Exception e) {
      return 1.0F;
    }
  }

  @Override
  public void render(PoseStack poseStack, software.bernie.geckolib.animatable.GeoAnimatable animatable, BakedGeoModel bakedModel, RenderType renderType,
                     MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                     int packedLight, int packedOverlay) {
    LivingEntity living = this.renderer.getAnimatable() instanceof LivingEntity entity ? entity : null;
    if (living == null) {
      return;
    }

    poseStack.pushPose();

    // 撤销 withScale 残留缩放（4.9.2 的 preRender 把 scaleWidth/scaleHeight 乘到栈上且不撤销，
    // 不撤销会让下方 nativeScale/−1.501 平移等被一并缩放；protected 字段，反射读）
    float w = readScale("scaleWidth");
    float h = readScale("scaleHeight");
    if (w != 0F && h != 0F && (w != 1F || h != 1F)) {
      poseStack.scale(1.0F / w, 1.0F / h, 1.0F / w);
    }

    // ===== 原版 LivingEntityRenderer.render 的 layer 前变换 =====
    float scale = living.getScale();
    poseStack.scale(scale, scale, scale);
    if (living.hasPose(Pose.SLEEPING)) {
      Direction bedOrientation = living.getBedOrientation();
      if (bedOrientation != null) {
        float headOffset = living.getEyeHeight(Pose.STANDING) - 0.1F;
        poseStack.translate(
            -bedOrientation.getStepX() * headOffset,
            0.0F,
            -bedOrientation.getStepZ() * headOffset);
      }
    }
    setupRotations(living, poseStack, partialTick, scale);
    poseStack.scale(-1.0F, -1.0F, 1.0F);
    poseStack.translate(0.0F, -1.501F, 0.0F);

    PetOverlayRenderer.renderOverlays(living, poseStack, bufferSource, packedLight, partialTick,
        color -> submitIframe(poseStack, bufferSource, living, bakedModel, partialTick, packedLight));

    poseStack.popPose();
  }

  /** GeoEntityRenderer 的 animatable 就是实体；GeoReplacedEntityRenderer 的 getAnimatable 返回替换动画对象，
   *  其实例字段在 render 时可能为 null——两路径都可能在 render 间隙拿不到实体，需空值兜底。 */

  /**
   * 免疫护盾叠加：GeckoLib 路径没有原版「父模型」概念，用 GeckoLib 自身烘焙模型按 GLINT RenderType
   * 重渲染一层。坐标系与本体 actuallyRender 同构（nativeScale + SLEEPING 平移 + setupRotations，
   * 不含 flip），朝向必然一致。
   */
  private void submitIframe(PoseStack poseStack, MultiBufferSource bufferSource, LivingEntity living,
                            BakedGeoModel bakedModel, float partialTick, int packedLight) {
    VertexConsumer buffer = bufferSource.getBuffer(PHRenderTypes.IFRAME_GLINT);
    for (Object boneObj : bakedModel.topLevelBones()) {
      this.renderer.renderRecursively(poseStack, this.renderer.getAnimatable(),
          (software.bernie.geckolib.cache.object.GeoBone) boneObj, PHRenderTypes.IFRAME_GLINT,
          bufferSource, buffer, true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, -1);
    }
  }

  /** 逐行复刻原版 {@code LivingEntityRenderer.setupRotations}（实体字段版）。 */
  private static void setupRotations(LivingEntity living, PoseStack poseStack, float partialTick, float entityScale) {
    float bodyRot = Mth.rotLerp(partialTick, living.yBodyRotO, living.yBodyRot);
    float ageInTicks = living.tickCount + partialTick;

    if (living.isFullyFrozen()) {
      bodyRot += (float) (Math.cos(Mth.floor(ageInTicks) * 3.25F) * Math.PI * 0.4F);
    }

    if (!living.hasPose(Pose.SLEEPING)) {
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));
    }

    if (living.deathTime > 0.0F) {
      float fall = (living.deathTime - 1.0F) / 20.0F * 1.6F;
      fall = Mth.sqrt(fall);
      if (fall > 1.0F) {
        fall = 1.0F;
      }
      poseStack.mulPose(Axis.ZP.rotationDegrees(fall * 90.0F));
    } else if (living.isAutoSpinAttack()) {
      poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F - living.getXRot()));
      poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * -75.0F));
    } else if (living.hasPose(Pose.SLEEPING)) {
      Direction bedOrientation = living.getBedOrientation();
      float angle = bedOrientation != null ? sleepDirectionToRotation(bedOrientation) : bodyRot;
      poseStack.mulPose(Axis.YP.rotationDegrees(angle));
      poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
      poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
    } else if (net.minecraft.client.renderer.entity.LivingEntityRenderer.isEntityUpsideDown(living)) {
      poseStack.translate(0.0F, (living.getBbHeight() + 0.1F) / entityScale, 0.0F);
      poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
    }
  }

  private static float sleepDirectionToRotation(Direction direction) {
    return switch (direction) {
      case SOUTH -> 90.0F;
      case WEST -> 0.0F;
      case NORTH -> 270.0F;
      case EAST -> 180.0F;
      default -> 0.0F;
    };
  }
}
