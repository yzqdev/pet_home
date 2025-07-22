package com.github.yzqdev.pet_home.client;


import com.github.yzqdev.pet_home.server.entity.PHEntityRegistry;
import com.github.yzqdev.pet_home.util.ClientMobTooltip;
import com.github.yzqdev.pet_home.util.ItemMobTooltip;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;


@EventBusSubscriber(value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerModels(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PHEntityRegistry.NET_ENTITY.get(), ThrownItemRenderer::new);
    }
    @SubscribeEvent
    public static void onRegisterClientTooltip(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ItemMobTooltip.class, ClientMobTooltip::new);

    }
}
