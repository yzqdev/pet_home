package com.github.yzqdev.pet_home.client.render;

import com.github.yzqdev.pet_home.PetHomeMod;
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
