package com.github.yzqdev.pethome.config;

/** 整数配置项，带 [min, max] 范围钳制（对应 Forge 的 {@code defineInRange(int)}）。 */
public class IntValue extends ConfigValue<Integer> {

    private final int min;
    private final int max;

    public IntValue(String path, String comment, int defaultValue, int min, int max) {
        super(path, comment, defaultValue);
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    protected Integer sanitize(Integer value) {
        if (value == null) {
            return getDefault();
        }
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public boolean read(String raw) {
        try {
            set(Integer.valueOf(raw.trim()));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String write() {
        return String.valueOf(get());
    }
}
