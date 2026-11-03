package com.github.yzqdev.pethome.compat.geckolib;

import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.github.yzqdev.pethome.client.render.PHRenderTypes;
import com.github.yzqdev.pethome.client.render.PetOverlayRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

/**
 * GeckoLib 生物的宠物 overlay 适配层。
 *
 * <p>GeckoLib 渲染器的 layer 调用点（{@code submitRenderTask}）的 poseStack 处于模型 自有变换（缩放/朝向）之后，与原版 {@code
 * LivingEntityRenderer} 的 layer 提交点语义不同。 本层先用 {@code
 * renderPassInfo.getPreRenderMatrixPose()}（模型变换前的实体空间）重置 poseStack，再逐行复刻原版 layer 提交变换（SLEEPING 平移 →
 * scale → setupRotations → flip(-1,-1,1) → translate(0,-1.501,0)），之后绘制与原版路径共用 {@link
 * PetOverlayRenderer}。
 */
@SuppressWarnings({"unchecked", "rawtypes"})
final class GeoPetOverlayLayer extends GeoRenderLayer {

    GeoPetOverlayLayer(GeoRenderer renderer) {
        super(renderer);
    }

    @Override
    public void submitRenderTask(RenderPassInfo renderPassInfo, SubmitNodeCollector renderTasks) {
        if (!(renderPassInfo.renderState() instanceof LivingEntityRenderState state)) return;

        LivingEntity living = PetOverlayRenderer.findEntity(state);
        if (living == null) return;

        // 以「模型变换前」的实体空间为基准重建 poseStack（复刻原版 submit 的 layer 调用点变换链）
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(renderPassInfo.getPreRenderMatrixPose().pose());

        // ===== 原版 LivingEntityRenderer.submit 的 layer 前变换 =====
        if (state.hasPose(Pose.SLEEPING)) {
            Direction bedOrientation = state.bedOrientation;
            if (bedOrientation != null) {
                float headOffset = state.eyeHeight - 0.1F;
                poseStack.translate(
                        -bedDirection(bedOrientation, true) * headOffset,
                        0.0F,
                        -bedDirection(bedOrientation, false) * headOffset);
            }
        }

        float scale = state.scale;
        poseStack.scale(scale, scale, scale);
        setupRotations(state, poseStack, state.bodyRot, scale);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        PetOverlayRenderer.renderOverlays(
                living,
                state,
                poseStack,
                renderTasks,
                renderPassInfo.packedLight(),
                color -> submitIframe(renderPassInfo, poseStack, renderTasks, state, color));
    }

    /**
     * 免疫护盾叠加：GeckoLib 路径没有原版「父模型」概念，用 GeckoLib 自身烘焙模型按 GLINT RenderType 重提交一层（顶点格式与原版实体一致，已核实
     * GeoQuad.render）。
     */
    private void submitIframe(
            RenderPassInfo renderPassInfo,
            PoseStack poseStack,
            SubmitNodeCollector renderTasks,
            LivingEntityRenderState state,
            int color) {
        // submitCustomGeometry 的 pose 参数是提交时快照；模型渲染需要骨骼动画，走 renderPosed
        renderTasks.submitCustomGeometry(
                poseStack,
                PHRenderTypes.IFRAME_GLINT,
                (pose, buffer) ->
                        renderPassInfo.renderPosed(
                                () ->
                                        renderPassInfo
                                                .model()
                                                .render(renderPassInfo, buffer, renderPassInfo.packedLight(), 0, -1)));
    }

    /** 逐行复刻原版 {@code LivingEntityRenderer.setupRotations}（GeckoLib applyRotations 与之同构）。 */
    private static void setupRotations(
            LivingEntityRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        if (state.isFullyFrozen) {
            bodyRot += (float) (Math.cos(Mth.floor(state.ageInTicks) * 3.25F) * Math.PI * 0.4F);
        }

        if (!state.hasPose(Pose.SLEEPING)) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));
        }

        if (state.deathTime > 0.0F) {
            float fall = (state.deathTime - 1.0F) / 20.0F * 1.6F;
            fall = Mth.sqrt(fall);
            if (fall > 1.0F) {
                fall = 1.0F;
            }
            poseStack.mulPose(Axis.ZP.rotationDegrees(fall * 90.0F));
        } else if (state.isAutoSpinAttack) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F - state.xRot));
            poseStack.mulPose(Axis.YP.rotationDegrees(state.ageInTicks * -75.0F));
        } else if (state.hasPose(Pose.SLEEPING)) {
            Direction bedOrientation = state.bedOrientation;
            float angle = bedOrientation != null ? sleepDirectionToRotation(bedOrientation) : bodyRot;
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
        } else if (state.isUpsideDown) {
            poseStack.translate(0.0F, (state.boundingBoxHeight + 0.1F) / entityScale, 0.0F);
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

    private static float bedDirection(Direction direction, boolean x) {
        return x ? direction.getStepX() : direction.getStepZ();
    }
}
