package com.github.yzqdev.pethome.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * 自绘数值滑条控件。
 *
 * <p>26.1 的控件渲染入口是 {@link AbstractWidget#extractWidgetRenderState}，
 * 原版 {@code AbstractSliderButton} 的可用性未在本工程验证，因此这里改为自绘：
 * 背景轨道 + 进度填充 + 手柄 + 居中数值文本，拖动由 {@link PetHomeConfigScreen} 转发
 * {@code mouseDragged} / {@code mouseReleased} 事件，避免依赖不确定的控件内部实现。</p>
 */
public final class SliderWidget extends AbstractWidget {

    private static final int TRACK_COLOR = 0xFF202020;
    private static final int TRACK_BORDER = 0xFF6A6A6A;
    private static final int FILL_COLOR = 0xFF2E7D46;
    private static final int HANDLE_COLOR = 0xFFE8E8E8;
    private static final int TEXT_COLOR = 0xFFF0F0F0;

    private final DoubleSupplier getter;
    private final DoubleConsumer setter;
    private final double min;
    private final double max;
    private final boolean percent;
    private final boolean integer;

    private boolean dragging;

    public SliderWidget(int x, int y, int width, int height, DoubleSupplier getter, DoubleConsumer setter,
                        double min, double max, boolean percent, boolean integer) {
        super(x, y, width, height, Component.empty());
        this.getter = getter;
        this.setter = setter;
        this.min = min;
        this.max = max;
        this.percent = percent;
        this.integer = integer;
    }

    public double getNormalized() {
        if (max <= min) {
            return 0.0D;
        }
        return Mth.clamp((getter.getAsDouble() - min) / (max - min), 0.0D, 1.0D);
    }

    void beginDrag() {
        this.dragging = true;
    }

    void endDrag() {
        this.dragging = false;
    }

    boolean isDragging() {
        return dragging;
    }

    /** 依据鼠标 X 更新数值（限制在控件宽度范围内） */
    void updateFromMouse(double mouseX) {
        double ratio = Mth.clamp((mouseX - getX()) / (double) getWidth(), 0.0D, 1.0D);
        double raw = min + (max - min) * ratio;
        if (integer) {
            setter.accept(Math.round(raw));
        } else {
            setter.accept(Math.round(raw * 1000.0D) / 1000.0D);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (!this.active) {
            return;
        }
        beginDrag();
        updateFromMouse(event.x());
    }

    /** 数值显示文本：百分比 / 整数 / 两位小数 */
    public String displayText() {
        double value = getter.getAsDouble();
        if (percent) {
            return String.format(Locale.ROOT, "%.0f%%", value * 100.0D);
        }
        if (integer) {
            return String.valueOf((int) Math.round(value));
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        // 轨道
        graphics.fill(x, y + h / 2 - 3, x + w, y + h / 2 + 3, TRACK_COLOR);
        graphics.outline(x, y + h / 2 - 3, w, 6, TRACK_BORDER);

        // 进度填充
        int filled = (int) Math.round(w * getNormalized());
        if (filled > 0) {
            graphics.fill(x + 1, y + h / 2 - 2, x + Math.max(1, filled - 1), y + h / 2 + 2, FILL_COLOR);
        }

        // 手柄
        int handleX = x + Mth.clamp(filled, 2, w - 2);
        graphics.fill(handleX - 2, y + 3, handleX + 2, y + h - 3, HANDLE_COLOR);

        // 数值文本
        Font font = Minecraft.getInstance().font;
        int textColor = this.active ? TEXT_COLOR : 0xFF909090;
        graphics.centeredText(font, Component.literal(displayText()), x + w / 2, y + (h - 8) / 2, textColor);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        defaultButtonNarrationText(narration);
    }
}
