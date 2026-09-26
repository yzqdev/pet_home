package com.github.yzqdev.pethome.config;

/** 浮点配置项，带 [min, max] 范围钳制（对应 Forge 的 {@code defineInRange(double)}）。 */
public class DoubleValue extends ConfigValue<Double> {

    private final double min;
    private final double max;

    public DoubleValue(String path, String comment, double defaultValue, double min, double max) {
        super(path, comment, defaultValue);
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    @Override
    protected Double sanitize(Double value) {
        if (value == null || value.isNaN()) {
            return getDefault();
        }
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public boolean read(String raw) {
        try {
            set(Double.valueOf(raw.trim()));
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
