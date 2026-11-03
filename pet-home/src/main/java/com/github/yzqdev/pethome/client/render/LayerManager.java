package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.compat.geckolib.GeoOverlayCompat;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.entity.EntityRenderer;
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

    private static boolean geckolibLoaded() {
        return FabricLoader.getInstance().isModLoaded("geckolib");
    }

    /** 该实体类型是否允许挂叠加层（当前恒 true：Mojang 未提供"实体是否某子类"的查询，保持全量挂载）。 */
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

    /**
     * GeckoLib 渲染器（GeoEntityRenderer/GeoReplacedEntityRenderer 直接继承 EntityRenderer，
     * 原版 layer 体系挂不上）：适配层按 GeckoLib 公开 API withRenderLayer 挂载。
     *
     * <p>先用 {@code isModLoaded("geckolib")} 短路，GeckoLib 不在场时 compat 类永不被加载
     * （类加载隔离见 {@link GeoOverlayCompat}）。返回 false 表示不是 GeckoLib 渲染器（或挂载失败）。</p>
     */
    public static boolean tryGeckoLibCompat(EntityRenderer<?, ?> renderer) {
        return geckolibLoaded() && GeoOverlayCompat.tryApply(renderer);
    }

    /** 调试用：输出实体类型的注册 id。 */
    public static void logEntityType(EntityType<?> entityType) {
        PetHomeMod.LOGGER.debug("EntityType: {}", BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
    }
}
