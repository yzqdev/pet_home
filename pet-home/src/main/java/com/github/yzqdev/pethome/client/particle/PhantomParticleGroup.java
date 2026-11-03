package com.github.yzqdev.pethome.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 26.1 粒子架构无 ParticleRenderType.CUSTOM / Particle.render（与 26.3 相同）； 自定义渲染粒子（护盾物品模型、恐吓实体幻影）走自定义
 * ParticleGroup： extract 阶段收集粒子渲染数据，submit 阶段统一渲染。
 * Fabric 侧无 {@code RegisterParticleGroupsEvent}，组类型经 {@code ParticleEngineGroupMixin} 注入 {@code createParticleGroup}，
 * extract 补提经 {@code ParticleEngineExtractMixin}（对齐 NeoForge patch 的 extract 遍历语义）。
 */
public class PhantomParticleGroup extends ParticleGroup<PhantomParticleGroup.PhantomParticle> {

    /** 自定义粒子组类型：PH 前缀避免与原版冲突。 */
    public static final ParticleRenderType PHANTOM = new ParticleRenderType("PH_PHANTOM");

    public PhantomParticleGroup(ParticleEngine engine) {
        super(engine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
        List<ParticleData> list = new ArrayList<>();
        for (PhantomParticle particle : this.particles) {
            ParticleData data = particle.extract(camera, partialTickTime, frustum);
            if (data != null) {
                list.add(data);
            }
        }
        return new State(list);
    }

    /** 渲染项：粒子世界坐标 + 已计算的动画参数，submit 时转换成相对相机坐标。 */
    public interface ParticleData {
        /** @return 粒子世界坐标 */
        Vec3 pos();

        /** @return 相对相机的偏移 */
        default Vec3 relative(Vec3 cameraPos) {
            return this.pos().subtract(cameraPos);
        }

        void submit(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, Vec3 cameraPos);
    }

    private record State(List<ParticleData> particles) implements ParticleGroupRenderState {
        @Override
        public void submit(SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
            PoseStack poseStack = new PoseStack();
            Vec3 cameraPos = camera.pos;
            for (ParticleData data : this.particles) {
                data.submit(poseStack, submitNodeCollector, camera, cameraPos);
            }
        }
    }

    /** 自定义渲染粒子基类：tick 不移动（静止展示），仅计时消失。 */
    public abstract static class PhantomParticle extends Particle {

        protected PhantomParticle(ClientLevel level, double x, double y, double z) {
            super(level, x, y, z);
            this.setSize(1, 1);
            this.gravity = 0.0F;
        }

        @Override
        public ParticleRenderType getGroup() {
            return PHANTOM;
        }

        /** 视锥剔除 + 提取渲染数据；视锥外或实体缺失返回 null。 */
        ParticleData extract(Camera camera, float partialTick, Frustum frustum) {
            if (!frustum.pointInFrustum(this.x, this.y, this.z)) {
                return null;
            }
            return this.extractData(camera, partialTick);
        }

        abstract ParticleData extractData(Camera camera, float partialTick);

        /** 26.1 侧淡出系数：fadeIn/fadeOut 各占 15% 生命周期，与 1.21/26.3 侧一致。 */
        static float[] fade(float lerpAge, float lifetime) {
            float ageProgress = lerpAge / lifetime;
            float fadeTime = 0.15F;
            float fadeIn = Math.min(ageProgress, fadeTime) / fadeTime;
            float fadeOut = Mth.clamp(ageProgress - (1F - fadeTime), 0F, fadeTime) / fadeTime;
            return new float[]{fadeIn, fadeOut};
        }

        /** 从粒子位置取该实体（恐吓幻影用）；实体不存在返回 null。 */
        static Entity entityById(int entityId) {
            return Minecraft.getInstance().level.getEntity(entityId);
        }
    }
}
