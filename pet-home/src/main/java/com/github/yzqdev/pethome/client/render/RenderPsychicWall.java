package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.entity.PsychicWallEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

public class RenderPsychicWall extends EntityRenderer<PsychicWallEntity, EntityRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "textures/psychic_wall_border.png");

    public RenderPsychicWall(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void extractRenderState(PsychicWallEntity entity, EntityRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);

        poseStack.pushPose();

        submitNodeCollector.submitCustomGeometry(poseStack, DIRenderTypes.PSYCHIC_WALL, (pose, buffer) -> {
            renderWall(pose, buffer);
        });
        submitNodeCollector.submitCustomGeometry(poseStack, DIRenderTypes.PSYCHIC_WALL_BORDER, (pose, buffer) -> {
            renderWall(pose, buffer);
        });
        poseStack.popPose();
    }

    private void renderWall(PoseStack.Pose pose, VertexConsumer buffer) {
        Matrix4f matrix = pose.pose();
        this.drawVertex(matrix, pose, buffer, -1, 0, -1, 0, 0, 1, 0, 1, 240);
        this.drawVertex(matrix, pose, buffer, -1, 0, 1, 0, 1, 1, 0, 1, 240);
        this.drawVertex(matrix, pose, buffer, 1, 0, 1, 1, 1, 1, 0, 1, 240);
        this.drawVertex(matrix, pose, buffer, 1, 0, -1, 1, 0, 1, 0, 1, 240);
    }

    public void drawVertex(Matrix4f matrix, PoseStack.Pose pose, VertexConsumer buffer, int x, int y, int z, float u, float v, int nx, int ny, int nz, int light) {
        buffer.addVertex(matrix, (float) x, (float) y, (float) z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(light, light).setNormal(pose, (float) nx, (float) ny, (float) nz);
    }
}