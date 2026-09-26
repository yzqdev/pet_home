package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.model.RecallBallModel;
import com.github.yzqdev.pethome.server.entity.RecallBallEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class RecallBallRender extends EntityRenderer<RecallBallEntity, RecallBallRenderState> {

    private final RecallBallModel recallBallModel = new RecallBallModel();
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "textures/recall_ball.png");

    public RecallBallRender(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public RecallBallRenderState createRenderState() {
        return new RecallBallRenderState();
    }

    @Override
    public void extractRenderState(RecallBallEntity entity, RecallBallRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRotO = entity.yRotO;
        state.yRot = entity.getYRot();
        state.xRotO = entity.xRotO;
        state.xRot = entity.getXRot();
        state.tickCount = entity.tickCount;
        state.partialTick = partialTicks;
        // 打开进度由服务端同步的 OPENED 驱动（主人点击后才打开），默认保持盒子关闭
        state.open = entity.getOpenProgress(partialTicks);
        state.finished = entity.isFinished();
    }

    @Override
    public void submit(RecallBallRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateX(180F * ((float) Math.PI / 180F)));
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(state.partialTick, state.yRotO, state.yRot)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(state.partialTick, state.xRotO, state.xRot)));

        this.recallBallModel.animateBall(state.open, state.finished);
        poseStack.translate(0, -1.65F, 0);
        var renderType = RenderTypes.entityCutout(TEXTURE);
        // 26.1: renderToBuffer 只渲染空的占位根 ModelPart，Tabula 风格模型须走 renderParts
        // submitCustomGeometry 回调延迟执行且只携带提交时的 Pose 快照，回调内不得使用外层 poseStack
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            PoseStack snapshot = new PoseStack();
            snapshot.mulPose(new Matrix4f(pose.pose()));
            this.recallBallModel.renderParts(snapshot, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY);
        });
        poseStack.popPose();
    }
}