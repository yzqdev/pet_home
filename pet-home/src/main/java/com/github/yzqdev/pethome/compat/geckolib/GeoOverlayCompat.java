package com.github.yzqdev.pethome.compat.geckolib;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.client.renderer.entity.EntityRenderer;

/**
 * GeckoLib overlay 适配对外入口。
 *
 * <p>本类不 import 任何 GeckoLib 类型，未装 GeckoLib 时 {@link GeoPetOverlayBridge} /
 * {@link GeoPetOverlayLayer} 两个持有 GeckoLib 引用的类永不被类加载；GeckoLib 版本变动导致的
 * LinkageError 在此降级为「不挂层」，不影响游戏运行。
 */
public final class GeoOverlayCompat {

    private GeoOverlayCompat() {
    }

    /**
     * 若 renderer 是 GeckoLib 渲染器则挂上宠物 overlay 适配层。
     *
     * @return true 表示是 GeckoLib 渲染器且挂层成功
     */
    public static boolean tryApply(EntityRenderer<?> renderer) {
        try {
            return GeoPetOverlayBridge.apply(renderer);
        } catch (LinkageError | RuntimeException e) {
            PetHomeMod.LOGGER.debug("Failed to apply geckolib pet overlay layer: " + e);
            return false;
        }
    }
}
