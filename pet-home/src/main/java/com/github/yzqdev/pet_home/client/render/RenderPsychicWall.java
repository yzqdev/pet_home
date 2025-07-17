package com.github.yzqdev.pet_home.client.render;

import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.server.entity.PsychicWallEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class RenderPsychicWall extends EntityRenderer<PsychicWallEntity> {

    private static final ResourceLocation TEXTURE =  ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "textures/psychic_wall_border.png");

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
        renderWall(poseStack, f, buffer, buffer.getBuffer(DIRenderTypes.PSYCHIC_WALL));
        renderWall(poseStack, 1, buffer, buffer.getBuffer(DIRenderTypes.PSYCHIC_WALL_BORDER));
        poseStack.popPose();

    }

    private void renderWall(PoseStack matrixStackIn, float size, MultiBufferSource bufferIn, VertexConsumer ivertexbuilder) {
        matrixStackIn.pushPose();
        PoseStack.Pose pose = matrixStackIn.last();
        Matrix4f lvt_20_1_ = pose.pose();

        this.drawVertex(lvt_20_1_, pose, ivertexbuilder, -1, 0, -1, 0, 0, 1, 0, 1, 240);
        this.drawVertex(lvt_20_1_, pose, ivertexbuilder, -1, 0, 1, 0, size, 1, 0, 1, 240);
        this.drawVertex(lvt_20_1_, pose, ivertexbuilder, 1, 0, 1, size, size, 1, 0, 1, 240);
        this.drawVertex(lvt_20_1_, pose, ivertexbuilder, 1, 0, -1, size, 0, 1, 0, 1, 240);
        matrixStackIn.popPose();
    }

    public void drawVertex(Matrix4f p_229039_1_,PoseStack.Pose p_229039_2_, VertexConsumer p_229039_3_, int p_229039_4_, int p_229039_5_, int p_229039_6_, float p_229039_7_, float p_229039_8_, int p_229039_9_, int p_229039_10_, int p_229039_11_, int p_229039_12_) {
        p_229039_3_.addVertex(p_229039_1_, (float) p_229039_4_, (float) p_229039_5_, (float) p_229039_6_).setColor(255, 255, 255, 255).setUv(p_229039_7_, p_229039_8_).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(p_229039_12_,p_229039_12_).setNormal(p_229039_2_, (float) p_229039_9_, (float) p_229039_11_, (float) p_229039_10_) ;
    }

    @Override
    public ResourceLocation getTextureLocation(PsychicWallEntity entity) {
        return TEXTURE;
    }

}