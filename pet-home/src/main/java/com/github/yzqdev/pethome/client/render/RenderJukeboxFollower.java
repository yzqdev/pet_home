package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.client.render.state.JukeboxRenderState;
import com.github.yzqdev.pethome.server.entity.FollowingJukeboxEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * disc_jockey 唱片机渲染（自 1.20 移植）：无自定义模型，直接渲染唱片机物品（放大 1.8 倍、按实体 yaw 插值旋转）。 26.1
 * 渲染管线：extractRenderState 抓数据 → submit 经 ItemStackRenderState 提交（对齐 RenderFeather 模式）。
 */
public class RenderJukeboxFollower
        extends EntityRenderer<FollowingJukeboxEntity, JukeboxRenderState> {

    // ItemStack 不能在静态初始化创建（组件系统未 bound → "Components not bound yet" →
    // 类初始化失败 NoClassDefFound 黑屏），改为实例字段 + extractRenderState 延迟初始化（对齐 RenderFeather 模式）
    private ItemStack jukebox;
    private final ItemModelResolver itemModelResolver;

    public RenderJukeboxFollower(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public JukeboxRenderState createRenderState() {
        return new JukeboxRenderState();
    }

    @Override
    public void extractRenderState(
            FollowingJukeboxEntity entity, JukeboxRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.partialTick = partialTicks;
        state.yRotO = entity.yRotO;
        state.yRot = entity.getYRot();
        // 有唱片时渲染唱片本体（更像「正在播放的唱片机」），否则渲染唱片机方块物品
        if (this.jukebox == null) {
            this.jukebox = new ItemStack(Items.JUKEBOX);
        }
        ItemStack disc = entity.getRecordItem();
        state.record = !disc.isEmpty() ? disc : this.jukebox;
    }

    @Override
    public void submit(
            JukeboxRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        poseStack.pushPose();
        poseStack.mulPose(
                new org.joml.Quaternionf()
                        .rotateY(Mth.lerp(state.partialTick, state.yRotO, state.yRot) * Mth.DEG_TO_RAD));
        poseStack.translate(0, 0.1F, 0);
        poseStack.scale(1.8F, 1.8F, 1.8F);
        ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
        this.itemModelResolver.updateForTopItem(
                itemStackRenderState,
                state.record,
                ItemDisplayContext.GROUND,
                Minecraft.getInstance().level,
                null,
                0);
        itemStackRenderState.submit(
                poseStack,
                submitNodeCollector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor);
        poseStack.popPose();
    }
}
