package com.github.yzqdev.pethome;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * 模组日志门面（跨侧统一，与 26.3 侧 {@code PHModLogger} 同结构）：所有输出统一加 {@code [pet_home]} 前缀，
 * 避免直接散用 {@code PetHomeMod.LOGGER} 时难定位来源。
 *
 * <p>1.20.1 侧原先由 {@link com.github.yzqdev.pethome.util.DebugLogger} 承担调试日志入口， 现已委托到本类的
 * {@link #debugLog}，新代码统一用本类。
 */
public class PHModLogger {
    private final Logger logger;
    private final String prefix;

    private PHModLogger() {
        this.logger = LogUtils.getLogger();

        this.prefix = "[" + PetHomeMod.MODID + "] ";
    }

    private static class Holder {
        private static final PHModLogger INSTANCE = new PHModLogger();
    }

    /** 取单例。 */
    public static PHModLogger getInstance() {
        return Holder.INSTANCE;
    }

    /** 记录 INFO 级日志（自动加模组前缀）。 */
    public void info(String message, Object... args) {
        logger.info(prefix + message, args);
    }

    /** 记录 WARN 级日志（自动加模组前缀）。 */
    public void warn(String message, Object... args) {
        logger.warn(prefix + message, args);
    }

    /** 记录 ERROR 级日志（自动加模组前缀）。 */
    public void error(String message, Object... args) {
        logger.error(prefix + message, args);
    }

    /** 记录 DEBUG 级日志（自动加模组前缀）。 */
    public void debug(String message, Object... args) {
        logger.debug(prefix + message, args);
    }

    /**
     * 调试日志统一入口，受配置项 {@code debug_mode}（{@code PetHomeConfig#debugMode}，默认关闭）门控：
     * 开启时以 INFO 级输出（默认日志级别即可见），关闭时静默返回； 调用点若有昂贵参数计算，应先用 {@code if
     * (PetHomeMod.CONFIG.debugMode.get())} 显式门控。
     */
    public static void debugLog(String message, Object... args) {
        if (!PetHomeMod.CONFIG.debugMode.get()) {
            return;
        }
        getInstance().info("[debug] " + message, args);
    }
}
