package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.PetHomeMod;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

/**
 * 宠物叠加层的挂载工具。
 *
 * <p>NeoForge 侧靠 {@code EntityRenderersEvent.AddLayers} 遍历所有实体类型逐个挂载；
 * Fabric 侧由 {@code LivingEntityFeatureRendererRegistrationCallback} 在渲染器构造时回调，
 * 因此这里只保留「给单个渲染器挂层」的能力。</p>
 */
public final class LayerManager {

    private LayerManager() {
    }

    public static boolean canApply(EntityType<?> type) {
        return true; //mojang provides no way to check if an entity is a child class from a arbitrary superclass from it's entitytype
    }

    /**
     * 通过 Fabric 的层注册助手挂载宠物叠加层。
     *
     * <p>26.1 的 {@code LivingEntityRenderer#addLayer} 是 protected，Fabric 侧必须经
     * {@code LivingEntityRenderLayerRegistrationCallback.RegistrationHelper} 注册；
     * 由于渲染器与层都是裸类型，这里统一做一次未检查转换。</p>
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void applyLayer(LivingEntityRenderLayerRegistrationCallback.RegistrationHelper helper,
                                  LivingEntityRenderer renderer) {
        try {
            helper.register((RenderLayer) new LayerPetOverlays(renderer));
        } catch (Exception e) {
            PetHomeMod.LOGGER.warn("Failed to apply pet overlays layer: {}", e.getMessage());
        }
    }

    public static void logEntityType(EntityType<?> entityType) {
        PetHomeMod.LOGGER.debug("EntityType: {}", BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
    }
}
