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
        return true; //mojang provides no way to check if an entity is a child class from a arbitrary superclass from it's entitytype
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
