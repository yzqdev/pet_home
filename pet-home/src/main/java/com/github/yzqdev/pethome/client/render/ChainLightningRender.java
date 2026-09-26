package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.server.entity.ChainLightningEntity;
import com.github.yzqdev.pethome.client.render.state.ChainLightningRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Vector4f;

public class ChainLightningRender extends EntityRenderer<ChainLightningEntity, ChainLightningRenderState> {

    private LightningRender lightningRender = new LightningRender();
    private LightningBoltData.BoltRenderInfo lightningBoltData = new LightningBoltData.BoltRenderInfo(1.3F, 0.15F, 0.5F, 0.25F, new Vector4f(0.1F, 0.3F, 0.5F, 0.5F), 0.45F);

    public ChainLightningRender(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public boolean shouldRender(ChainLightningEntity entity, Frustum frustum, double x, double y, double z) {
        Entity next = entity.getFromEntity();
        return next != null && frustum.isVisible(entity.getBoundingBox().minmax(next.getBoundingBox())) || super.shouldRender(entity, frustum, x, y, z);
    }

    @Override
    public ChainLightningRenderState createRenderState() {
        return new ChainLightningRenderState();
    }

    @Override
    public void extractRenderState(ChainLightningEntity entity, ChainLightningRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.lightningEntity = entity;
        state.partialTick = partialTicks;
    }

    @Override
    public void submit(ChainLightningRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        // 26.1: 旧 render() 入口已删除，闪电绘制挪到 submit；不调用的话实体什么都不渲染
        if (state.lightningEntity != null) {
            renderLightning(state.lightningEntity, state.partialTick, poseStack, submitNodeCollector);
        }
    }

    public void renderLightning(ChainLightningEntity entity, float partialTicks, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        Entity from = entity.getFromEntity();
        float x = (float) Mth.lerp(partialTicks, entity.xo, entity.getX());
        float y = (float) Mth.lerp(partialTicks, entity.yo, entity.getY());
        float z = (float) Mth.lerp(partialTicks, entity.zo, entity.getZ());
        if (from != null) {
            LightningBoltData bolt = new LightningBoltData(lightningBoltData, from.getEyePosition(), entity.position(), 5)
                    .size(0.1F)
                    .lifespan(2)
                    .spawn(LightningBoltData.SpawnFunction.NO_DELAY);
            // 26.1: 按实体隔离 bolt 数据并只渲染自己的闪电，避免多实体共用 null key 互相污染
            lightningRender.update(entity, bolt, partialTicks);
            poseStack.pushPose();
            poseStack.translate(-x, -y, -z);
            lightningRender.renderOwner(entity, partialTicks, poseStack, submitNodeCollector);
            poseStack.popPose();
        }
    }
}