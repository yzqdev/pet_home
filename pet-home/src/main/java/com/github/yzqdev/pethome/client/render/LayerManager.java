package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;


public class LayerManager {

    public static boolean canApply(EntityType<?> type) {
        // 原版没有提供「实体类型是否继承自某个任意父类」的查询手段，只能一律放行，由调用方 try/catch 兜底
        return true;
    }

    public static void addLayerIfApplicable(EntityType<? extends LivingEntity> entityType, EntityRenderersEvent.AddLayers event) {
        if (entityType == EntityType.ENDER_DRAGON) {
            return;
        }


        try {
            EntityRenderer<?> renderer = event.getRenderer(entityType);
            if (renderer instanceof LivingEntityRenderer<?, ?> livingEntityRenderer) {
                livingEntityRenderer.addLayer(new LayerPetOverlays(livingEntityRenderer));
            } else {
                PetHomeMod.LOGGER.warn("Could not apply pet overlays layer to {}. Renderer is not a LivingEntityRenderer.",
                        BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
            }
        } catch (Exception e) {
            PetHomeMod.LOGGER.warn("Failed to apply pet overlays layer to {}: {}",
                    BuiltInRegistries.ENTITY_TYPE.getKey(entityType), e.getMessage());
        }


    }
}
