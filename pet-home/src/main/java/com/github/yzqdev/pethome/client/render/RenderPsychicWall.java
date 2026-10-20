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
import org.joml.Matrix4f;

public class RenderPsychicWall extends EntityRenderer<PsychicWallEntity> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "textures/psychic_wall_border.png");

    public RenderPsychicWall(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void render(PsychicWallEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        super.render(entity, yaw, partialTicks, poseStack, buffer, light);
        poseStack.pushPose();
        float growth = Math.min(10, entity.tickCount + partialTicks) / 10F;
        if (entity.getLifespan() < 10) {
            growth = (entity.getLifespan() - partialTicks) / 10F;
        }
        float f = entity.getBlockWidth() * growth;
        poseStack.mulPose(entity.getWallDirection().getOpposite().getRotation());
        poseStack.scale(f * 0.5F, f * 0.5F, f * 0.5F);
        renderWall(poseStack, 1, buffer, buffer.getBuffer(DIRenderTypes.PSYCHIC_WALL));
        renderWall(poseStack, 1, buffer, buffer.getBuffer(DIRenderTypes.PSYCHIC_WALL_BORDER));
        poseStack.popPose();

    }

    private void renderWall(PoseStack matrixStackIn, float size, MultiBufferSource bufferIn, VertexConsumer ivertexbuilder) {
        matrixStackIn.pushPose();
        PoseStack.Pose pose = matrixStackIn.last();
        Matrix4f matrix4f = pose.pose();

        this.drawVertex(matrix4f, pose, ivertexbuilder, -1, 0, -1, 0, 0, 1, 0, 1, 240);
        this.drawVertex(matrix4f, pose, ivertexbuilder, -1, 0, 1, 0, size, 1, 0, 1, 240);
        this.drawVertex(matrix4f, pose, ivertexbuilder, 1, 0, 1, size, size, 1, 0, 1, 240);
        this.drawVertex(matrix4f, pose, ivertexbuilder, 1, 0, -1, size, 0, 1, 0, 1, 240);
        matrixStackIn.popPose();
    }

    public void drawVertex(Matrix4f matrix4f, PoseStack.Pose pose, VertexConsumer vertexConsumer, int x, int y, int z, float u, float v, int normalY, int normalZ, int normalX, int lightCoords) {
        vertexConsumer.addVertex(matrix4f, (float) x, (float) y, (float) z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(lightCoords, lightCoords).setNormal(pose, (float) normalY, (float) normalX, (float) normalZ);
    }

    @Override
    public ResourceLocation getTextureLocation(PsychicWallEntity entity) {
        return TEXTURE;
    }

}