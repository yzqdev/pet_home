package com.github.yzqdev.pethome.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;

/**
 * 原版 RenderLayer 路径的宠物 overlay 薄壳：绘制逻辑全部在 {@link PetOverlayRenderer}， 与 GeckoLib
 * 适配路径（compat.geckolib.GeoPetOverlayLayer）共用。
 */
public class LayerPetOverlays
        extends RenderLayer<LivingEntityRenderState, EntityModel<? super LivingEntityRenderState>> {

    public LayerPetOverlays(
            RenderLayerParent<LivingEntityRenderState, EntityModel<? super LivingEntityRenderState>> parent) {
        super(parent);
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            LivingEntityRenderState state,
            float yRot,
            float xRot) {
        // 从客户端 level 反查实体
        LivingEntity living = PetOverlayRenderer.findEntity(state);
        if (living == null) return;
        PetOverlayRenderer.renderOverlays(
                living,
                state,
                poseStack,
                submitNodeCollector,
                lightCoords,
                color ->
                        // 免疫护盾以叠加色重提交父模型（tintedColor 位置传 color）
                        submitIframeModel(submitNodeCollector, state, poseStack, lightCoords, color));
    }

    /**
     * 免疫护盾叠加：以 tintedColor=color 重提交父模型。10 参原生 submitModel 的 {@code Model<? super S>} 通配捕获
     * 在 lambda 推断下失败（EntityModel&lt;CAP#1&gt; 不匹配），用裸类型绕开。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void submitIframeModel(
            SubmitNodeCollector submitNodeCollector,
            LivingEntityRenderState state,
            PoseStack poseStack,
            int lightCoords,
            int color) {
        submitNodeCollector.submitModel(
                (Model) getParentModel(),
                state,
                poseStack,
                PHRenderTypes.IFRAME_GLINT,
                lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0),
                color,
                null,
                0,
                null);
    }
}
