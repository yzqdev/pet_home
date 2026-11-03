package com.github.yzqdev.pethome.compat.geckolib;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.GeoReplacedEntityRenderer;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.GeoRenderLayer;
import net.minecraft.client.renderer.entity.EntityRenderer;

/**
 * GeckoLib 渲染器识别与挂载桥（本包内仅有的 import GeckoLib 类型的类之一）。
 *
 * <p>泛型说明：GeckoLib 的 {@code R extends EntityRenderState} 同时须满足 {@code GeoRenderer<R extends
 * GeoRenderState>}——该 bound 由 GeckoLib 自身的 duck-typing mixin（EntityRenderStateMixin）在运行时注入满足，编译期原版
 * {@code EntityRenderState} 并未实现该接口。为绕开这一编译期矛盾，本包相关类 全部以 raw type 表达，实际类型在运行时按 instanceof 分发。
 */
@SuppressWarnings({"unchecked", "rawtypes"})
final class GeoPetOverlayBridge {

    private GeoPetOverlayBridge() {
    }

    /** 见 {@link GeoOverlayCompat#tryApply}。 */
    static boolean apply(EntityRenderer<?, ?> renderer) {
        if (renderer instanceof GeoEntityRenderer geoEntityRenderer) {
            geoEntityRenderer.withRenderLayer(
                    (GeoRenderLayer) new GeoPetOverlayLayer((GeoRenderer) geoEntityRenderer));
            return true;
        }
        if (renderer instanceof GeoReplacedEntityRenderer geoReplacedRenderer) {
            geoReplacedRenderer.withRenderLayer(
                    (GeoRenderLayer) new GeoPetOverlayLayer((GeoRenderer) geoReplacedRenderer));
            return true;
        }
        return false;
    }
}
