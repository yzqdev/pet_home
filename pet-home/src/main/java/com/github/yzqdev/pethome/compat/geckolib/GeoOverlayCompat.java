package com.github.yzqdev.pethome.compat.geckolib;

import com.github.yzqdev.pethome.PHModLogger;
import net.minecraft.client.renderer.entity.EntityRenderer;

/**
 * GeckoLib 生物 overlay 兼容入口。
 *
 * <p>背景：GeckoLib 的 {@code GeoEntityRenderer}/{@code GeoReplacedEntityRenderer} 直接继承 {@code
 * EntityRenderer}（非 {@code LivingEntityRenderer}），原版 RenderLayer 体系挂不上， 导致全部宠物
 * overlay（暗影之手/虚空云/免疫护盾/治疗之息/烈焰防护条/暗影线）在 GeckoLib 生物上不显示。本包经 GeckoLib 公开 API {@code
 * withRenderLayer(GeoRenderLayer)} 挂适配层。
 *
 * <p>类加载隔离：本类不 import 任何 GeckoLib 类型，GeckoLib 引用全部在 {@link GeoPetOverlayBridge}/{@link
 * GeoPetOverlayLayer}；调用方必须先用 {@code ModList.isLoaded("geckolib")} 门控，GeckoLib 不在场时这两个类永不被加载。 本类兜底
 * {@link LinkageError}（依赖版本变动导致的方法缺失等），失败只降级为原行为。
 */
public final class GeoOverlayCompat {

    private GeoOverlayCompat() {}

    /**
     * 尝试给 GeckoLib 渲染器挂宠物 overlay 适配层。
     *
     * @return true 表示 renderer 是 GeckoLib 渲染器且挂载成功；false 表示不是 GeckoLib 渲染器
     */
    public static boolean tryApply(EntityRenderer<?, ?> renderer) {
        try {
            return GeoPetOverlayBridge.apply(renderer);
        } catch (LinkageError | RuntimeException e) {
            PHModLogger.debugLog(
                    "[layer-debug] Failed to apply pet overlay to GeckoLib renderer {}: {}",
                    renderer.getClass().getName(),
                    e);
            return false;
        }
    }
}
