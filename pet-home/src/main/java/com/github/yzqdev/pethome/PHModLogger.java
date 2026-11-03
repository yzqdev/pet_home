package com.github.yzqdev.pethome;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class PHModLogger {
    private final Logger logger;
    private final String prefix;


    private PHModLogger() {
        this.logger = LogUtils.getLogger();

        this.prefix = "[" +PetHomeMod.MODID + "] ";
    }


    private static class Holder {
        private static final PHModLogger INSTANCE = new PHModLogger();
    }


    public static PHModLogger getInstance() {
        return Holder.INSTANCE;
    }



    public void info(String message, Object... args) {
        logger.info(prefix + message, args);
    }

    public void warn(String message, Object... args) {
        logger.warn(prefix + message, args);
    }

    public void error(String message, Object... args) {
        logger.error(prefix + message, args);
    }

    public void debug(String message, Object... args) {
        logger.debug(prefix + message, args);
    }

    /**
     * 调试日志统一入口，受配置项 {@code debugMode} 门控（默认关闭）：开启时以 INFO 级输出（默认日志级别即可见），
     * 关闭时静默返回；调用点若有昂贵参数计算，应先用 {@code if (PetHomeConfig.debugMode)} 显式门控。
     */
    public static void debugLog(String message, Object... args) {
        if (!PetHomeConfig.debugMode) {
            return;
        }
        getInstance().info("[debug] " + message, args);
    }
}