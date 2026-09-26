package com.github.yzqdev.pethome.config;

import java.util.Objects;

/**
 * 单个配置项的值载体：持有「默认值 + 当前值 + 注释 + 点分路径」。
 *
 * <p>本模组的配置系统为 Fabric 端自研实现（不依赖 Forge / NeoForge / ForgeConfigAPIPort）。
 * 每个配置项由 {@link ConfigBuilder} 创建并登记到 {@link ConfigSpec}，由 {@link ConfigSpec}
 * 负责 {@code config/pet_home.toml} 的读写。</p>
 *
 * <p>子类只需实现 {@link #read(String)}（TOML 字面量 → 值）与 {@link #write()}（值 → TOML 字面量），
 * 新增配置项类型时按 {@link BooleanValue} / {@link IntValue} / {@link DoubleValue} / {@link StringListValue} 的模式扩展即可。</p>
 *
 * @param <T> 值类型
 */
public abstract class ConfigValue<T> {
    private final String path;
    private final String comment;
    private final T defaultValue;
    private volatile T current;
    private ConfigSpec owner;

    protected ConfigValue(String path, String comment, T defaultValue) {
        this.path = path;
        this.comment = comment == null ? "" : comment;
        this.defaultValue = defaultValue;
        this.current = defaultValue;
    }

    void attach(ConfigSpec owner) {
        this.owner = owner;
    }

    /** 完整点分路径，例如 {@code general.rotten_apple} */
    public String getPath() {
        return path;
    }

    /** 配置项注释（写入 toml 时以 # 前缀输出） */
    public String getComment() {
        return comment;
    }

    /** 默认值 */
    public T getDefault() {
        return defaultValue;
    }

    /** 当前值（未显式设置过时等于默认值，永不返回 null） */
    public T get() {
        T value = current;
        return value == null ? defaultValue : value;
    }

    /** 写入新值；越界/非法值会被子类的 {@link #sanitize(Object)} 修正 */
    public void set(T value) {
        T sanitized = sanitize(value);
        if (!Objects.equals(sanitized, current)) {
            current = sanitized;
            if (owner != null) {
                owner.markDirty();
            }
        }
    }

    /** 恢复默认值 */
    public void resetToDefault() {
        set(defaultValue);
    }

    /** 当前值是否与默认值不同 */
    public boolean isModified() {
        return !Objects.equals(get(), defaultValue);
    }

    /** 取值范围校验/修正的扩展点 */
    protected T sanitize(T value) {
        return value == null ? defaultValue : value;
    }

    /**
     * 从 TOML 字面量解析并写入当前值。
     *
     * @return 解析成功返回 true；失败返回 false（调用方保留原值并记录告警）
     */
    public abstract boolean read(String raw);

    /** 序列化为 TOML 字面量 */
    public abstract String write();

    /** 配置界面展示用的文本（默认与 {@link #write()} 相同） */
    public String display() {
        return write();
    }

    @Override
    public String toString() {
        return path + "=" + write();
    }
}
