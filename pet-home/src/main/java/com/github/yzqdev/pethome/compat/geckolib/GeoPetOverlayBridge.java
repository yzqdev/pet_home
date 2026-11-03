package com.github.yzqdev.pethome.compat.geckolib;

import net.minecraft.client.renderer.entity.EntityRenderer;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * GeckoLib 渲染器识别与挂载桥（本包内仅有的 import GeckoLib 类型的类之一）。
 *
 * <p>4.x 的 layer 泛型为 {@code <T extends GeoAnimatable>}，GeoEntityRenderer / GeoReplacedEntityRenderer
 * 的 animatable 类型各异（实体实例 / 替换动画对象），统一以 raw type 表达，运行时按 instanceof 分发。
 */
@SuppressWarnings({"unchecked", "rawtypes"})
final class GeoPetOverlayBridge {

    private GeoPetOverlayBridge() {
    }

  /** 见 {@link GeoOverlayCompat#tryApply}。 */
  static boolean apply(EntityRenderer<?> renderer) {
    if (renderer instanceof GeoEntityRenderer geoEntityRenderer) {
            geoEntityRenderer.addRenderLayer(
                (GeoRenderLayer) new GeoPetOverlayLayer((GeoRenderer) geoEntityRenderer));
            return true;
        }
        if (renderer instanceof GeoReplacedEntityRenderer geoReplacedRenderer) {
            geoReplacedRenderer.addRenderLayer(
                (GeoRenderLayer) new GeoPetOverlayLayer((GeoRenderer) geoReplacedRenderer));
            return true;
        }
        return false;
    }
}
