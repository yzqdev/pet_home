package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.entity.GiantBubbleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionf;

public class RenderGiantBubble extends EntityRenderer<GiantBubbleEntity, EntityRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "textures/giant_bubble.png");

    public RenderGiantBubble(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void extractRenderState(GiantBubbleEntity entity, EntityRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        poseStack.pushPose();
        float age = state.ageInTicks;
        float bubbleWobbleXZ = (float) Math.sin(age * 0.3F) * 0.2F;
        float bubbleWobbleY = (float) Math.cos(age * 0.3F) * 0.2F;
        poseStack.scale(2.6F + bubbleWobbleXZ, 2.6F + bubbleWobbleY, 2.6F + bubbleWobbleXZ);
        poseStack.mulPose(camera.orientation);
        poseStack.mulPose(new Quaternionf().rotateY(180F * ((float) Math.PI / 180F)));

        var renderType = RenderTypes.entityCutout(TEXTURE);
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            var matrix4f = pose.pose();
            var matrix3f = pose.normal();
            vertex(buffer, matrix4f, pose, state.lightCoords, 0.0F, 0, 0, 1);
            vertex(buffer, matrix4f, pose, state.lightCoords, 1.0F, 0, 1, 1);
            vertex(buffer, matrix4f, pose, state.lightCoords, 1.0F, 1, 1, 0);
            vertex(buffer, matrix4f, pose, state.lightCoords, 0.0F, 1, 0, 0);
        });
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer p_114090_, org.joml.Matrix4f p_114091_, PoseStack.Pose p_114092_, int p_114093_, float p_114094_, int p_114095_, int p_114096_, int p_114097_) {
        p_114090_.addVertex(p_114091_, p_114094_ - 0.5F, (float) p_114095_ - 0.25F, 0.0F).setColor(255, 255, 255, 255).setUv((float) p_114096_, (float) p_114097_).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(p_114093_, p_114093_).setNormal(p_114092_, 0.0F, 1.0F, 0.0F);
    }
}