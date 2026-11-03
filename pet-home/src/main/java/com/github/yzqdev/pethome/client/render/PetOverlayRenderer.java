package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.client.ClientGameEvents;
import com.github.yzqdev.pethome.client.model.BlazingBarModel;
import com.github.yzqdev.pethome.client.model.ShadowHandModel;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.util.TameableUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

/**
 * 宠物 overlay 绘制核心（从 LayerPetOverlays 抽出，供两类宿主共用）： 原版 RenderLayer 路径（LayerPetOverlays）与 GeckoLib
 * GeoRenderLayer 适配路径（compat/geckolib）。 宿主负责把 poseStack 变换到「原版 LivingEntityRenderer.submit 的 layer
 * 调用点」状态 （scale → setupRotations → flip(-1,-1,1) → translate(0,-1.501,0)），本类只做绘制。
 */
public final class PetOverlayRenderer {

    /** 绘制回调签名：接收免疫护盾叠加的颜色（ARGB），由宿主决定如何重提交父模型。 */
    public interface IframeSubmitter {
        void submit(int color);
    }

    // 26.1: 组件绑定完成前创建 ItemStack 会抛 NPE，改为首次渲染时懒加载
    private static ItemStack magnetStack;

    private static ItemStack magnetStack() {
        if (magnetStack == null) {
            magnetStack = new ItemStack(PHItemRegistry.MAGNET.get());
        }
        return magnetStack;
    }

    private static final int CLOUD_COUNT = 14;
    private static final Vec3[] CLOUD_OFFSETS = new Vec3[CLOUD_COUNT];
    private static final Vec3[] CLOUD_SCALES = new Vec3[CLOUD_COUNT];
    private static final ShadowHandModel SHADOW_HAND_MODEL =
            new ShadowHandModel(ShadowHandModel.createBodyLayer().bakeRoot());
    private static final BlazingBarModel BLAZING_BAR_MODEL =
            new BlazingBarModel(BlazingBarModel.createBodyLayer().bakeRoot());

    private static final Identifier BLAZE_TEXTURE = Identifier.withDefaultNamespace("textures/entity/blaze/blaze.png");
    private static final LightningBoltData.BoltRenderInfo HEALTH_BOLT_DATA = new LightningBoltData.BoltRenderInfo(
            0.3F, 0.0F, 0.0F, 0.0F, new Vector4f(0.4F, 0, 0, 0.4F), 0.2F);

    /**
     * 共享单实例：所有宠物实体经 update(living,…) 按实体为键写入，渲染走 renderOwner 只画自己的 bolt（多实体共享实例的安全路径，与
     * ChainLightningRender 同款）。
     */
    private static final LightningRender LIGHTNING_RENDER = new LightningRender();

    static {
        Random random = new Random(500);
        for (int i = 0; i < CLOUD_COUNT; i++) {
            CLOUD_OFFSETS[i] = new Vec3(random.nextFloat() - 0.5F, 0.2F * (random.nextFloat() - 0.5F), random.nextFloat() - 0.5F)
                    .scale(1.2F);
            CLOUD_SCALES[i] = new Vec3(
                    0.6F + random.nextFloat() * 0.2F, 0.4F + random.nextFloat() * 0.2F, 0.4F + random.nextFloat() * 0.2F);
        }
    }

    private PetOverlayRenderer() {}

