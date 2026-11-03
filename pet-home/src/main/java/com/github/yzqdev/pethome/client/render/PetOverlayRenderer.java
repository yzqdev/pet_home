package com.github.yzqdev.pethome.client.render;


import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.ClientGameEvents;
import com.github.yzqdev.pethome.client.model.BlazingBarModel;
import com.github.yzqdev.pethome.client.model.ShadowHandModel;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 宠物 overlay 绘制核心，原版 RenderLayer 路径（LayerPetOverlays）与 GeckoLib 路径
 * （compat.geckolib.GeoPetOverlayLayer）共用。要求调用点 poseStack 已处于原版
 * LivingEntityRenderer 的 layer 坐标系（setupRotations + flip(-1,-1,1) + translate(0,-1.501,0) 之后）。
 */
public class PetOverlayRenderer {

    /** 宿主提交「父模型渲染」的回调：原版=ParentModel.renderToBuffer，GeckoLib=bakedModel 重渲染 */
    public interface ParentModelSubmitter {
        void submit(ResourceLocation texture, float red, float green, float blue, float alpha);
    }

    private static final ItemStack MAGNET = new ItemStack(PHItemRegistry.MAGNET);
    private static final int CLOUD_COUNT = 14;
    private static final Vec3[] CLOUD_OFFSETS = new Vec3[CLOUD_COUNT];
    private static final Vec3[] CLOUD_SCALES = new Vec3[CLOUD_COUNT];
    private static final ShadowHandModel SHADOW_HAND_MODEL = new ShadowHandModel();
    private static final BlazingBarModel BLAZING_BAR_MODEL = new BlazingBarModel();
    private static final ResourceLocation BLAZE_TEXTURE =  new ResourceLocation("textures/entity/blaze.png");
    private static final ResourceLocation AURA_TEXTURE = new ResourceLocation(PetHomeMod.MODID, "textures/healing_aura.png");
    private static final Map<ResourceLocation, Integer> MODELS_TO_XSIZE = new HashMap<>();
    private static final Map<ResourceLocation, Integer> MODELS_TO_YSIZE = new HashMap<>();
    private static final LightningRender LIGHTNING_RENDER = new LightningRender();
    private static final LightningBoltData.BoltRenderInfo HEALTH_BOLT_DATA = new LightningBoltData.BoltRenderInfo(0.3F, 0.0F, 0.0F, 0.0F, new Vector4f(0.4F, 0, 0, 0.4F), 0.2F);
    private static final LightningBoltData.BoltRenderInfo SHADOW_BOLT_DATA = new LightningBoltData.BoltRenderInfo(0.3F, 0.0F, 0.0F, 0.0F, new Vector4f(1F, 1F, 1F, 1F), 0.2F);

    static {
        Random random = new Random(500);
        for (int i = 0; i < CLOUD_COUNT; i++) {
            CLOUD_OFFSETS[i] = new Vec3(random.nextFloat() - 0.5F, 0.2F * (random.nextFloat() - 0.5F), random.nextFloat() - 0.5F).scale(1.2F);
            CLOUD_SCALES[i] = new Vec3(0.6F + random.nextFloat() * 0.2F, 0.4F + random.nextFloat() * 0.2F, 0.4F + random.nextFloat() * 0.2F);
        }
    }

