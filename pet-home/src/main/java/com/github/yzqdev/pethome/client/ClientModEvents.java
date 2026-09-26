package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.util.ClientMobTooltip;
import com.github.yzqdev.pethome.util.ItemMobTooltip;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityType;
import com.github.yzqdev.pethome.client.render.ChainLightningRender;
import com.github.yzqdev.pethome.client.render.LayerPetOverlays;
import com.github.yzqdev.pethome.client.render.RecallBallRender;
import com.github.yzqdev.pethome.client.render.RenderFeather;
import com.github.yzqdev.pethome.client.render.RenderGiantBubble;
import com.github.yzqdev.pethome.client.render.RenderHighlightedBlock;
import com.github.yzqdev.pethome.client.render.RenderPsychicWall;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class ClientModEvents {

    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(PHEntityRegistry.RECALL_BALL, RecallBallRender::new);
        EntityRendererRegistry.register(PHEntityRegistry.CHAIN_LIGHTNING, ChainLightningRender::new);
        EntityRendererRegistry.register(PHEntityRegistry.GIANT_BUBBLE, RenderGiantBubble::new);
        EntityRendererRegistry.register(PHEntityRegistry.PSYCHIC_WALL, RenderPsychicWall::new);
        EntityRendererRegistry.register(PHEntityRegistry.HIGHLIGHTED_BLOCK, RenderHighlightedBlock::new);
        EntityRendererRegistry.register(PHEntityRegistry.FEATHER, RenderFeather::new);
        EntityRendererRegistry.register(PHEntityRegistry.NET_ENTITY, ThrownItemRenderer::new);
    }

    public static void registerLayers() {
       LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
                (entityType, entityRenderer, registrationHelper, context) -> {
                    if (entityRenderer instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer<?, ?> livingEntityRenderer) {
                        try {
                            registrationHelper.register(new LayerPetOverlays(livingEntityRenderer));
                        } catch (Exception e) {
                            com.github.yzqdev.pethome.PetHomeMod.LOGGER.warn("Failed to apply pet overlays layer to {}: {}",
                                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entityType), e.getMessage());
                        }
                    }
                });
    }

    public static void registerTooltipComponents() {
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof ItemMobTooltip itemMobTooltip) {
                return new ClientMobTooltip(itemMobTooltip);
            }
            return null;
        });
    }

    public static void registerItemTooltips() {
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) ->
                com.github.yzqdev.pethome.server.event.ServerEvent.onItemTooltip(stack, (List<net.minecraft.network.chat.Component>) lines));
    }
}
