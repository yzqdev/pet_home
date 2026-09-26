package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.client.render.state.FeatherRenderState;
import com.github.yzqdev.pethome.server.entity.FeatherEntity;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class RenderFeather extends EntityRenderer<FeatherEntity, FeatherRenderState> {


    private ItemStack feather;
    private final net.minecraft.client.renderer.item.ItemModelResolver itemModelResolver;

    public RenderFeather(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public FeatherRenderState createRenderState() {
        return new FeatherRenderState();
    }

    @Override
    public void extractRenderState(FeatherEntity entity, FeatherRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        Player player = entity.getPlayerOwner();
        state.player = player;
        if (player != null) {
            state.mainArm = player.getMainArm();
            state.mainHandItem = player.getMainHandItem();
            state.attackAnim = player.getAttackAnim(partialTicks);
            state.yBodyRotO = player.yBodyRotO;
            state.yBodyRot = player.yBodyRot;
            state.xo = player.xo;
            state.yo = player.yo;
            state.zo = player.zo;
            state.playerX = player.getX();
            state.playerY = player.getY();
            state.playerZ = player.getZ();
            state.eyeHeight = player.getEyeHeight();
            state.isCrouching = player.isCrouching();
            state.isPlayer = (player == Minecraft.getInstance().player);
        }
        state.entityXo = entity.xo;
        state.entityYo = entity.yo;
        state.entityZo = entity.zo;
        state.entityX = entity.getX();
        state.entityY = entity.getY();
        state.entityZ = entity.getZ();
        state.partialTick = partialTicks;
    }

    @Override
    public void submit(FeatherRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        float partialTick = state.partialTick;

        // Render the feather item
        poseStack.pushPose();
        poseStack.translate(0, 0.1F, 0);
        poseStack.mulPose(camera.orientation);
        poseStack.mulPose(new Quaternionf().rotateZ(35F * ((float) Math.PI / 180F)));
        net.minecraft.client.renderer.item.ItemStackRenderState itemStackRenderState = new net.minecraft.client.renderer.item.ItemStackRenderState();
        if (this.feather == null) {
            this.feather = new ItemStack(Items.FEATHER);
        }
        this.itemModelResolver.updateForTopItem(itemStackRenderState, this.feather, ItemDisplayContext.GROUND, Minecraft.getInstance().level, null, 0);
        itemStackRenderState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();

        // Fishing rod line
        Player player = state.player;
        if (player != null) {
            poseStack.pushPose();

            int i = state.mainArm == HumanoidArm.RIGHT ? 1 : -1;
            if (!state.mainHandItem.is(PHItemRegistry.FEATHER_ON_A_STICK)) {
                i = -i;
            }

            float f = state.attackAnim;
            float f1 = Mth.sin(Mth.sqrt(f) * (float) Math.PI);
            float f2 = Mth.lerp(partialTick, state.yBodyRotO, state.yBodyRot) * ((float) Math.PI / 180F);
            double d0 = (double) Mth.sin(f2);
            double d1 = (double) Mth.cos(f2);
            double d2 = (double) i * 0.35D;
            double d4;
            double d5;
            double d6;
            float f3;
            if (state.isPlayer && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                // 26.1：getNearPlane 的参数是 FOV 角度（不是 partialTick），误传会让杆尖偏移≈0、线画到准星上
                float fov = (float) Minecraft.getInstance().options.fov().get().intValue();
                double d7 = 960.0D / (double) fov;
                Vec3 vec3 = Minecraft.getInstance().gameRenderer.getMainCamera().getNearPlane(fov).getPointOnPlane((float) i * 0.525F, -0.1F);
                vec3 = vec3.scale(d7);
                vec3 = vec3.yRot(f1 * 0.5F);
                vec3 = vec3.xRot(-f1 * 0.7F);
                d4 = Mth.lerp((double) partialTick, state.xo, state.playerX) + vec3.x;
                d5 = Mth.lerp((double) partialTick, state.yo, state.playerY) + vec3.y;
                d6 = Mth.lerp((double) partialTick, state.zo, state.playerZ) + vec3.z;
                f3 = state.eyeHeight;
            } else {
                d4 = Mth.lerp((double) partialTick, state.xo, state.playerX) - d1 * d2 - d0 * 0.8D;
                d5 = state.yo + (double) state.eyeHeight + (state.playerY - state.yo) * (double) partialTick - 0.55D;
                d6 = Mth.lerp((double) partialTick, state.zo, state.playerZ) - d0 * d2 + d1 * 0.8D;
                f3 = state.isCrouching ? -0.1875F : 0.0F;
            }

            double d9 = Mth.lerp((double) partialTick, state.entityXo, state.entityX);
            double d10 = Mth.lerp((double) partialTick, state.entityYo, state.entityY) + 0.25D;
            double d8 = Mth.lerp((double) partialTick, state.entityZo, state.entityZ);
            float f4 = (float) (d4 - d9);
            float f5 = (float) (d5 - d10) + f3;
            float f6 = (float) (d6 - d8);

            // 26.1: leash 渲染类型要求顶点带 UV2（光照），改用原版钓线的 lines() 渲染类型
            float width = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;
            submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
                PoseStack.Pose posestack$pose1 = pose;
                int j = 16;
                for (int k = 0; k < 16; ++k) {
                    stringVertex(f4, f5, f6, buffer, posestack$pose1, fraction(k, 16), fraction(k + 1, 16), width);
                    stringVertex(f4, f5, f6, buffer, posestack$pose1, fraction(k + 1, 16), fraction(k, 16), width);
                }
            });
            poseStack.popPose();
        }
    }

    private static float fraction(int p_114691_, int p_114692_) {
        return (float) p_114691_ / (float) p_114692_;
    }

    private static void stringVertex(float p_174119_, float p_174120_, float p_174121_, VertexConsumer p_174122_, PoseStack.Pose p_174123_, float p_174124_, float p_174125_, float width) {
        float f = p_174119_ * p_174124_;
        float f1 = p_174120_ * (p_174124_ * p_174124_ + p_174124_) * 0.5F + 0.25F;
        float f2 = p_174121_ * p_174124_;
        float f3 = p_174119_ * p_174125_ - f;
        float f4 = p_174120_ * (p_174125_ * p_174125_ + p_174125_) * 0.5F + 0.25F - f1;
        float f5 = p_174121_ * p_174125_ - f2;
        float f6 = Mth.sqrt(f3 * f3 + f4 * f4 + f5 * f5);
        f3 /= f6;
        f4 /= f6;
        f5 /= f6;
        p_174122_.addVertex(p_174123_.pose(), f, f1, f2).setColor(0, 0, 0, 255).setNormal(p_174123_, f3, f4, f5).setLineWidth(width);
    }
}