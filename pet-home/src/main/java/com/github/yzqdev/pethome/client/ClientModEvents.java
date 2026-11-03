package com.github.yzqdev.pethome.client;


import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.util.ClientMobTooltip;
import com.github.yzqdev.pethome.util.ItemMobTooltip;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;


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

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        PetInfoHudOverlay.registerGuiLayers(event);
    }

    /** 捕网发射器蓄力谓词（模型 overrides 按 pet_home:charge 切换能量核心贴图）。 */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                PHItemRegistry.NET_LAUNCHER_ITEM.get(),
                ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "charge"),
                (stack, level, holder, seed) -> {
                    if (holder == null || !holder.isUsingItem() || holder.getUseItem() != stack) {
                        return 0.0F;
                    }
                    return Math.min(
                            (stack.getUseDuration(holder) - holder.getUseItemRemainingTicks()) / 20.0F,
                            1.0F);
                }));
    }
}
