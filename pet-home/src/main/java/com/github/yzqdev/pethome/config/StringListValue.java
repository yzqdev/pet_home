package com.github.yzqdev.pethome.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 字符串列表配置项，对应 toml 中的 {@code ["a", "b"]}。
 * 每一项都会经过 {@code validator} 校验，非法项在写入时被丢弃。
 */
public class StringListValue extends ConfigValue<List<String>> {

    private final Predicate<Object> validator;

    public StringListValue(String path, String comment, List<String> defaultValue, Predicate<Object> validator) {
        super(path, comment, new ArrayList<>(defaultValue == null ? List.of() : defaultValue));
        this.validator = validator;
    }

    /** 校验器（可能为 null，表示不做校验） */
    public Predicate<Object> getValidator() {
        return validator;
    }

    @Override
    protected List<String> sanitize(List<String> value) {
        List<String> result = new ArrayList<>();
        if (value == null) {
            return result;
        }
        for (String entry : value) {
            if (entry == null) {
                continue;
            }
            String trimmed = entry.trim();
            if (trimmed.isEmpty() || result.contains(trimmed)) {
                continue;
            }
            if (validator != null && !validator.test(trimmed)) {
                continue;
            }
            result.add(trimmed);
        }
        return result;
    }

    @Override
    public boolean read(String raw) {
        List<String> parsed = Toml.parseStringArray(raw);
        if (parsed == null) {
            return false;
        }
        set(parsed);
        return true;
    }

    @Override
    public String write() {
        return Toml.writeStringArray(get());
    }
}
