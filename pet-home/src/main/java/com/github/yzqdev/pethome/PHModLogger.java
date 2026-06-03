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
}