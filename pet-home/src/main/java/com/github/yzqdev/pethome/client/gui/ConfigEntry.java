package com.github.yzqdev.pethome.client.gui;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

/**
 * 配置界面的单个条目：标签 + 读写访问器。 修改经 {@code ModConfigSpec.ConfigValue#set} 写入内存；落盘由 PetHomeConfigScreen
 * 关闭时统一 {@code save()}。 悬停说明走多语言（标签翻译键 + ".tooltip"，见 PetHomeConfigScreen.extractTooltip）。
 */
public abstract class ConfigEntry {
  /** 每行高度 */
  public static final int ROW_HEIGHT = 24;

  private final Component label;

  protected ConfigEntry(Component label) {
    this.label = label;
  }

  public Component getLabel() {
    return label;
  }

  /**
   * @return 标签的翻译键（label 为 translatable 时）；literal 标签返回 null。tooltip 键 = 此键 + ".tooltip"
   */
  public @org.jspecify.annotations.Nullable String getTranslationKey() {
    if (label.getContents() instanceof TranslatableContents contents) {
      return contents.getKey();
    }
    return null;
  }

  /** 恢复该条目的配置默认值 */
  public abstract void resetToDefault();

  /** 静态配置字段的读写句柄（由 ModConfigSpecScreen 的工厂方法用 lambda 组装） */
  public interface FieldAccess<T> {
    T get();

    void set(T value);

    T getDefault();
  }

  /** 布尔条目：开/关按钮 */
  public static class BooleanEntry extends ConfigEntry {
    private final FieldAccess<Boolean> value;

    public BooleanEntry(Component label, FieldAccess<Boolean> value) {
      super(label);
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
      set(value.getDefault());
    }
  }

  /** 整数条目：滑条（min ~ max） */
  public static class IntEntry extends ConfigEntry {
    private final FieldAccess<Integer> value;
    private final int min;
    private final int max;

    public IntEntry(Component label, FieldAccess<Integer> value, int min, int max) {
      super(label);
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
        set(i);
      }
    }
  }

  /** 整数文本条目：输入框（适合范围跨度大、滑条无意义的值，如 maxSize）。 输入合法且落在 [min, max] 时即时写入，非法输入被忽略。 */
  public static class IntTextEntry extends ConfigEntry {
    private final FieldAccess<Integer> value;
    private final int min;
    private final int max;

    public IntTextEntry(Component label, FieldAccess<Integer> value, int min, int max) {
      super(label);
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

    /**
     * @return 输入是否被接受
     */
    public boolean trySet(String text) {
      try {
        int parsed = Integer.parseInt(text.trim());
        if (parsed < min || parsed > max) {
          return false;
        }
        set(parsed);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }

    @Override
    public void resetToDefault() {
      Integer i = value.getDefault();
      if (i != null) {
        set(i);
      }
    }
  }

  /** 小数条目：滑条（min ~ max） */
  public static class DoubleEntry extends ConfigEntry {
    private final FieldAccess<Double> value;
    private final double min;
    private final double max;

    public DoubleEntry(Component label, FieldAccess<Double> value, double min, double max) {
      super(label);
      this.value = value;
      this.min = min;
      this.max = max;
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

    @Override
    public void resetToDefault() {
      Double d = value.getDefault();
      if (d != null) {
        set(d);
      }
    }
  }

  /** 枚举条目：循环切换按钮（点击切换到下一个枚举值） */
  public static class EnumEntry<T extends Enum<T>> extends ConfigEntry {
    private final FieldAccess<T> value;
    private final T[] values;
    private final String valueKeyPrefix;

    public EnumEntry(Component label, FieldAccess<T> value, T[] values, String valueKeyPrefix) {
      super(label);
      this.value = value;
      this.values = values;
      this.valueKeyPrefix = valueKeyPrefix;
    }

    public T get() {
      T v = value.get();
      return v == null ? values[0] : v;
    }

    public Component getDisplay() {
      return Component.translatable(valueKeyPrefix + get().name().toLowerCase(Locale.ROOT));
    }

    /**
     * @return 切换后的新值
     */
    public T cycle() {
      T next = values[(get().ordinal() + 1) % values.length];
      value.set(next);
      return next;
    }

    @Override
    public void resetToDefault() {
      T def = value.getDefault();
      if (def != null) {
        value.set(def);
      }
    }
  }

  /** 子界面入口条目：按钮显示摘要（如列表条目数），点击打开编辑子界面；Reset 执行传入的恢复默认动作 */
  public static class ScreenEntry extends ConfigEntry {
    private final Supplier<String> summary;
    private final Function<Screen, Screen> screenFactory;
    private final Runnable resetAction;

    public ScreenEntry(
        Component label,
        Supplier<String> summary,
        Function<Screen, Screen> screenFactory,
        Runnable resetAction) {
      super(label);
      this.summary = summary;
      this.screenFactory = screenFactory;
      this.resetAction = resetAction;
    }

    public String getSummary() {
      return summary.get();
    }

    /**
     * @return 以当前界面为父界面的编辑子界面
     */
    public Screen open(Screen current) {
      return screenFactory.apply(current);
    }

    @Override
    public void resetToDefault() {
      resetAction.run();
    }
  }
}
