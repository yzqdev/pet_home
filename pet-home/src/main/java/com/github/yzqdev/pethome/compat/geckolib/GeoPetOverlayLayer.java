package com.github.yzqdev.pethome.compat.geckolib;

import com.github.yzqdev.pethome.client.render.PHRenderTypes;
import com.github.yzqdev.pethome.client.render.PetOverlayRenderer;
import com.github.yzqdev.pethome.client.render.TextureSizer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.lang.reflect.Field;

/**
 * GeckoLib 生物的宠物 overlay 适配层（4.x 即时渲染架构，1.20.1 GeckoLib 4.8.4）。
 *
 * <p>4.x 的 {@code GeoRenderer.defaultRender} 在 actuallyRender 的 push/pop 帧<strong>之外</strong>
 * 调 {@code applyRenderLayers}，layer 调用点的 poseStack 处于「实体脚底原点、Y-up、无 body 旋转、
 * 无翻转」状态，但 {@code withScale} 的缩放（preRender 中乘入）留在栈上。本层先撤销该缩放，
 * 再正向重放原版 {@code LivingEntityRenderer.render} 的 layer 前变换链（SLEEPING 平移 →
 * setupRotations → flip(-1,-1,1) → translate(0,-1.501,0)），之后绘制与原版路径共用的
 * {@link PetOverlayRenderer}。
 *
 * <p>注意 4.8.4 与 4.9.2 的差异：本体渲染不乘 nativeScale（getScale），故此处也不补。
 */
@SuppressWarnings({"unchecked", "rawtypes"})
final class GeoPetOverlayLayer extends GeoRenderLayer {

    private static Field scaleWidthField;
    private static Field scaleHeightField;

    GeoPetOverlayLayer(GeoRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, software.bernie.geckolib.core.animatable.GeoAnimatable animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        LivingEntity living = this.renderer.getAnimatable() instanceof LivingEntity entity ? entity : null;
        if (living == null) {
            return;
        }

        poseStack.pushPose();

        // 撤销 withScale 残留缩放（scaleWidth/scaleHeight 是 protected，反射读；0 值防御）
        float w = readScale("scaleWidth");
        float h = readScale("scaleHeight");
        if (w != 0F && h != 0F && (w != 1F || h != 1F)) {
            poseStack.scale(1.0F / w, 1.0F / h, 1.0F / w);
        }

        // ===== 原版 LivingEntityRenderer.render 的 layer 前变换 =====
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
        setupRotations(living, poseStack, partialTick);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        PetOverlayRenderer.renderOverlays(living, poseStack, bufferSource, packedLight, partialTick,
            (texture, r, g, b, a) -> submitParentModel(poseStack, bufferSource, living, bakedModel, partialTick, packedLight, texture, r, g, b, a));

        poseStack.popPose();
    }

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

    /**
     * 原版父模型类回调（免疫护盾/僵尸 overlay）：GeckoLib 路径没有「父模型」概念，用 GeckoLib 自身烘焙模型
     * 按对应 RenderType 重渲染一层。坐标系与本体 actuallyRender 同构（SLEEPING 平移 + setupRotations，
     * 不含 flip），朝向必然一致。4.8.4 的 renderRecursively 颜色是 float r,g,b,a。
     */
    private void submitParentModel(PoseStack poseStack, MultiBufferSource bufferSource, LivingEntity living,
                                   BakedGeoModel bakedModel, float partialTick, int packedLight,
                                   Object texture, float r, float g, float b, float a) {
        net.minecraft.resources.ResourceLocation rl = (net.minecraft.resources.ResourceLocation) texture;
        RenderType type = rl == null ? PHRenderTypes.IFRAME_GLINT
            : PHRenderTypes.getZombieOverlay(rl,
                TextureSizer.getTextureWidth(rl).getFirst(),
                TextureSizer.getTextureWidth(rl).getSecond());
        VertexConsumer buffer = bufferSource.getBuffer(type);
        for (software.bernie.geckolib.cache.object.GeoBone bone : bakedModel.topLevelBones()) {
            this.renderer.renderRecursively(poseStack, this.renderer.getAnimatable(), bone, type,
                bufferSource, buffer, true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, a);
        }
    }

    /** 逐行复刻原版 {@code LivingEntityRenderer.setupRotations}（实体字段版）。 */
    private static void setupRotations(LivingEntity living, PoseStack poseStack, float partialTick) {
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
        } else if (isUpsideDown(living)) {
            poseStack.translate(0.0F, living.getBbHeight() + 0.1F, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
    }

    private static boolean isUpsideDown(LivingEntity living) {
        if (!(living.hasCustomName() || living instanceof Player)) {
            return false;
        }
        String name = living.getName().getString();
        if (living instanceof Player player) {
            if (!player.isModelPartShown(PlayerModelPart.CAPE)) {
                return false;
            }
        } else {
            name = ChatFormatting.stripFormatting(name);
        }
        return name != null && (name.equals("Dinnerbone") || name.equalsIgnoreCase("Grumm"));
    }

    private static float sleepDirectionToRotation(Direction direction) {
        switch (direction) {
            case SOUTH:
                return 90.0F;
            case WEST:
                return 0.0F;
            case NORTH:
                return 270.0F;
            case EAST:
                return 180.0F;
            default:
                return 0.0F;
        }
    }
}
