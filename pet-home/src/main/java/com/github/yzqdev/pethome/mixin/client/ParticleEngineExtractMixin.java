package com.github.yzqdev.pethome.mixin.client;

import com.github.yzqdev.pethome.client.particle.PhantomParticleGroup;
import com.github.yzqdev.pethome.mixin.ParticleEngineAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * 26.1 原版（Fabric 侧）{@code ParticleEngine.extract} 只遍历静态 {@code RENDER_ORDER}
 * （SINGLE_QUADS / ITEM_PICKUP / ELDER_GUARDIANS，javap 实证）；NeoForge 以 patch 在构造器里维护实例字段
 * {@code particleRenderOrder} 并经 {@code RegisterParticleGroupsEvent} 让 mod 追加自定义类型。
 * Fabric 无该 patch：{@code PH_PHANTOM} 组即使由 {@code ParticleEngineGroupMixin} 正确创建，
 * 也永远不会被 extract → 组内粒子（偏转护盾模型 / 恐吓幻影）整组不渲染。
 * 此处在 extract 尾部补提本模组的自定义组（对齐 NeoForge patch 的 extract 遍历语义）。
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineExtractMixin {

    /**
     * 原版 extract 收尾后，把 {@code PH_PHANTOM} 组的渲染状态补进 {@link ParticlesRenderState}。
     * {@code particles} 是 private identity map，经 {@link ParticleEngineAccessor} 取出后只读。
     */
    @Inject(method = "extract", at = @At("TAIL"))
    private void pet_home$extractPhantomGroup(ParticlesRenderState state, Frustum frustum, Camera camera,
                                              float partialTickTime, CallbackInfo ci) {
        Map<ParticleRenderType, ParticleGroup<?>> groups = ((ParticleEngineAccessor) this).pet_home$getParticles();
        ParticleGroup<?> group = groups.get(PhantomParticleGroup.PHANTOM);
        if (group != null && !group.isEmpty()) {
            state.add(group.extractRenderState(frustum, camera, partialTickTime));
        }
    }
}
