package com.github.yzqdev.pethome.mixin.client;

import com.github.yzqdev.pethome.client.particle.PhantomParticleGroup;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.1 的 {@code ParticleEngine.createParticleGroup} 是私有硬编码分发（ITEM_PICKUP / ELDER_GUARDIANS /
 * NO_RENDER / 其余落 QuadParticleGroup），Fabric API 无粒子组注册钩子（{@code RegisterParticleGroupsEvent}
 * 是 NeoForge 专有）；自定义渲染粒子组在此拦截 HEAD 返回，避免落入 QuadParticleGroup 后对非
 * SingleQuadParticle 粒子 ClassCastException。
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineGroupMixin {

    @Inject(method = "createParticleGroup", at = @At("HEAD"), cancellable = true)
    private void pet_home$createPhantomGroup(ParticleRenderType type, CallbackInfoReturnable<ParticleGroup<?>> cir) {
        if (type == PhantomParticleGroup.PHANTOM) {
            cir.setReturnValue(new PhantomParticleGroup((ParticleEngine) (Object) this));
        }
    }
}
