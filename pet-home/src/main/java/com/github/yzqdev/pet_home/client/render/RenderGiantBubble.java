package com.github.yzqdev.pet_home.client.render;
import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.server.entity.GiantBubbleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
public class RenderGiantBubble extends EntityRenderer<GiantBubbleEntity> {

    private static final ResourceLocation TEXTURE =  ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "textures/giant_bubble.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE);

    public RenderGiantBubble(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void render(GiantBubbleEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        super.render(entity, yaw, partialTicks, poseStack, buffer, light);
        poseStack.pushPose();
        float age = entity.tickCount + partialTicks;
        float bubbleWobbleXZ = (float)Math.sin(age * 0.3F) * 0.2F;
        float bubbleWobbleY = (float)Math.cos(age * 0.3F) * 0.2F;
        poseStack.scale(2.6F + bubbleWobbleXZ, 2.6F + bubbleWobbleY, 2.6F + bubbleWobbleXZ);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(new Quaternionf().rotateY(180F * ((float)Math.PI / 180F)));
        PoseStack.Pose posestack$pose = poseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();
        VertexConsumer vertexconsumer = buffer.getBuffer(RENDER_TYPE);
        vertex(vertexconsumer, matrix4f, posestack$pose, light, 0.0F, 0, 0, 1);
        vertex(vertexconsumer, matrix4f, posestack$pose, light, 1.0F, 0, 1, 1);
        vertex(vertexconsumer, matrix4f, posestack$pose, light, 1.0F, 1, 1, 0);
        vertex(vertexconsumer, matrix4f, posestack$pose, light, 0.0F, 1, 0, 0);
        poseStack.popPose();

    }

    private static void vertex(VertexConsumer p_114090_, Matrix4f p_114091_, PoseStack.Pose p_114092_, int p_114093_, float p_114094_, int p_114095_, int p_114096_, int p_114097_) {
        p_114090_.addVertex(p_114091_, p_114094_ - 0.5F, (float)p_114095_ - 0.25F, 0.0F).setColor(255, 255, 255, 255).setUv((float)p_114096_, (float)p_114097_).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(p_114093_, p_114093_).setNormal(p_114092_, 0.0F, 1.0F, 0.0F) ;
    }

    @Override
    public ResourceLocation getTextureLocation(GiantBubbleEntity entity) {
        return TEXTURE;
    }

}