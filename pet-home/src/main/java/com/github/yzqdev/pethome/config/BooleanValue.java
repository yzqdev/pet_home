package com.github.yzqdev.pethome.config;

import java.util.Locale;

/** 布尔配置项，对应 toml 中的 {@code true} / {@code false}。 */
public class BooleanValue extends ConfigValue<Boolean> {

    public BooleanValue(String path, String comment, boolean defaultValue) {
        super(path, comment, defaultValue);
    }

    @Override
    public boolean read(String raw) {
        String text = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(text)) {
            set(Boolean.TRUE);
            return true;
        }
        if ("false".equals(text)) {
            set(Boolean.FALSE);
            return true;
        }
        return false;
    }

    @Override
    public String write() {
        return String.valueOf(get());
    }
}
