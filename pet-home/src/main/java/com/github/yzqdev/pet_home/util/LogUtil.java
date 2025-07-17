package com.github.yzqdev.pet_home.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Minecraft 日志工具类，支持按类名记录日志
 */
public class LogUtil {

    // 日志级别标记
    private static final Marker INFO_MARKER = MarkerManager.getMarker("INFO");
    private static final Marker DEBUG_MARKER = MarkerManager.getMarker("DEBUG");
    private static final Marker WARN_MARKER = MarkerManager.getMarker("WARN");
    private static final Marker ERROR_MARKER = MarkerManager.getMarker("ERROR");

    /**
     * 记录 INFO 级别日志
     * @param clazz 日志来源类
     * @param message 日志消息，支持 {} 占位符
     * @param params 替换占位符的参数
     */
    public static void info(Class<?> clazz, String message, Object... params) {
        getLogger(clazz).log(Level.INFO, INFO_MARKER, formatMessage(clazz, message), params);
    }

    /**
     * 记录 DEBUG 级别日志
     * @param clazz 日志来源类
     * @param message 日志消息，支持 {} 占位符
     * @param params 替换占位符的参数
     */
    public static void debug(Class<?> clazz, String message, Object... params) {
        getLogger(clazz).log(Level.DEBUG, DEBUG_MARKER, formatMessage(clazz, message), params);
    }

    /**
     * 记录 WARN 级别日志
     * @param clazz 日志来源类
     * @param message 日志消息，支持 {} 占位符
     * @param params 替换占位符的参数
     */
    public static void warn(Class<?> clazz, String message, Object... params) {
        getLogger(clazz).log(Level.WARN, WARN_MARKER, formatMessage(clazz, message), params);
    }

    /**
     * 记录 ERROR 级别日志
     * @param clazz 日志来源类
     * @param message 日志消息，支持 {} 占位符
     * @param params 替换占位符的参数
     */
    public static void error(Class<?> clazz, String message, Object... params) {
        getLogger(clazz).log(Level.ERROR, ERROR_MARKER, formatMessage(clazz, message), params);
    }

    /**
     * 记录带异常的 ERROR 日志
     * @param clazz 日志来源类
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public static void error(Class<?> clazz, String message, Throwable throwable) {
        getLogger(clazz).log(Level.ERROR, ERROR_MARKER, formatMessage(clazz, message), throwable);
    }

    // 获取类对应的Logger
    private static Logger getLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }

    // 格式化日志消息：添加类名前缀
    private static String formatMessage(Class<?> clazz, String message) {
        return String.format("[%s] %s", clazz.getSimpleName(), message);
    }

    // 性能优化：仅当需要时构建日志消息
    public static void debugIfEnabled(Class<?> clazz, String message, Object... params) {
        Logger logger = getLogger(clazz);
        if (logger.isDebugEnabled()) {
            debug(clazz, message, params);
        }
    }
}