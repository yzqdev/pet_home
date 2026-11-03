package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.compat.geckolib.GeoOverlayCompat;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.Map;

public class LayerManager {

    /** {@link EntityRenderersEvent.AddLayers#getRenderer} 的方法体自带 {@code checkcast LivingEntityRenderer}（{@code return (R) renderers.get(...)} 擦除产物），
     * GeckoLib 渲染器（GeoEntityRenderer 直接继承 EntityRenderer）调它必抛 ClassCastException。缓存反射出的 renderers map 绕过。 */
    private static Field renderersField;

    public static boolean canApply(EntityType<?> type) {
        // 原版没有提供「实体类型是否继承自某个任意父类」的查询手段，只能一律放行，由调用方 try/catch 兜底
        return true;
    }

    @SuppressWarnings("unchecked")
    public static void addLayerIfApplicable(EntityType<? extends LivingEntity> entityType, EntityRenderersEvent.AddLayers event) {
        if (entityType == EntityType.ENDER_DRAGON) {
            return;
        }

        EntityRenderer<?> renderer = getRendererRaw(event, entityType);
        if (renderer == null) {
            return;
        }
        if (renderer instanceof LivingEntityRenderer) {
            LivingEntityRenderer livingRenderer = (LivingEntityRenderer) renderer;
            livingRenderer.addLayer(new LayerPetOverlays(livingRenderer));
        } else if (GeoOverlayCompat.tryApply(renderer)) {
            // GeckoLib 渲染器直接继承 EntityRenderer，走 compat 适配层挂 GeoRenderLayer
        } else {
            PetHomeMod.LOGGER.warn("Could not apply pet overlays layer to " + ForgeRegistries.ENTITY_TYPES.getKey(entityType) + ", renderer=" + renderer.getClass().getName());
        }
    }

    @SuppressWarnings("unchecked")
    private static EntityRenderer<?> getRendererRaw(EntityRenderersEvent.AddLayers event, EntityType<? extends LivingEntity> entityType) {
        try {
            if (renderersField == null) {
                renderersField = EntityRenderersEvent.AddLayers.class.getDeclaredField("renderers");
                renderersField.setAccessible(true);
            }
            Map<EntityType<?>, EntityRenderer<?>> renderers = (Map<EntityType<?>, EntityRenderer<?>>) renderersField.get(event);
            return renderers.get(entityType);
        } catch (Exception e) {
            PetHomeMod.LOGGER.warn("Failed to access AddLayers renderers map reflectively", e);
            return null;
        }
    }
}
