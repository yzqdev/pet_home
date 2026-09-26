package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.render.ChainLightningRender;
import com.github.yzqdev.pethome.client.render.LayerManager;
import com.github.yzqdev.pethome.client.render.RecallBallRender;
import com.github.yzqdev.pethome.client.render.RenderFeather;
import com.github.yzqdev.pethome.client.render.RenderGiantBubble;
import com.github.yzqdev.pethome.client.render.RenderHighlightedBlock;
import com.github.yzqdev.pethome.client.render.RenderPsychicWall;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.util.ClientMobTooltip;
import com.github.yzqdev.pethome.util.ItemMobTooltip;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

import java.util.List;


public final class ClientModEvents {

    private ClientModEvents() {
    }

    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(PHEntityRegistry.RECALL_BALL, RecallBallRender::new);
        EntityRendererRegistry.register(PHEntityRegistry.CHAIN_LIGHTNING, ChainLightningRender::new);
        EntityRendererRegistry.register(PHEntityRegistry.GIANT_BUBBLE, RenderGiantBubble::new);
        EntityRendererRegistry.register(PHEntityRegistry.PSYCHIC_WALL, RenderPsychicWall::new);
        EntityRendererRegistry.register(PHEntityRegistry.HIGHLIGHTED_BLOCK, RenderHighlightedBlock::new);
        EntityRendererRegistry.register(PHEntityRegistry.FEATHER, RenderFeather::new);
        EntityRendererRegistry.register(PHEntityRegistry.NET_ENTITY, ThrownItemRenderer::new);
    }

    /** 给所有生物渲染器挂上「宠物叠加层」（项圈/附魔特效） */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void registerLayers() {
        LivingEntityRenderLayerRegistrationCallback.EVENT.register(
                (entityType, entityRenderer, registrationHelper, context) -> {
                    if (entityRenderer instanceof LivingEntityRenderer livingEntityRenderer) {
                        try {
                            LayerManager.applyLayer(registrationHelper, livingEntityRenderer);
                        } catch (Exception e) {
                            PetHomeMod.LOGGER.warn("Failed to apply pet overlays layer to {}: {}",
                                    BuiltInRegistries.ENTITY_TYPE.getKey(entityType), e.getMessage());
                        }
                    }
                });
    }

    public static void registerTooltipComponents() {
        ClientTooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof ItemMobTooltip itemMobTooltip) {
                return new ClientMobTooltip(itemMobTooltip);
            }
            return null;
        });
    }

    /** 附魔书 tooltip：显示宠物附魔说明（逻辑复用 ServerEvent.onItemTooltip） */
    public static void registerItemTooltips() {
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) ->
                ServerEvent.onItemTooltip((List<Component>) lines, stack));
    }
}
