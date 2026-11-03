package com.github.yzqdev.pethome.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * 恐吓粒子：在宠物头顶渲染该宠物的半透明幻影并缓缓升起（1.21 侧行为）。 26.1 侧与 26.3 同走 RenderState 路径：extract 阶段提取
 * EntityRenderState，submit 阶段经 EntityRenderDispatcher.submit 渲染；透明感由升起+淡出的动画节奏表达。
 * 实体 id / xRot / yRot 占用速度通道，走 {@link PhantomParticleGroup} 自定义渲染。
 */
public class ParticleIntimidation extends PhantomParticleGroup.PhantomParticle {

    private final int entityId;
    private final float xRot;
    private final float yRot;

    ParticleIntimidation(
            ClientLevel lvl, double x, double y, double z, int entityId, float xRot, float yRot) {
        super(lvl, x, y, z);
        this.lifetime = 22 + this.random.nextInt(7);
        this.entityId = entityId;
        this.xRot = xRot;
        this.yRot = yRot;
    }

    @Override
    PhantomParticleGroup.ParticleData extractData(Camera camera, float partialTick) {
        Entity entity = PhantomParticleGroup.PhantomParticle.entityById(this.entityId);
        if (entity == null) {
            this.remove();
            return null;
        }
        EntityRenderState renderState;
        try {
            renderState =
                    Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(entity, partialTick);
        } catch (Throwable t) {
            this.remove();
            return null;
        }
        renderState.outlineColor = 0;
        // 幻影不该带出实体的名牌/拴绳/火焰/影子
        renderState.nameTag = null;
        renderState.leashStates = null;
        renderState.shadowPieces.clear();
        renderState.displayFireAnimation = false;

        float lerpAge = this.age + partialTick;
        float[] fade = fade(lerpAge, this.lifetime);
        float fadeIn = fade[0];
        float fadeOut = fade[1];
        float up =
                0.2F + (2F + entity.getBbHeight()) * fadeIn + (float) Math.sin(lerpAge * 0.3F) * 0.03F;
        float down = -3 * fadeOut;
        float scale = (fadeIn - fadeOut) * 1.5F;
        // 1.21 手工模型渲染的锚点在模型颈部，submit 全实体渲染锚点在脚底（内部 -1.501 枢轴），
        // 按缩放补回差值使幻影位置与 1.21 一致
        float yLift = up + down - 1.5F * scale;
        return new Data(new Vec3(this.x, this.y, this.z), renderState, this.yRot, yLift, scale);
    }

    /** 幻影渲染项：渲染状态在 extract 已带实体姿态；submit 时平移到粒子位置并缩放。 */
    private record Data(Vec3 pos, EntityRenderState renderState, float yRot, float yLift, float scale)
            implements PhantomParticleGroup.ParticleData {

        @Override
        public void submit(
                PoseStack poseStack,
                SubmitNodeCollector collector,
                CameraRenderState camera,
                Vec3 cameraPos) {
            if (this.scale <= 0.01F) {
                return;
            }
            Vec3 rel = this.relative(cameraPos);
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            poseStack.pushPose();
            poseStack.translate(rel.x, rel.y + this.yLift, rel.z);
            // LivingEntityRenderer.submit 内部会再按 bodyRot 旋转；此处只做整体缩放与朝向修正
            poseStack.scale(this.scale, this.scale, this.scale);
            dispatcher.submit(this.renderState, camera, 0, 0, 0, poseStack, collector);
            poseStack.popPose();
        }
    }

    public record Factory() implements ParticleProvider<SimpleParticleType> {
        @Override
        public ParticleIntimidation createParticle(
                SimpleParticleType type,
                ClientLevel world,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed,
                RandomSource random) {
            return new ParticleIntimidation(world, x, y, z, (int) xSpeed, (float) ySpeed, (float) zSpeed);
        }
    }
}
