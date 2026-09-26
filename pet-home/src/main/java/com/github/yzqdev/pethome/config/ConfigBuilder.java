package com.github.yzqdev.pethome.config;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

/**
 * 配置注册器：用法刻意贴近 Forge 的 {@code ForgeConfigSpec.Builder}，便于从 Forge 版平滑迁移。
 *
 * <pre>{@code
 * ConfigBuilder builder = SPEC.builder();
 * builder.push("general");
 * BooleanValue enabled = builder.comment("说明").define("enabled", true);
 * builder.pop();
 * }</pre>
 */
public final class ConfigBuilder {

    private final ConfigSpec spec;
    private final Deque<String> sections = new ArrayDeque<>();
    private String pendingComment = "";

    ConfigBuilder(ConfigSpec spec) {
        this.spec = spec;
    }

    /** 进入一个配置节 */
    public ConfigBuilder push(String section) {
        sections.addLast(section);
        spec.addSection(currentPath());
        pendingComment = "";
        return this;
    }

    /** 退出一个配置节 */
    public ConfigBuilder pop() {
        if (!sections.isEmpty()) {
            sections.removeLast();
        }
        return this;
    }

    /** 设置下一项配置的注释（写入 toml 时以 # 前缀输出） */
    public ConfigBuilder comment(String comment) {
        this.pendingComment = comment == null ? "" : comment;
        return this;
    }

    /**
     * 兼容 Forge 写法：Forge 用它绑定翻译键。本模组的配置界面直接使用下面的 comment 文本，
     * 因此这里是空实现，只为让迁移过来的调用点保持原样。
     */
    public ConfigBuilder translation(String translationKey) {
        return this;
    }

    public BooleanValue define(String name, boolean defaultValue) {
        return spec.add(new BooleanValue(path(name), takeComment(), defaultValue));
    }

    public IntValue defineInRange(String name, int defaultValue, int min, int max) {
        return spec.add(new IntValue(path(name), takeComment(), defaultValue, min, max));
    }

    public DoubleValue defineInRange(String name, double defaultValue, double min, double max) {
        return spec.add(new DoubleValue(path(name), takeComment(), defaultValue, min, max));
    }

    /**
     * 定义字符串列表配置项。
     *
     * @param validator 逐项校验器（可为 null）
     */
    public StringListValue defineListAllowEmpty(String name, List<String> defaultValue, Predicate<Object> validator) {
        return spec.add(new StringListValue(path(name), takeComment(), defaultValue, validator));
    }

    /** 兼容 Forge 的 {@code defineListAllowEmpty("key", supplier, validator)} 形式 */
    public StringListValue defineListAllowEmpty(String name, java.util.function.Supplier<List<String>> defaultValue, Predicate<Object> validator) {
        return defineListAllowEmpty(name, defaultValue.get(), validator);
    }

    private String takeComment() {
        String comment = pendingComment;
        pendingComment = "";
        return comment;
    }

    private String path(String name) {
        String prefix = currentPath();
        return prefix.isEmpty() ? name : prefix + "." + name;
    }

    private String currentPath() {
        return String.join(".", sections);
    }
}
