package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.model.HighlightedBlockModel;
import com.github.yzqdev.pethome.server.entity.HighlightedBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;


public class RenderHighlightedBlock extends EntityRenderer<HighlightedBlockEntity, EntityRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "textures/highlighted_block.png");
    private final HighlightedBlockModel highlightedBlockModel = new HighlightedBlockModel();

    public RenderHighlightedBlock(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        EntityRenderState state = new EntityRenderState();
        state.shadowRadius = 0.0F;
        return state;
    }

    @Override
    public void extractRenderState(HighlightedBlockEntity entity, EntityRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.shadowRadius = 0.0F;
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        poseStack.pushPose();
        poseStack.translate(0, 0.5F, 0);
        // 26.1: renderToBuffer 只渲染空的占位根 ModelPart，Tabula 风格模型须走 renderParts
        // submitCustomGeometry 回调延迟执行且只携带提交时的 Pose 快照，回调内不得使用外层 poseStack
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.outline(TEXTURE), (pose, buffer) -> {
            PoseStack snapshot = new PoseStack();
            snapshot.mulPose(new Matrix4f(pose.pose()));
            this.highlightedBlockModel.renderParts(snapshot, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY);
        });
        poseStack.popPose();
    }
}