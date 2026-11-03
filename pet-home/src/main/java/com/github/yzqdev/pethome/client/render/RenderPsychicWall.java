package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.entity.PsychicWallEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** 念力墙实体渲染器：按墙体朝向绘制半透明墙面板 + 边框两层，前 10 tick 生长展开、临近消失时收缩。 */
public class RenderPsychicWall extends EntityRenderer<PsychicWallEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(PetHomeMod.MODID, "textures/psychic_wall_border.png");

    public RenderPsychicWall(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void render(PsychicWallEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        super.render(entity, yaw, partialTicks, poseStack, buffer, light);
        poseStack.pushPose();
        float growth = Math.min(10, entity.tickCount + partialTicks) / 10F;
        if(entity.getLifespan() < 10){
            growth = (entity.getLifespan() - partialTicks) / 10F;
        }
        float f = entity.getBlockWidth() * growth;
        poseStack.mulPose(entity.getWallDirection().getOpposite().getRotation());
        poseStack.scale(f * 0.5F, f * 0.5F, f * 0.5F);
        renderWall(poseStack, f, buffer, buffer.getBuffer(PHRenderTypes.PSYCHIC_WALL));
        renderWall(poseStack, 1, buffer, buffer.getBuffer(PHRenderTypes.PSYCHIC_WALL_BORDER));
        poseStack.popPose();

    }

    private void renderWall(PoseStack matrixStackIn, float size, MultiBufferSource bufferIn, VertexConsumer ivertexbuilder) {
        matrixStackIn.pushPose();
        PoseStack.Pose pose = matrixStackIn.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        this.drawVertex(poseMatrix, normalMatrix, ivertexbuilder, -1, 0, -1, 0, 0, 1, 0, 1, 240);
        this.drawVertex(poseMatrix, normalMatrix, ivertexbuilder, -1, 0, 1, 0, size, 1, 0, 1, 240);
        this.drawVertex(poseMatrix, normalMatrix, ivertexbuilder, 1, 0, 1, size, size, 1, 0, 1, 240);
        this.drawVertex(poseMatrix, normalMatrix, ivertexbuilder, 1, 0, -1, size, 0, 1, 0, 1, 240);
        matrixStackIn.popPose();
    }

    public void drawVertex(Matrix4f matrix, Matrix3f normal, VertexConsumer consumer, int x, int y, int z, float u, float v, int normalX, int normalY, int normalZ, int lightmapUV) {
        consumer.vertex(matrix, (float) x, (float) y, (float) z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightmapUV).normal(normal, (float) normalX, (float) normalZ, (float) normalY).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(PsychicWallEntity entity) {
        return TEXTURE;
    }

}