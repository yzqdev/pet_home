package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.server.entity.FollowingJukeboxEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;

/**
 * disc_jockey 唱片机渲染（自 1.20 移植）：无自定义模型，直接渲染唱片机物品（放大 1.8 倍、按实体 yaw 插值旋转）。 有唱片时渲染唱片本体，否则渲染唱片机方块物品。
 */
public class RenderJukeboxFollower extends EntityRenderer<FollowingJukeboxEntity> {

    private ItemStack jukebox;

    public RenderJukeboxFollower(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void render(FollowingJukeboxEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        super.render(entity, yaw, partialTicks, poseStack, buffer, light);
        if (this.jukebox == null) {
            this.jukebox = new ItemStack(Items.JUKEBOX);
        }
        ItemStack disc = entity.getRecordItem();
        ItemStack toRender = !disc.isEmpty() ? disc : this.jukebox;
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(new Quaternionf().rotateY(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) * Mth.DEG_TO_RAD));
        poseStack.translate(0, 0.1F, 0);
        poseStack.scale(1.8F, 1.8F, 1.8F);
        Minecraft.getInstance().getItemRenderer().renderStatic(toRender, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(FollowingJukeboxEntity entity) {
        return null;
    }
}
