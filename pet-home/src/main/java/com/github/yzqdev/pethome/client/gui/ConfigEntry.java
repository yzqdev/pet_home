package com.github.yzqdev.pethome.client.gui;

import com.github.yzqdev.pethome.config.BooleanValue;
import com.github.yzqdev.pethome.config.DoubleValue;
import com.github.yzqdev.pethome.config.IntValue;
import com.github.yzqdev.pethome.config.StringListValue;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;


public abstract class ConfigEntry {
    /** 每行高度 */
    public static final int ROW_HEIGHT = 24;

    public final String label;
    @Nullable
    public final String tooltip;

    protected ConfigEntry(String label, @Nullable String tooltip) {
        this.label = label;
        this.tooltip = tooltip;
    }

    public Component getLabel() {
        return Component.translatable(label);
    }

    @Nullable
    public Component getTooltip() {
        return tooltip == null ? null : Component.translatable(tooltip);
    }

    /** 原始说明文本（用于换行渲染） */
    @Nullable
    public String getTooltipText() {
        return tooltip;
    }

    /** 恢复该条目的配置默认值 */
    public abstract void resetToDefault();

    /** 布尔条目：开/关按钮 */
    public static class BooleanEntry extends ConfigEntry {
        private final BooleanValue value;

        public BooleanEntry(String label, @Nullable String tooltip, BooleanValue value) {
            super(label, tooltip);
            this.value = value;
        }

        public boolean get() {
            return Boolean.TRUE.equals(value.get());
        }

        public void set(boolean v) {
            value.set(v);
        }

        @Override
        public void resetToDefault() {
            value.resetToDefault();
        }
    }

    /** 双精度条目：滑条（min ~ max） */
    public static class DoubleEntry extends ConfigEntry {
        private final DoubleValue value;
        private final double min;
        private final double max;
        private final boolean percent;

        public DoubleEntry(String label, @Nullable String tooltip, DoubleValue value, double min, double max, boolean percent) {
            super(label, tooltip);
            this.value = value;
            this.min = min;
            this.max = max;
            this.percent = percent;
        }

        public double get() {
            Double v = value.get();
            return v == null ? min : v;
        }

        public void set(double v) {
            value.set(v);
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        public boolean isPercent() {
            return percent;
        }

        @Override
        public void resetToDefault() {
            Double d = value.getDefault();
            if (d != null) {
                value.set(d);
            }
        }
    }

    /** 整数条目：滑条 */
    public static class IntEntry extends ConfigEntry {
        private final IntValue value;
        private final int min;
        private final int max;

        public IntEntry(String label, @Nullable String tooltip, IntValue value, int min, int max) {
            super(label, tooltip);
            this.value = value;
            this.min = min;
            this.max = max;
        }

        public int get() {
            Integer v = value.get();
            return v == null ? min : v;
        }

        public void set(int v) {
            value.set(v);
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        @Override
        public void resetToDefault() {
            Integer i = value.getDefault();
            if (i != null) {
                value.set(i);
            }
        }
    }

    /** 字符串列表条目：逗号分隔的文本框 */
    public static class StringListEntry extends ConfigEntry {
        private final StringListValue value;

        public StringListEntry(String label, @Nullable String tooltip, StringListValue value) {
            super(label, tooltip);
            this.value = value;
        }

        public String toText() {
            List<String> list = value.get();
            return list == null ? "" : String.join(", ", list);
        }

        public void fromText(String text) {
            List<String> list = new ArrayList<>();
            for (String part : text.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    list.add(trimmed);
                }
            }
            value.set(list);
        }

        @Override
        public void resetToDefault() {
            List<String> def = value.getDefault();
            if (def != null) {
                value.set(new ArrayList<>(def));
            }
        }
    }
}
