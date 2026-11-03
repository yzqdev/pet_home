package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.entity.PsychicWallEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

public class RenderPsychicWall extends EntityRenderer<PsychicWallEntity, PsychicWallRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "textures/psychic_wall_border.png");

    public RenderPsychicWall(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public PsychicWallRenderState createRenderState() {
        return new PsychicWallRenderState();
    }

    @Override
    public void extractRenderState(PsychicWallEntity entity, PsychicWallRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.direction = entity.getWallDirection();
        float growth = Math.min(10, entity.tickCount + partialTicks) / 10F;
        if (entity.getLifespan() < 10) {
            growth = (entity.getLifespan() - partialTicks) / 10F;
        }
        state.size = entity.getBlockWidth() * growth;
    }

    @Override
    public void submit(PsychicWallRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);

        poseStack.pushPose();
        float f = state.size;
        poseStack.mulPose(state.direction.getOpposite().getRotation());
        poseStack.scale(f * 0.5F, f * 0.5F, f * 0.5F);

        // 两层均整幅 UV 0..1 映射贴满墙面（四版统一），生长动画由上面的 scale 完成
        submitNodeCollector.submitCustomGeometry(poseStack, PHRenderTypes.PSYCHIC_WALL, (pose, buffer) -> {
            renderWall(pose, buffer, 1F);
        });
        submitNodeCollector.submitCustomGeometry(poseStack, PHRenderTypes.PSYCHIC_WALL_BORDER, (pose, buffer) -> {
            renderWall(pose, buffer, 1F);
        });
        poseStack.popPose();
    }

    private void renderWall(PoseStack.Pose pose, VertexConsumer buffer, float size) {
        Matrix4f matrix = pose.pose();
        this.drawVertex(matrix, pose, buffer, -1, 0, -1, 0, 0, 1, 0, 1, 240);
        this.drawVertex(matrix, pose, buffer, -1, 0, 1, 0, size, 1, 0, 1, 240);
        this.drawVertex(matrix, pose, buffer, 1, 0, 1, size, size, 1, 0, 1, 240);
        this.drawVertex(matrix, pose, buffer, 1, 0, -1, size, 0, 1, 0, 1, 240);
    }

    public void drawVertex(Matrix4f matrix, PoseStack.Pose pose, VertexConsumer buffer, int x, int y, int z, float u, float v, int nx, int ny, int nz, int light) {
        buffer.addVertex(matrix, (float) x, (float) y, (float) z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setUv2(light, light).setNormal(pose, (float) nx, (float) ny, (float) nz);
    }
}
