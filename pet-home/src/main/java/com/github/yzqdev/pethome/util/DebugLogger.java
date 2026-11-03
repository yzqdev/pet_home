package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.PHModLogger;
import com.github.yzqdev.pethome.PetHomeMod;

/**
 * 调试日志统一入口，受配置项 {@code debug_mode}（{@code PetHomeConfig#debugMode}，默认关闭）门控：
 * 开启时以 INFO 级输出（默认日志级别即可见），关闭时静默返回； 调用点若有昂贵参数计算，应先用 {@code if
 * (PetHomeMod.CONFIG.debugMode.get())} 显式门控。
 *
 * <p>跨侧统一：实现已迁到 {@code com.github.yzqdev.pethome.PHModLogger}（与 1.21/26.1/26.3 四侧同结构），
 * 本类保留为薄壳委托，避免既有调用点大改；新代码请直接用 {@code PHModLogger.debugLog}。
 */
public final class DebugLogger {

    private DebugLogger() {
    }

    public static void debugLog(String message, Object... args) {
        PHModLogger.debugLog(message, args);
    }
}