    public static void renderOverlays(LivingEntity living, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, float partialTicks, ParentModelSubmitter parentModelSubmitter) {
        Entity entity = living;
        float f = Mth.rotLerp(partialTicks, living.yBodyRotO, living.yBodyRot);
        float realAge = living.tickCount + partialTicks;
        if (TameableUtils.hasEnchant(living, PHEnchantmentRegistry.IMMUNITY_FRAME) && TameableUtils.getImmuneTime(living) > 0) {
            parentModelSubmitter.submit(null, 1, 1, 1, 0.5F);
        }
        if (TameableUtils.hasEnchant(living, PHEnchantmentRegistry.MAGNETIC)) {
            Entity suck = TameableUtils.getPetAttackTarget(living);
            if (suck != null) {
                double d0 = Mth.lerp(partialTicks, suck.xo, suck.getX()) - Mth.lerp(partialTicks, living.xo, living.getX());
                double d1 = Mth.lerp(partialTicks, suck.yo, suck.getY()) - Mth.lerp(partialTicks, living.yo, living.getY());
                double d2 = Mth.lerp(partialTicks, suck.zo, suck.getZ()) - Mth.lerp(partialTicks, living.zo, living.getZ());
                double d3 = d0 * d0 + d2 * d2 + d1 * d1;
                double d4 = Math.sqrt(d0 * d0 + d2 * d2);
                float f1 = (float) (Mth.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
                float f2 = (float) (-(Mth.atan2(d1, d4) * (double) (180F / (float) Math.PI)));
                matrixStackIn.pushPose();
                matrixStackIn.mulPose(Axis.YN.rotationDegrees(f));
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(f1));
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(f2));
                matrixStackIn.pushPose();
                float bob1 = (float) Math.sin(realAge * 0.5F) * 0.05F;
                float bob2 = (float) Math.sin(realAge * 0.3F) * 0.09F - 0.03F;
                float bob3 = (float) Math.cos(realAge * 0.1F) * 0.05F;
                matrixStackIn.translate(bob1, 1.25F - entity.getBbHeight() * 0.5F - bob2, -entity.getBbWidth() - 0.125F - bob3);
                matrixStackIn.mulPose(Axis.XN.rotationDegrees(90));
                matrixStackIn.scale(1.6F, 1.6F, 3F);
                Minecraft.getInstance().getItemRenderer().renderStatic(MAGNET, ItemDisplayContext.GROUND, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, entity.level(), entity.getId());
                matrixStackIn.popPose();
                matrixStackIn.popPose();
            }
        }
        if (TameableUtils.hasEnchant(living, PHEnchantmentRegistry.HEALTH_SIPHON)) {
            Entity owner = TameableUtils.getOwnerOf(living);
            if (owner != null && owner.isAlive() && owner.distanceTo(living) < 100) {
                float x = (float) Mth.lerp(partialTicks, entity.xo, entity.getX());
                float y = (float) Mth.lerp(partialTicks, entity.yo, entity.getY());
                float z = (float) Mth.lerp(partialTicks, entity.zo, entity.getZ());
                if (living.hurtTime > 0 && living.hurtTime == living.hurtDuration - 1) {
                    float height = -2 + entity.getBbHeight() * 0.8F;
                    float ownerHeight = -2 + owner.getBbHeight() * 0.6F;
                    LightningBoltData bolt = new LightningBoltData(HEALTH_BOLT_DATA, new Vec3(x, y + height, z), owner.position().add(0, ownerHeight, 0), 3)
                            .size(0.5F)
                            .lifespan(5)
                            .spawn(LightningBoltData.SpawnFunction.NO_DELAY);
                    LIGHTNING_RENDER.update(living, bolt, partialTicks);
                }
                matrixStackIn.pushPose();
                matrixStackIn.mulPose(Axis.YN.rotationDegrees(f));
                matrixStackIn.mulPose(Axis.XN.rotationDegrees(180));
                matrixStackIn.pushPose();
                matrixStackIn.translate(-x, -y, -z);
                LIGHTNING_RENDER.render(partialTicks, matrixStackIn, bufferIn);
                matrixStackIn.popPose();
                matrixStackIn.popPose();
            }
        }
        if (TameableUtils.hasEnchant(living, PHEnchantmentRegistry.VOID_CLOUD) && !living.isInWaterOrBubble() && !living.onGround() && TameableUtils.getFallDistance(living) >= 3.0F) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.4F, 1.25F + entity.getBbHeight(), 0.2F);
            matrixStackIn.mulPose(Axis.YN.rotationDegrees(f));
            matrixStackIn.mulPose(Axis.XN.rotationDegrees(180));
            for (int i = 0; i < CLOUD_COUNT; i++) {
                float xSin = (float) Math.sin(realAge * 0.05F + i * 2F) * 0.1F;
                float ySin = (float) Math.cos(realAge * 0.05F + i * 2F) * 0.1F;
                float zSin = (float) Math.sin(realAge * 0.05F + i * 2F - 2F) * 0.1F;
                matrixStackIn.pushPose();
                matrixStackIn.translate(CLOUD_OFFSETS[i].x + xSin, CLOUD_OFFSETS[i].y + ySin, CLOUD_OFFSETS[i].z + zSin);
                matrixStackIn.scale((float) CLOUD_SCALES[i].x + xSin, (float) CLOUD_SCALES[i].y + ySin, (float) CLOUD_SCALES[i].z + xSin);
                renderVoidCloudCube(entity, matrixStackIn, bufferIn.getBuffer(PHRenderTypes.VOID_CLOUD));
                matrixStackIn.popPose();

            }
            matrixStackIn.popPose();
        }
        int shadowHandCount = TameableUtils.getEnchantLevel(living, PHEnchantmentRegistry.SHADOW_HANDS);
        if (shadowHandCount > 0) {
            Entity punching = TameableUtils.getPetAttackTarget(living);
            double d0 = 0;
            double d1 = 0;
            double d2 = 0;
            if (punching != null) {
                d0 = Mth.lerp(partialTicks, punching.xo, punching.getX()) - Mth.lerp(partialTicks, living.xo, living.getX());
                d1 = Mth.lerp(partialTicks, punching.yo, punching.getY()) - Mth.lerp(partialTicks, living.yo, living.getY());
                d2 = Mth.lerp(partialTicks, punching.zo, punching.getZ()) - Mth.lerp(partialTicks, living.zo, living.getZ());
            }
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;
            double d4 = Math.sqrt(d0 * d0 + d2 * d2);
            float f1 = (float) (Mth.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
            float f2 = (float) (-(Mth.atan2(d1, d4) * (double) (180F / (float) Math.PI)));
            for (int i = 0; i < shadowHandCount; i++) {
                float punch = getPunchFor(living, i, partialTicks) / 10F;
                VertexConsumer vertexconsumer = bufferIn.getBuffer(PHRenderTypes.SHADOW_HAND_ENTITY);
                float xSpread = shadowHandCount <= 1 ? 0F : ((i) / (float) (shadowHandCount - 1)) - 0.5F;
                float xOffset = shadowHandCount <= 1 ? 0 : Mth.sin((float) (xSpread * Math.PI)) * 1.8F;
                float zOffset = shadowHandCount <= 1 ? 0.25F : Mth.cos((float) (xSpread * Math.PI)) * 1.8F;
                float yOffset = -zOffset * 0.3F;
                Vec3 fromPos = new Vec3(0, 1.25F - entity.getBbHeight() * 0.5F, -entity.getBbWidth() * 0.3F);
                Vec3 handTranslate = new Vec3(xOffset * entity.getBbWidth(), 1 + yOffset + Math.sin(realAge * 0.2F + i * 1.5F) * 0.15F, zOffset * entity.getBbWidth());
                if (punching != null) {
                    Vec3 vec3 = new Vec3(d0, d1, d2).scale(punch);
                    handTranslate = handTranslate.add(vec3).yRot((float) Math.PI + f1 * (float) (Math.PI / 180));
                }
                matrixStackIn.pushPose();
                if (punching != null) {
                    matrixStackIn.mulPose(Axis.YN.rotationDegrees(f));
                    matrixStackIn.mulPose(Axis.YP.rotationDegrees(f1));
                    matrixStackIn.mulPose(Axis.XP.rotationDegrees(f2));
                }

                matrixStackIn.pushPose();
                matrixStackIn.translate(handTranslate.x, handTranslate.y, handTranslate.z);
                matrixStackIn.pushPose();
                matrixStackIn.translate(0, -1.15F, -0.15F);
                SHADOW_HAND_MODEL.animateShadowHand(punch, i, shadowHandCount, realAge);
                SHADOW_HAND_MODEL.renderToBuffer(matrixStackIn, vertexconsumer, packedLightIn, LivingEntityRenderer.getOverlayCoords(living, 0), 1, 1, 1, 1);
                matrixStackIn.popPose();
                matrixStackIn.popPose();
                matrixStackIn.pushPose();
                renderShadowString(entity, fromPos, partialTicks, matrixStackIn, bufferIn, handTranslate);
                matrixStackIn.popPose();
                matrixStackIn.popPose();
            }
        }
        if (TameableUtils.hasEnchant(living, PHEnchantmentRegistry.BLAZING_PROTECTION)) {
            int bars = TameableUtils.getBlazingProtectionBars(living);
            float f1 = realAge * 7;
            float seperation = 360F / (TameableUtils.getEnchantLevel(living, PHEnchantmentRegistry.BLAZING_PROTECTION) * 2F);
            VertexConsumer vertexconsumer = bufferIn.getBuffer(RenderType.entityTranslucentEmissive(BLAZE_TEXTURE));
            matrixStackIn.pushPose();
            matrixStackIn.mulPose(Axis.YN.rotationDegrees(f));
            for (int i = 0; i < bars; i++) {
                f1 += seperation;
                matrixStackIn.pushPose();
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(f1));
                float bob = (float) Math.sin(realAge * 0.6F + Math.toRadians(seperation * i)) * 0.15F - 0.07F;
                matrixStackIn.translate(0, 0.4F - entity.getBbHeight() * 0.5F - bob, -entity.getBbWidth() - 0.2F);
                BLAZING_BAR_MODEL.animateBar(f1);
                BLAZING_BAR_MODEL.renderToBuffer(matrixStackIn, vertexconsumer, 240, LivingEntityRenderer.getOverlayCoords(living, 0), 1, 1, 1, 1);
                matrixStackIn.popPose();
            }
            matrixStackIn.popPose();
        }
        if (TameableUtils.hasEnchant(living, PHEnchantmentRegistry.HEALING_AURA)) {
            int t = TameableUtils.getHealingAuraTime(living);
            if (t > 0) {
                float time = t > 20 ? 200 - Math.max(180, t + partialTicks) : t - partialTicks;
                float pulse = 0.9F + (float) (Math.sin(realAge * 0.08F) * 0.1F + 0.1F);
                float healscale = (Math.min(time, 20) / 20F) * 2.2F * pulse;
                matrixStackIn.pushPose();
                matrixStackIn.translate(0, 1.8F - entity.getBbHeight() * 0.5F, 0);
                matrixStackIn.mulPose(Axis.YN.rotationDegrees(f));
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(realAge * 3F));
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(90F));
                matrixStackIn.scale(3 * healscale, 3 * healscale, 3 * healscale);
                VertexConsumer vertexconsumer = bufferIn.getBuffer(PHRenderTypes.HEALING_AURA);
                PoseStack.Pose posestack$pose = matrixStackIn.last();
                Matrix4f matrix4f = posestack$pose.pose();
                Matrix3f matrix3f = posestack$pose.normal();
                vertex(vertexconsumer, matrix4f, matrix3f, 240, 0.0F, 0, 0, 1, 1);
                vertex(vertexconsumer, matrix4f, matrix3f, 240, 1.0F, 0, 1, 1, 1);
                vertex(vertexconsumer, matrix4f, matrix3f, 240, 1.0F, 1, 1, 0, 1);
                vertex(vertexconsumer, matrix4f, matrix3f, 240, 0.0F, 1, 0, 0, 1);
                matrixStackIn.popPose();
            }
        }
    }

    private static ResourceLocation getTextureLocation(LivingEntity living) {
        return Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(living).getTextureLocation(living);
    }

    private static void addVertexPairAlex(VertexConsumer consumer, Matrix4f matrix, float dx, float dy, float dz, int fromBlockLight, int toBlockLight, int fromSkyLight, int toSkyLight, float topOffset, float widthOffset, float halfWidthX, float halfWidthZ, int index, boolean unused) {
        float f = (float) index / 8.0F;
        int i = (int) Mth.lerp(f, (float) fromBlockLight, (float) toBlockLight);
        int j = (int) Mth.lerp(f, (float) fromSkyLight, (float) toSkyLight);
        int k = LightTexture.pack(i, j);
        float f2 = 0;
        float f3 = 0;
        float f4 = 0;
        float f5 = dx * f;
        float f6 = dy < 0.0F ? dy * f * f : dy - dy * (1.0F - f) * (1.0F - f);
        float f7 = dz * f;
        consumer.vertex(matrix, f5 - halfWidthX, f6 + widthOffset, f7 + halfWidthZ).color(f2, f3, f4, 1.0F).uv2(k).endVertex();
        consumer.vertex(matrix, f5 + halfWidthX, f6 + topOffset - widthOffset, f7 - halfWidthZ).color(f2, f3, f4, 1.0F).uv2(k).endVertex();
    }

    public static <E extends Entity> void renderShadowString(Entity from, Vec3 fromVec, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, Vec3 to) {
        poseStack.pushPose();
        double d3 = fromVec.x;
        double d4 = fromVec.y;
        double d5 = fromVec.z;
        poseStack.translate(d3, d4, d5);
        float f = (float) (to.x - d3);
        float f1 = (float) (to.y - d4);
        float f2 = (float) (to.z - d5);
        float f3 = 0.025F;
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.leash());
        Matrix4f matrix4f = poseStack.last().pose();
        BlockPos blockpos = BlockPos.containing(fromVec);
        BlockPos blockpos1 = BlockPos.containing(to);
        int i = 0;
        int j = from.level().getBrightness(LightLayer.BLOCK, blockpos1);
        int k = from.level().getBrightness(LightLayer.SKY, blockpos);
        int l = from.level().getBrightness(LightLayer.SKY, blockpos1);
        for (int i1 = 0; i1 <= 8; ++i1) {
            float width = 0.05F - (i1 / 8F) * 0.025F;
            addVertexPairAlex(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.2F, width, width, width, i1, false);
        }
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, int lightmapUV, float x, int y, int u, int v, float alpha) {
        consumer.vertex(matrix, x - 0.5F, (float) y - 0.5F, 0.0F).color(255, 255, 255, alpha * 255).uv((float) u, (float) v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lightmapUV).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
    }

    private static float getPunchFor(LivingEntity living, int i, float partialTicks) {
        int[] arr = TameableUtils.getShadowPunchTimes(living);
        if (arr.length > i) {
            int[] prevArr = ClientGameEvents.shadowPunchRenderData.get(living);
            if (prevArr != null && prevArr.length > i) {
                return prevArr[i] + (arr[i] - prevArr[i]) * partialTicks;
            }
            return arr[i];
        }
        return 0F;
    }

    private static void renderVoidCloudCube(Entity entity, PoseStack poseStack, VertexConsumer consumer) {
        Matrix4f cubeAt = poseStack.last().pose();
        renderVoidCloudFace(entity, cubeAt, consumer, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, Direction.SOUTH);
        renderVoidCloudFace(entity, cubeAt, consumer, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, Direction.NORTH);
        renderVoidCloudFace(entity, cubeAt, consumer, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.EAST);
        renderVoidCloudFace(entity, cubeAt, consumer, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.WEST);
        renderVoidCloudFace(entity, cubeAt, consumer, 0.0F, 1.0F, 0, 0, 0.0F, 0.0F, 1.0F, 1.0F, Direction.DOWN);
        renderVoidCloudFace(entity, cubeAt, consumer, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, Direction.UP);
    }

    private static void renderVoidCloudFace(Entity entity, Matrix4f matrix, VertexConsumer consumer, float v000X, float v100X, float v000Y, float v100Y, float v000Z, float v100Z, float v110Z, float v111Z, Direction face) {
        consumer.vertex(matrix, v000X, v000Y, v000Z).overlayCoords(240).endVertex();
        consumer.vertex(matrix, v100X, v000Y, v100Z).overlayCoords(240).endVertex();
        consumer.vertex(matrix, v100X, v100Y, v110Z).overlayCoords(240).endVertex();
        consumer.vertex(matrix, v000X, v100Y, v111Z).overlayCoords(240).endVertex();
    }
}
