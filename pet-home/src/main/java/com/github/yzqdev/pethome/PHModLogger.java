package com.github.yzqdev.pethome;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/** 模组日志门面：单例包装 slf4j，统一加 {@code [pet_home]} 前缀，便于日志过滤。 */
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
   * debugMode 开关下的诊断日志（info 级别、带 [debug] 前缀）。 关闭时静默返回；调用点若有昂贵参数计算，应先用 {@code if
   * (PetHomeConfig.debugMode)} 显式门控。
   */
  public static void debugLog(String message, Object... args) {
    if (!PetHomeConfig.debugMode) {
      return;
    }
    getInstance().info("[debug] " + message, args);
  }
}
