package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.server.entity.FeatherEntity;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class RenderFeather extends EntityRenderer<FeatherEntity> {

    private ItemStack feather = new ItemStack(Items.FEATHER);

    public RenderFeather(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void render(FeatherEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {

        super.render(entity, yaw, partialTicks, poseStack, buffer, light);
        Player player = entity.getPlayerOwner();
        poseStack.pushPose();
        poseStack.translate(0, 0.1F, 0);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(new Quaternionf().rotateZ(35F * ((float) Math.PI / 180F)));
        Minecraft.getInstance().getItemRenderer().renderStatic(feather, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();

        // 钓线部分：羽毛实体始终与主人手持的「羽毛钓竿」成对渲染
        if (player != null) {
            poseStack.pushPose();

            int i = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            ItemStack itemstack = player.getMainHandItem();
            if (!itemstack.is(PHItemRegistry.FEATHER_ON_A_STICK.get())) {
                i = -i;
            }

            float f = player.getAttackAnim(partialTicks);
            float f1 = Mth.sin(Mth.sqrt(f) * (float) Math.PI);
            float f2 = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot) * ((float) Math.PI / 180F);
            double d0 = (double) Mth.sin(f2);
            double d1 = (double) Mth.cos(f2);
            double d2 = (double) i * 0.35D;
            double d3 = 0.8D;
            double d4;
            double d5;
            double d6;
            float f3;
            if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && player == Minecraft.getInstance().player) {
                double d7 = 960.0D / (double) this.entityRenderDispatcher.options.fov().get().intValue();
                Vec3 vec3 = this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float) i * 0.525F, -0.1F);
                vec3 = vec3.scale(d7);
                vec3 = vec3.yRot(f1 * 0.5F);
                vec3 = vec3.xRot(-f1 * 0.7F);
                d4 = Mth.lerp((double) partialTicks, player.xo, player.getX()) + vec3.x;
                d5 = Mth.lerp((double) partialTicks, player.yo, player.getY()) + vec3.y;
                d6 = Mth.lerp((double) partialTicks, player.zo, player.getZ()) + vec3.z;
                f3 = player.getEyeHeight();
            } else {
                d4 = Mth.lerp((double) partialTicks, player.xo, player.getX()) - d1 * d2 - d0 * 0.8D;
                d5 = player.yo + (double) player.getEyeHeight() + (player.getY() - player.yo) * (double) partialTicks - 0.55D;
                d6 = Mth.lerp((double) partialTicks, player.zo, player.getZ()) - d0 * d2 + d1 * 0.8D;
                f3 = player.isCrouching() ? -0.1875F : 0.0F;
            }

            double d9 = Mth.lerp((double) partialTicks, entity.xo, entity.getX());
            double d10 = Mth.lerp((double) partialTicks, entity.yo, entity.getY()) + 0.25D;
            double d8 = Mth.lerp((double) partialTicks, entity.zo, entity.getZ());
            float f4 = (float) (d4 - d9);
            float f5 = (float) (d5 - d10) + f3;
            float f6 = (float) (d6 - d8);
            VertexConsumer vertexconsumer1 = buffer.getBuffer(RenderType.lineStrip());
            PoseStack.Pose posestack$pose1 = poseStack.last();
            int j = 16;

            for (int k = 0; k <= 16; ++k) {
                stringVertex(f4, f5, f6, vertexconsumer1, posestack$pose1, fraction(k, 16), fraction(k + 1, 16));
            }
            poseStack.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(FeatherEntity entity) {
        return null;
    }

    /** 钓线绘制辅助：沿吊钩下垂曲线插值顶点（仿原版 FishingHookRenderer）。 */
    private static float fraction(int numerator, int denominator) {
        return (float) numerator / (float) denominator;
    }

    private static void stringVertex(float x, float y, float z, VertexConsumer consumer, PoseStack.Pose pose, float stringFraction, float nextStringFraction) {
        float f = x * stringFraction;
        float f1 = y * (stringFraction * stringFraction + stringFraction) * 0.5F + 0.25F;
        float f2 = z * stringFraction;
        float f3 = x * nextStringFraction - f;
        float f4 = y * (nextStringFraction * nextStringFraction + nextStringFraction) * 0.5F + 0.25F - f1;
        float f5 = z * nextStringFraction - f2;
        float f6 = Mth.sqrt(f3 * f3 + f4 * f4 + f5 * f5);
        f3 /= f6;
        f4 /= f6;
        f5 /= f6;
        consumer.addVertex(pose.pose(), f, f1, f2).setColor(0, 0, 0, 255).setNormal(pose, f3, f4, f5);
    }

}
