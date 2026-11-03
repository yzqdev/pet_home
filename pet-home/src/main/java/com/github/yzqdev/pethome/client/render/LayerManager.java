package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.compat.geckolib.GeoOverlayCompat;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;


public class LayerManager {

    private static boolean geckolibLoaded() {
        return ModList.get().isLoaded("geckolib");
    }

    public static boolean canApply(EntityType<?> type) {
        // 原版没有提供「实体类型是否继承自某个任意父类」的查询手段，只能一律放行，由调用方 try/catch 兜底
        return true;
    }

    public static void addLayerIfApplicable(EntityType<? extends LivingEntity> entityType, EntityRenderersEvent.AddLayers event) {
        if (entityType == EntityType.ENDER_DRAGON) {
            return;
        }


        try {
            EntityRenderer<?, ?> renderer = event.getRenderer(entityType);
            if (renderer instanceof LivingEntityRenderer<?, ?, ?> livingEntityRenderer) {
                // 26.1: addLayer 要求 RenderLayerParent<S, M>；LivingEntityRenderer 实现了该接口，
                // 但 S 是具体子类的 RenderState，需要按渲染器自身的泛型参数构造
                applyLayer(livingEntityRenderer);
            } else if (geckolibLoaded() && GeoOverlayCompat.tryApply(renderer)) {
                // GeckoLib 渲染器（GeoEntityRenderer/GeoReplacedEntityRenderer 直接继承 EntityRenderer，
                // 原版 layer 体系挂不上）：适配层按 GeckoLib 公开 API withRenderLayer 挂载
                PetHomeMod.LOGGER.debug(
                        "Applied pet overlays via GeckoLib compat to {}",
                        BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
            } else {
                PetHomeMod.LOGGER.warn("Could not apply pet overlays layer to {}. Renderer is not a LivingEntityRenderer.",
                        BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
            }
        } catch (Exception e) {
            PetHomeMod.LOGGER.warn("Failed to apply pet overlays layer to {}: {}",
                    BuiltInRegistries.ENTITY_TYPE.getKey(entityType), e.getMessage());
        }


    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void applyLayer(LivingEntityRenderer renderer) {
        renderer.addLayer(new LayerPetOverlays(renderer));
    }
}
