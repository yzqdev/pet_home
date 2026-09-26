package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.ClientGameEvents;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 替代 Forge RenderNameTagEvent：按住 Shift 查看宠物时绘制附魔列表名牌。
 */
@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(method = "renderNameTag(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    private void ph_renderNametag(Entity entity, Component component, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (ClientGameEvents.renderNametagEvent(entity, component, poseStack, buffer, packedLight)) {
            ci.cancel();
        }
    }
}
