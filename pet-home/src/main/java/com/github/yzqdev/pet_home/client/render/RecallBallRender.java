package com.github.yzqdev.pet_home.client.render;

import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.client.model.RecallBallModel;
import com.github.yzqdev.pet_home.server.entity.RecallBallEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public class RecallBallRender extends EntityRenderer<RecallBallEntity> {

    private RecallBallModel recallBallModel = new RecallBallModel();
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "textures/recall_ball.png");

    public RecallBallRender(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void render(RecallBallEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        super.render(entity, yaw, partialTicks, poseStack, buffer, light);
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateX(180F * ((float)Math.PI / 180F)));
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));


        VertexConsumer vertexconsumer = buffer.getBuffer(this.recallBallModel.renderType(this.getTextureLocation(entity)));
        poseStack.translate(0, -1.65F, 0);
        this.recallBallModel.animateBall(entity, partialTicks);
        this.recallBallModel.renderToBuffer(poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1 );
        poseStack.popPose();

    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(RecallBallEntity entity) {
        return TEXTURE;
    }

}