    /** 从客户端 level 按 renderState 坐标反查实体（extract 阶段实体不在 renderState 上）。 */
    public static LivingEntity findEntity(LivingEntityRenderState state) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return null;
        AABB aabb = new AABB(
                state.x - 0.5, state.y - 0.5, state.z - 0.5, state.x + 0.5, state.y + 0.5, state.z + 0.5);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb, e -> e.getType() == state.entityType);
        return entities.isEmpty() ? null : entities.get(0);
    }

    /**
     * 绘制全部宠物 overlay。调用点 poseStack 须已处于原版 layer 提交状态（见类注释）。
     *
     * @param iframeSubmitter 免疫护盾激活时以叠加颜色回调一次，宿主用父模型重提交一层
     */
    public static void renderOverlays(
            LivingEntity living,
            LivingEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            IframeSubmitter iframeSubmitter) {
        if (!TameableUtils.couldBeTamed(living)) return;

        float partialTicks = state.ageInTicks - living.tickCount;
        float bodyYaw = Mth.rotLerp(partialTicks, living.yBodyRotO, living.yBodyRot);
        float realAge = state.ageInTicks;

        // ===== 1. IMMUNITY_FRAME: 闪烁护盾叠加 =====
        if (TameableUtils.hasEnchant(living, PHEnchantments.IMMUNITY_FRAME)
                && TameableUtils.getImmuneTime(living) > 0) {
            float alpha = Math.min(TameableUtils.getImmuneTime(living), 20) / 20.0F;
            int color = (int) (alpha * 255) << 24 | 0xFFFFFF;
            iframeSubmitter.submit(color);
        }

        // ===== 2. MAGNETIC: 磁铁吸力标识 =====
        if (TameableUtils.hasEnchant(living, PHEnchantments.MAGNETIC)) {
            Entity suck = TameableUtils.getPetAttackTarget(living);
            if (suck != null) {
                double d0 = Mth.lerp(partialTicks, suck.xo, suck.getX()) - Mth.lerp(partialTicks, living.xo, living.getX());
                double d1 = Mth.lerp(partialTicks, suck.yo, suck.getY()) - Mth.lerp(partialTicks, living.yo, living.getY());
                double d2 = Mth.lerp(partialTicks, suck.zo, suck.getZ()) - Mth.lerp(partialTicks, living.zo, living.getZ());
                double d4 = Math.sqrt(d0 * d0 + d2 * d2);
                float f1 = (float) (Mth.atan2(d2, d0) * (180F / Math.PI)) - 90.0F;
                float f2 = (float) (-(Mth.atan2(d1, d4) * (180F / Math.PI)));

                poseStack.pushPose();
                poseStack.mulPose(Axis.YN.rotationDegrees(bodyYaw));
                poseStack.mulPose(Axis.YP.rotationDegrees(f1));
                poseStack.mulPose(Axis.XP.rotationDegrees(f2));
                poseStack.pushPose();
                float bob1 = (float) Math.sin(realAge * 0.5F) * 0.05F;
                float bob2 = (float) Math.sin(realAge * 0.3F) * 0.09F - 0.03F;
                float bob3 = (float) Math.cos(realAge * 0.1F) * 0.05F;
                poseStack.translate(
                        bob1, 1.25F - state.boundingBoxHeight * 0.5F - bob2, -state.boundingBoxWidth - 0.125F - bob3);
                poseStack.mulPose(Axis.XN.rotationDegrees(90));
                poseStack.scale(1.6F, 1.6F, 3.0F);

                ItemStackRenderState itemState = new ItemStackRenderState();
                Minecraft.getInstance()
                        .getItemModelResolver()
                        .updateForTopItem(
                                itemState, magnetStack(), ItemDisplayContext.GROUND, living.level(), null, living.getId());
                itemState.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, -1);
                poseStack.popPose();
                poseStack.popPose();
            }
        }

        // ===== 3. HEALTH_SIPHON: 生命汲取闪电 =====
        if (TameableUtils.hasEnchant(living, PHEnchantments.HEALTH_SIPHON)) {
            Entity owner = TameableUtils.getOwnerOf(living);
            if (owner != null && owner.isAlive() && owner.distanceTo(living) < 100) {
                float x = (float) Mth.lerp(partialTicks, living.xo, living.getX());
                float y = (float) Mth.lerp(partialTicks, living.yo, living.getY());
                float z = (float) Mth.lerp(partialTicks, living.zo, living.getZ());
                if (living.hurtTime > 0 && living.hurtTime == living.hurtDuration - 1) {
                    float height = -2 + state.boundingBoxHeight * 0.8F;
                    float ownerHeight = -2 + owner.getBbHeight() * 0.6F;
                    LightningBoltData bolt = new LightningBoltData(
                                    HEALTH_BOLT_DATA,
                                    new Vec3(x, y + height, z),
                                    new Vec3(
                                            Mth.lerp(partialTicks, owner.xo, owner.getX()),
                                            Mth.lerp(partialTicks, owner.yo, owner.getY()) + ownerHeight,
                                            Mth.lerp(partialTicks, owner.zo, owner.getZ())),
                                    3)
                            .size(0.5F)
                            .lifespan(5)
                            .spawn(LightningBoltData.SpawnFunction.NO_DELAY);
                    LIGHTNING_RENDER.update(living, bolt, partialTicks);
                }
                poseStack.pushPose();
                poseStack.mulPose(Axis.YN.rotationDegrees(bodyYaw));
                poseStack.mulPose(Axis.XN.rotationDegrees(180));
                poseStack.pushPose();
                poseStack.translate(-x, -y, -z);
                LIGHTNING_RENDER.renderOwner(living, partialTicks, poseStack, submitNodeCollector);
                poseStack.popPose();
                poseStack.popPose();
            }
        }

        // ===== 4. VOID_CLOUD: 虚空云朵 =====
        if (TameableUtils.hasEnchant(living, PHEnchantments.VOID_CLOUD)
                && !living.isInWater()
                && !living.isInWaterOrRain()
                && !living.onGround()
                && TameableUtils.getFallDistance(living) >= 3.0F) {
            poseStack.pushPose();
            poseStack.translate(0.4F, 1.25F + state.boundingBoxHeight, 0.2F);
            poseStack.mulPose(Axis.YN.rotationDegrees(bodyYaw));
            poseStack.mulPose(Axis.XN.rotationDegrees(180));
            for (int i = 0; i < CLOUD_COUNT; i++) {
                float xSin = (float) Math.sin(realAge * 0.05F + i * 2) * 0.1F;
                float ySin = (float) Math.cos(realAge * 0.05F + i * 2) * 0.1F;
                float zSin = (float) Math.sin(realAge * 0.05F + i * 2 - 2) * 0.1F;
                poseStack.pushPose();
                poseStack.translate(CLOUD_OFFSETS[i].x + xSin, CLOUD_OFFSETS[i].y + ySin, CLOUD_OFFSETS[i].z + zSin);
                poseStack.scale(
                        (float) CLOUD_SCALES[i].x + xSin,
                        (float) CLOUD_SCALES[i].y + ySin,
                        (float) CLOUD_SCALES[i].z + xSin);
                submitNodeCollector.submitCustomGeometry(
                        poseStack, PHRenderTypes.VOID_CLOUD, (pose, buffer) -> renderVoidCloudCube(pose, buffer));
                poseStack.popPose();
            }
            poseStack.popPose();
        }

        // ===== 5. SHADOW_HANDS: 暗影护手 =====
        int shadowHandCount = TameableUtils.getEnchantLevel(living, PHEnchantments.SHADOW_HANDS);
        if (shadowHandCount > 0) {
            // 与服务端 handleShadowHandsLogic 的过滤一致：玩家/驯服动物不是合法目标，不做出拳位移；
            // 服务端只在 16 格内出拳，超出 16 格的目标（即使有进度残留）也不渲染位移
            Entity attackTarget = TameableUtils.getPetAttackTarget(living);
            Entity punching = (attackTarget instanceof Player || attackTarget instanceof TamableAnimal) ? null : attackTarget;
            if (punching != null && living.distanceTo(punching) >= 16) punching = null;
            double d0 = 0, d1 = 0, d2 = 0;
            if (punching != null) {
                d0 = Mth.lerp(partialTicks, punching.xo, punching.getX()) - Mth.lerp(partialTicks, living.xo, living.getX());
                d1 = Mth.lerp(partialTicks, punching.yo, punching.getY()) - Mth.lerp(partialTicks, living.yo, living.getY());
                d2 = Mth.lerp(partialTicks, punching.zo, punching.getZ()) - Mth.lerp(partialTicks, living.zo, living.getZ());
            }
            double d4 = Math.sqrt(d0 * d0 + d2 * d2);
            float f1 = (float) (Mth.atan2(d2, d0) * (180F / Math.PI)) - 90.0F;
            float f2 = (float) (-(Mth.atan2(d1, d4) * (180F / Math.PI)));

            for (int i = 0; i < shadowHandCount; i++) {
                float punch = getPunchFor(living, i, partialTicks) / 10F;
                float xSpread = shadowHandCount <= 1 ? 0F : ((i) / (float) (shadowHandCount - 1)) - 0.5F;
                float xOffset = shadowHandCount <= 1 ? 0 : Mth.sin((float) (xSpread * Math.PI)) * 1.8F;
                float zOffset = shadowHandCount <= 1 ? 0.25F : Mth.cos((float) (xSpread * Math.PI)) * 1.8F;
                float yOffset = -zOffset * 0.3F;
                Vec3 fromPos = new Vec3(0, 1.25F - state.boundingBoxHeight * 0.5F, -state.boundingBoxWidth * 0.3F);
                Vec3 handTranslate = new Vec3(
                        xOffset * state.boundingBoxWidth,
                        1 + yOffset + Math.sin(realAge * 0.2F + i * 1.5F) * 0.15F,
                        zOffset * state.boundingBoxWidth);
                if (punching != null) {
                    Vec3 vec3 = new Vec3(d0, d1, d2).scale(punch);
                    handTranslate = handTranslate.add(vec3).yRot((float) Math.PI + f1 * (float) (Math.PI / 180));
                }

                poseStack.pushPose();
                if (punching != null) {
                    poseStack.mulPose(Axis.YN.rotationDegrees(bodyYaw));
                    poseStack.mulPose(Axis.YP.rotationDegrees(f1));
                    poseStack.mulPose(Axis.XP.rotationDegrees(f2));
                }
                poseStack.pushPose();
                poseStack.translate(handTranslate.x, handTranslate.y, handTranslate.z);
                poseStack.pushPose();
                poseStack.translate(0, -1.15F, -0.15F);
                // submitModel 延迟绘制时才调 setupAnim（ModelFeatureRenderer），每只手 submit 各自
                // 的 state 即可携带独立姿态，无需在回调内手动 animate
                submitNodeCollector.submitModel(
                        SHADOW_HAND_MODEL,
                        new ShadowHandModel.ShadowHandState(punch, i, shadowHandCount, realAge),
                        poseStack,
                        PHRenderTypes.SHADOW_HAND_ENTITY,
                        lightCoords,
                        LivingEntityRenderer.getOverlayCoords(state, 0),
                        0,
                        null);
                poseStack.popPose();
                poseStack.popPose();

                // 渲染暗影线
                poseStack.pushPose();
                renderShadowString(living, fromPos, partialTicks, poseStack, submitNodeCollector, handTranslate);
                poseStack.popPose();
                poseStack.popPose();
            }
        }

        // ===== 6. BLAZING_PROTECTION: 烈焰防护条 =====
        if (TameableUtils.hasEnchant(living, PHEnchantments.BLAZING_PROTECTION)) {
            int bars = TameableUtils.getBlazingProtectionBars(living);
            if (bars > 0) {
                float f1 = realAge * 7;
                float separation =
                        360.0F / (TameableUtils.getEnchantLevel(living, PHEnchantments.BLAZING_PROTECTION) * 2.0F);
                poseStack.pushPose();
                poseStack.mulPose(Axis.YN.rotationDegrees(bodyYaw));
                for (int i = 0; i < bars; i++) {
                    f1 += separation;
                    poseStack.pushPose();
                    poseStack.mulPose(Axis.YP.rotationDegrees(f1));
                    float bob = (float) Math.sin(realAge * 0.6F + Math.toRadians(separation * i)) * 0.15F - 0.07F;
                    poseStack.translate(0, 0.4F - state.boundingBoxHeight * 0.5F - bob, -state.boundingBoxWidth - 0.2F);
                    // 多条烈焰条共用一个模型实例：submitModel 携带各自 rotY，延迟绘制时
                    // ModelFeatureRenderer 逐 submit 调 setupAnim(Float)，姿态互不覆盖
                    submitNodeCollector.submitModel(
                            BLAZING_BAR_MODEL,
                            f1,
                            poseStack,
                            RenderTypes.entityTranslucentEmissive(BLAZE_TEXTURE),
                            240,
                            LivingEntityRenderer.getOverlayCoords(state, 0),
                            0,
                            null);
                    poseStack.popPose();
                }
                poseStack.popPose();
            }
        }

        // ===== 7. HEALING_AURA: 治愈光环 =====
        if (TameableUtils.hasEnchant(living, PHEnchantments.HEALING_AURA)) {
            int t = TameableUtils.getHealingAuraTime(living);
            if (t > 0) {
                float time = t > 20 ? 200 - Math.max(180, t + partialTicks) : t - partialTicks;
                float pulse = 0.9F + (float) (Math.sin(realAge * 0.08F) * 0.1F + 0.1F);
                float healscale = (Math.min(time, 20) / 20F) * 2.2F * pulse;
                poseStack.pushPose();
                poseStack.translate(0, 1.8F - state.boundingBoxHeight * 0.5F, 0);
                poseStack.mulPose(Axis.YN.rotationDegrees(bodyYaw));
                poseStack.mulPose(Axis.YP.rotationDegrees(realAge * 3));
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                poseStack.scale(3 * healscale, 3 * healscale, 3 * healscale);
                submitNodeCollector.submitCustomGeometry(
                        poseStack,
                        PHRenderTypes.HEALING_AURA,
                        (pose, buffer) -> {
                            vertex(buffer, pose, 240, 0.0F, 0, 0, 1, 1);
                            vertex(buffer, pose, 240, 1.0F, 0, 1, 1, 1);
                            vertex(buffer, pose, 240, 1.0F, 1, 1, 0, 1);
                            vertex(buffer, pose, 240, 0.0F, 1, 0, 0, 1);
                        });
                poseStack.popPose();
            }
        }
    }

    // ===== 辅助方法 =====

    private static float getPunchFor(LivingEntity living, int i, float partialTicks) {
        int[] arr = TameableUtils.getShadowPunchTimes(living);
        if (arr.length > i) {
            if (ClientGameEvents.shadowPunchRenderData.containsKey(living)
                    && ClientGameEvents.shadowPunchRenderData.get(living).length > i) {
                int[] prevArr = ClientGameEvents.shadowPunchRenderData.get(living);
                return prevArr[i] + (arr[i] - prevArr[i]) * partialTicks;
            }
            return arr[i];
        }
        return 0;
    }

    private static void renderShadowString(
            LivingEntity from,
            Vec3 fromVec,
            float partialTicks,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            Vec3 to) {
        double d3 = fromVec.x;
        double d4 = fromVec.y;
        double d5 = fromVec.z;
        poseStack.pushPose();
        poseStack.translate(d3, d4, d5);
        float f = (float) (to.x - d3);
        float f1 = (float) (to.y - d4);
        float f2 = (float) (to.z - d5);
        BlockPos blockpos = BlockPos.containing(fromVec);
        BlockPos blockpos1 = BlockPos.containing(to);
        int j = from.level().getBrightness(LightLayer.BLOCK, blockpos1);
        int k = from.level().getBrightness(LightLayer.SKY, blockpos);
        int l = from.level().getBrightness(LightLayer.SKY, blockpos1);
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.leash(),
                (pose, buffer) -> {
                    // leash 是 TRIANGLE_STRIP 且所有影线共享同一缓冲：相邻 strip 会生成桥接三角形，
                    // 表现为两根手之间连着细线。strip 的正确断开方式是「单顶点」重复：
                    // 开头把段 0 的顶边顶点多写一次、结尾把段 8 的底边顶点多写一次，
                    // 这样相邻影线之间的所有桥接三角形都退化为零面积。
                    addStringVertexAlex(buffer, pose, f, f1, f2, 0, j, k, l, 0.2F, 0.05F, 0.05F, 0.05F, 0, false);
                    for (int i1 = 0; i1 <= 8; ++i1) {
                        float width = 0.05F - (i1 / 8F) * 0.025F;
                        addVertexPairAlex(buffer, pose, f, f1, f2, 0, j, k, l, 0.2F, width, width, width, i1);
                    }
                    addStringVertexAlex(buffer, pose, f, f1, f2, 0, j, k, l, 0.2F, 0.025F, 0.025F, 0.025F, 8, true);
                });
        poseStack.popPose();
    }

    private static void addVertexPairAlex(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float dx,
            float dy,
            float dz,
            int blockLightStart,
            int blockLightEnd,
            int skyLightStart,
            int skyLightEnd,
            float radius,
            float width1,
            float width2,
            float width3,
            int segment) {
        addStringVertexAlex(
                buffer, pose, dx, dy, dz, blockLightStart, blockLightEnd, skyLightStart, skyLightEnd, radius, width1, width2,
                width3, segment, false);
        addStringVertexAlex(
                buffer, pose, dx, dy, dz, blockLightStart, blockLightEnd, skyLightStart, skyLightEnd, radius, width1, width2,
                width3, segment, true);
    }

    /**
     * 写影线带子的单个顶点。{@code second} 为 false 写顶边顶点（带子的"上"边）， 为 true 写底边顶点。供 TRIANGLE_STRIP 的首尾重复顶点（strip
     * 断开）单独调用。
     */
    private static void addStringVertexAlex(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float dx,
            float dy,
            float dz,
            int blockLightStart,
            int blockLightEnd,
            int skyLightStart,
            int skyLightEnd,
            float radius,
            float width1,
            float width2,
            float width3,
            int segment,
            boolean second) {
        float f = (float) segment / 8.0F;
        int i = (int) Mth.lerp(f, (float) blockLightStart, (float) blockLightEnd);
        int j = (int) Mth.lerp(f, (float) skyLightStart, (float) skyLightEnd);
        int k = i << 4 | j << 20;
        float f5 = dx * f;
        float f6 = dy < 0.0F ? dy * f * f : dy - dy * (1.0F - f) * (1.0F - f);
        float f7 = dz * f;
        if (!second) {
            // 顶边 y 偏移是 width2（widthOffset），与 1.20/1.21 的 f6 + widthOffset 一致；
            // 误用 radius（topOffset 0.2）会让带厚从 0.2-2*width 缩成 width（细约 3 倍的回归）
            buffer.addVertex(pose, f5 - width1, f6 + width2, f7 + width3)
                    .setColor(0, 0, 0, 255)
                    .setLight(k);
        } else {
            buffer.addVertex(pose, f5 + width1, f6 + radius - width2, f7 - width3)
                    .setColor(0, 0, 0, 255)
                    .setLight(k);
        }
    }

    private static void renderVoidCloudCube(PoseStack.Pose pose, VertexConsumer buffer) {

        buffer.addVertex(pose, -0.5F, -0.5F, -0.5F).setOverlay(240);
        buffer.addVertex(pose, -0.5F, 0.5F, -0.5F).setOverlay(240);
        buffer.addVertex(pose, 0.5F, 0.5F, -0.5F).setOverlay(240);
        buffer.addVertex(pose, 0.5F, -0.5F, -0.5F).setOverlay(240);
        // 北面
        buffer.addVertex(pose, 0.5F, -0.5F, 0.5F).setOverlay(240);
        buffer.addVertex(pose, 0.5F, 0.5F, 0.5F).setOverlay(240);
        buffer.addVertex(pose, -0.5F, 0.5F, 0.5F).setOverlay(240);
        buffer.addVertex(pose, -0.5F, -0.5F, 0.5F).setOverlay(240);
        // 东面
        buffer.addVertex(pose, 0.5F, -0.5F, -0.5F).setOverlay(240);
        buffer.addVertex(pose, 0.5F, 0.5F, -0.5F).setOverlay(240);
        buffer.addVertex(pose, 0.5F, 0.5F, 0.5F).setOverlay(240);
        buffer.addVertex(pose, 0.5F, -0.5F, 0.5F).setOverlay(240);
        // 西面
        buffer.addVertex(pose, -0.5F, -0.5F, 0.5F).setOverlay(240);
        buffer.addVertex(pose, -0.5F, 0.5F, 0.5F).setOverlay(240);
        buffer.addVertex(pose, -0.5F, 0.5F, -0.5F).setOverlay(240);
        buffer.addVertex(pose, -0.5F, -0.5F, -0.5F).setOverlay(240);
        // 顶面
        buffer.addVertex(pose, -0.5F, 0.5F, -0.5F).setOverlay(240);
        buffer.addVertex(pose, -0.5F, 0.5F, 0.5F).setOverlay(240);
        buffer.addVertex(pose, 0.5F, 0.5F, 0.5F).setOverlay(240);
        buffer.addVertex(pose, 0.5F, 0.5F, -0.5F).setOverlay(240);
        // 底面
        buffer.addVertex(pose, -0.5F, -0.5F, 0.5F).setOverlay(240);
        buffer.addVertex(pose, -0.5F, -0.5F, -0.5F).setOverlay(240);
        buffer.addVertex(pose, 0.5F, -0.5F, -0.5F).setOverlay(240);
        buffer.addVertex(pose, 0.5F, -0.5F, 0.5F).setOverlay(240);
    }

    private static void vertex(
            VertexConsumer buffer, PoseStack.Pose pose, int light, float x, int y, float u, float v, int alphaUnused) {
        buffer.addVertex(pose, x - 0.5F, (float) y - 0.5F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
