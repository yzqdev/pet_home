package com.github.yzqdev.pethome.client.compass;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * 罗盘列表的绘制：面板/标题/空态、逐行内容（变高行、状态行、详情行）、行内按钮（常规/危险两种）与滚动条。
 * 只读 state 做绘制，不修改任何状态；几何公式全部来自 {@link CompassListState}，与点击命中天然一致。
 */
public final class CompassListRenderer {

    public static final int BUTTON_WIDTH = 64;
    public static final int BUTTON_HEIGHT = 16;

    /** 行按钮水平间距（toPet 最右、toPlayer 左移一格的步长） */
    public static final int BUTTON_GAP = 6;

    /** 行高亮框相对行框向左右外扩的外边距（悬停判定与选中/悬停高亮绘制共用） */
    public static final int ROW_HOVER_MARGIN = 6;

    /** 面板半宽（面板总宽 = 2 * 半宽 + 边框） */
    public static final int PANEL_HALF_WIDTH = 214;

    /** 标题区高度（面板顶到列表第一行）：标题 + 紧贴的分隔线 + 少量间距 */
    public static final int TITLE_AREA = 26;

    /** 面板内列表底部留白 */
    public static final int PANEL_BOTTOM_MARGIN = 6;

    /** 面板顶距屏幕上边固定间距（也是条目多到放不下时面板的最大伸展起点） */
    public static final int SCREEN_TOP_MARGIN = 30;

    /** 面板底距屏幕底的最小间距 */
    public static final int SCREEN_BOTTOM_MARGIN = 30;

    /** 面板最小高度（标题 + 一行四行文字的行）：GUI 缩放拉满、屏幕很矮时兜底，避免面板被压成一条 */
    public static final int PANEL_MIN_HEIGHT = TITLE_AREA
            + 4 * 9
            + 3 * CompassListState.ROW_LINE_PADDING
            + 2 * CompassListState.ROW_PADDING
            + PANEL_BOTTOM_MARGIN;

    /** 面板底色 */
    private static final int PANEL_BG = 0xF0101018;

    /** 面板边框/分隔线 */
    private static final int PANEL_EDGE = 0xFF55556A;

    /** 选中行高亮 */
    private static final int ROW_SELECTED = 0x66395C8F;

    /** 悬停行高亮 */
    private static final int ROW_HOVER = 0x24FFFFFF;

    /** 滚动条轨道/滑块 */
    private static final int SCROLL_TRACK = 0x33000000;

    private static final int SCROLL_THUMB = 0xFF7A8FB5;

    /** 滚动条宽（贴面板右缘，留出与内容的间距） */
    private static final int SCROLL_BAR_WIDTH = 6;

    /** 滚动条距面板右缘的留白（与行内容隔开） */
    private static final int SCROLL_BAR_MARGIN = 2;

    /** 滑块命中区比绘制区加宽的余量 */
    private static final int SCROLL_HIT_PAD = 8;

    /** 行内容右侧为滚动条预留的外边距（行宽右侧扣掉它，保证按钮/文字与滚动条之间有间距） */
    private static final int ROW_SCROLL_GAP = 8;

    /** 滑块最小高度 */
    private static final int THUMB_MIN_HEIGHT = 12;

    private CompassListRenderer() {
    }

    /**
     * 面板整体布局：高度固定为最大伸展（屏幕顶距 30 到屏幕底距 30 之间），宠物少时内容靠上、底部留空，宠物多时走滚动；
     * 渲染与点击命中共用同一份几何。
     *
     * @param contentHeight 列表内容总高（调用前先 {@code state.computeRowTops()}）；保留参数以稳定调用点签名，布局不再依赖它
     */
    public static Layout layoutOf(int screenWidth, int screenHeight, int contentHeight) {
        int panelX0 = screenWidth / 2 - PANEL_HALF_WIDTH;
        int panelX1 = screenWidth / 2 + PANEL_HALF_WIDTH;
        int panelTop = SCREEN_TOP_MARGIN;
        // 面板高度固定为最大伸展：屏幕高 - 顶距 30 - 底距 30；屏幕过矮时兜底不小于 PANEL_MIN_HEIGHT
        int panelHeight = Math.max(screenHeight - SCREEN_TOP_MARGIN - SCREEN_BOTTOM_MARGIN, PANEL_MIN_HEIGHT);
        int panelBottom = Math.min(panelTop + panelHeight, screenHeight - 12);
        int listTop = panelTop + TITLE_AREA;
        int listBottom = panelBottom - PANEL_BOTTOM_MARGIN;
        return new Layout(panelX0, panelX1, panelTop, panelBottom, listTop, listBottom);
    }

    /** 面板矩形（布局公式唯一来源：渲染帧与点击命中都从 {@link #layoutOf} 取） */
    public record Layout(int panelX0, int panelX1, int panelTop, int panelBottom, int listTop, int listBottom) {
        public int rowWidth() {
            // 右侧扣 ROW_SCROLL_GAP：滚动条（含命中加宽区）与行内按钮/文字保持间距
            return this.panelX1 - this.panelX0 - 2 * CompassListState.LIST_MARGIN - ROW_SCROLL_GAP;
        }

        public int listLeft() {
            return this.panelX0 + CompassListState.LIST_MARGIN;
        }

        public int trackHeight() {
            return this.listBottom - this.listTop;
        }
    }

    /** 面板底 + 边框 + 标题 + 分隔线 + （空态时）居中提示。返回 false 表示列表为空、调用方可直接结束本帧。 */
    public static boolean renderPanel(
            GuiGraphicsExtractor graphics,
            Font font,
            Component title,
            CompassListState state,
            Layout layout) {
        graphics.fill(layout.panelX0(), layout.panelTop(), layout.panelX1(), layout.panelBottom(), PANEL_BG);
        graphics.outline(
                layout.panelX0(),
                layout.panelTop(),
                layout.panelX1() - layout.panelX0(),
                layout.panelBottom() - layout.panelTop(),
                PANEL_EDGE);
        graphics.centeredText(
                font, title, (layout.panelX0() + layout.panelX1()) / 2, layout.panelTop() + 5, 0xFFFFFFFF);
        graphics.horizontalLine(
                layout.panelX0() + 6, layout.panelX1() - 6, layout.panelTop() + 16, PANEL_EDGE);

        if (state.isEmpty()) {
            graphics.centeredText(
                    font,
                    CompassTexts.msg("empty").withStyle(ChatFormatting.GRAY),
                    (layout.panelX0() + layout.panelX1()) / 2,
                    (layout.panelTop() + layout.panelBottom()) / 2 - 8,
                    0xFFFFFFFF);
            return false;
        }
        return true;
    }

    /**
     * scissor 裁剪内按像素偏移绘制可见行（调用方负责 enable/disableScissor）：从「视口顶所在的行」画到列表底，
     * 行 y 减 scrollY 得屏幕位置（平滑滚动，行不必与视口顶对齐）。
     */
    public static void renderRows(
            GuiGraphicsExtractor graphics,
            Font font,
            CompassListState state,
            Layout layout,
            int scrollY,
            boolean cfgToPet,
            boolean cfgToPlayer,
            int mouseX,
            int mouseY) {
        int rowWidth = layout.rowWidth();
        int listLeft = layout.listLeft() + CompassListState.ITEM_LEFT_MARGIN;
        int listBottom = layout.listBottom();
        for (int i = state.firstVisibleIndex(scrollY); i < state.entries().size(); i++) {
            int rowScreenTop = layout.listTop() + state.rowTop(i) - scrollY;
            if (rowScreenTop >= listBottom) {
                break;
            }
            renderEntry(
                    graphics,
                    font,
                    state,
                    state.entries().get(i),
                    listLeft,
                    rowScreenTop,
                    rowWidth,
                    cfgToPet,
                    cfgToPlayer,
                    mouseX,
                    mouseY);
        }
    }

    /** 单行渲染：名字+类型、存活态显示位置/距离/维度状态（未加载显示"最后已知位置"），死亡只显示待复活；选中行右侧按配置画"传送/召回"按钮。 */
    private static void renderEntry(
            GuiGraphicsExtractor graphics,
            Font font,
            CompassListState state,
            CompassEntry entry,
            int left,
            int top,
            int rowWidth,
            boolean cfgToPet,
            boolean cfgToPlayer,
            int mouseX,
            int mouseY) {
        int rowHeight = state.rowHeightOf(entry);
        boolean hovered = mouseX >= left - ROW_HOVER_MARGIN
                && mouseX <= left + rowWidth + ROW_HOVER_MARGIN
                && mouseY >= top
                && mouseY < top + rowHeight;
        boolean isSelected = state.isSelected(entry);
        if (isSelected) {
            graphics.fill(left - ROW_HOVER_MARGIN, top, left + rowWidth + ROW_HOVER_MARGIN, top + rowHeight, ROW_SELECTED);
        } else if (hovered) {
            graphics.fill(left - ROW_HOVER_MARGIN, top, left + rowWidth + ROW_HOVER_MARGIN, top + rowHeight, ROW_HOVER);
        }

        // 文字块上下各留 ROW_PADDING，行距 ROW_LINE_PADDING（与 rowHeightOf 同一套公式，上下正好对齐）
        int lineCount = entry.alive() ? (CompassTexts.hasDetailLine(entry) ? 4 : 3) : 2;
        int contentHeight = lineCount * font.lineHeight + (lineCount - 1) * CompassListState.ROW_LINE_PADDING;
        int textY = top + (rowHeight - contentHeight) / 2;

        graphics.text(
                font,
                Component.literal(entry.name()).withStyle(ChatFormatting.GOLD),
                left,
                textY,
                0xFFFFFFFF);
        graphics.text(
                font,
                Component.literal(entry.type()).withStyle(ChatFormatting.DARK_GRAY),
                left + font.width(entry.name()) + 8,
                textY,
                0xFFFFFFFF);

        int line2Y = textY + font.lineHeight + CompassListState.ROW_LINE_PADDING;
        int line3Y = line2Y + font.lineHeight + CompassListState.ROW_LINE_PADDING;
        int line4Y = line3Y + font.lineHeight + CompassListState.ROW_LINE_PADDING;

        if (!entry.alive()) {
            graphics.text(
                    font,
                    CompassTexts.msg("status_dead").withStyle(ChatFormatting.RED),
                    left,
                    line2Y,
                    0xFFFFFFFF);
        } else {
            String posLabel = entry.loaded()
                    ? CompassTexts.msg("position").getString()
                    : CompassTexts.msg("last_known_pos").getString();
            String posText = posLabel + entry.x() + ", " + entry.y() + ", " + entry.z();
            graphics.text(
                    font,
                    Component.literal(posText).withStyle(ChatFormatting.GRAY),
                    left,
                    line2Y,
                    0xFFFFFFFF);
            graphics.text(
                    font,
                    CompassTexts.msg("distance", entry.dist()).withStyle(ChatFormatting.AQUA),
                    left + font.width(posText) + 8,
                    line2Y,
                    0xFFFFFFFF);

            // 失效条目用红色单独显示，与「区块未加载暂时够不着」（黄色）区分开（文案选择规则见 CompassTexts.statusOf）
            graphics.text(
                    font,
                    CompassTexts.msg("dim_label")
                            .append(CompassTexts.dimName(entry.dim()))
                            .withStyle(ChatFormatting.GRAY)
                            .append("  ")
                            .append(CompassTexts.statusOf(entry)),
                    left,
                    line3Y,
                    0xFFFFFFFF);
            // 实时详情行（仅在线宠物有包内数据）：血量 + 指令 + 附魔摘要
            if (entry.health() >= 0) {
                Component detail = CompassTexts.buildDetailLine(entry);
                if (detail != null) {
                    graphics.text(font, detail, left, line4Y, 0xFFFFFFFF);
                }
            }
        }

        // 失效条目（实体确实已不在）显示删除按钮：未确认时显示「删除」，点击进入待确认态显示「确认删除」，
        // 再点一次才发包。存活宠物的传送/召回按钮不受影响。
        if (isSelected && entry.stale()) {
            int buttonY = top + (rowHeight - BUTTON_HEIGHT) / 2;
            int buttonX = left + rowWidth - BUTTON_WIDTH;
            if (state.pendingForget() != null && state.pendingForget().equals(entry.petId())) {
                renderDangerButton(graphics, font, "button_confirm_forget", buttonX, buttonY, mouseX, mouseY);
            } else {
                renderDangerButton(graphics, font, "button_forget", buttonX, buttonY, mouseX, mouseY);
            }
            return;
        }

        if (isSelected && entry.alive()) {
            int buttonY = top + (rowHeight - BUTTON_HEIGHT) / 2;
            renderRowButtons(graphics, font, cfgToPet, cfgToPlayer, rowWidth, left, buttonY, mouseX, mouseY);
        }
    }

    /** 选中存活行按配置绘制传送/召回按钮（cfg 布尔由 Screen 从服务端下发的数据带入）；渲染与点击共用同一几何（见 Screen 命中分支）。 */
    public static void renderRowButtons(
            GuiGraphicsExtractor graphics,
            Font font,
            boolean cfgToPet,
            boolean cfgToPlayer,
            int rowWidth,
            int left,
            int buttonY,
            int mouseX,
            int mouseY) {
        int buttonX = left + rowWidth - BUTTON_WIDTH;
        if (cfgToPet) {
            renderButton(graphics, font, "button_tp", buttonX, buttonY, mouseX, mouseY);
            buttonX -= BUTTON_WIDTH + BUTTON_GAP;
        }
        if (cfgToPlayer) {
            renderButton(graphics, font, "button_recall", buttonX, buttonY, mouseX, mouseY);
        }
    }

    /** 行内「传送/召回」按钮的绘制（蓝色系）；命中判定用 {@link #isInButton}。 */
    private static void renderButton(
            GuiGraphicsExtractor graphics, Font font, String key, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isInButton(mouseX, mouseY, x, y);
        graphics.fill(x, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, hovered ? 0xFF44598A : 0xFF2E3542);
        graphics.outline(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, hovered ? 0xFFA9BEE8 : 0xFF7A8FB5);
        graphics.centeredText(font, CompassTexts.msg(key), x + BUTTON_WIDTH / 2, y + 4, 0xFFFFFFFF);
    }

    /** 危险操作按钮（删除档案）：红色描边，与常规按钮区分。 */
    private static void renderDangerButton(
            GuiGraphicsExtractor graphics, Font font, String key, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isInButton(mouseX, mouseY, x, y);
        graphics.fill(x, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, hovered ? 0xFF6A2E2E : 0xFF42262A);
        graphics.outline(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, hovered ? 0xFFE8A9A9 : 0xFFB57A7A);
        graphics.centeredText(font, CompassTexts.msg(key), x + BUTTON_WIDTH / 2, y + 4, 0xFFFFFFFF);
    }

    /** 按钮命中判定：渲染与点击共用同一套矩形，避免「看得到点不到」 */
    public static boolean isInButton(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + BUTTON_WIDTH && mouseY >= y && mouseY <= y + BUTTON_HEIGHT;
    }

    /** 滚动条：内容超一屏时在面板右缘绘制轨道与滑块（滑块位置按像素滚动偏移占比）。 */
    public static void renderScrollbar(
            GuiGraphicsExtractor graphics, CompassListState state, Layout layout, int scrollY) {
        int trackHeight = layout.trackHeight();
        int scrollable = state.totalContentHeight() - trackHeight;
        if (scrollable <= 0) {
            return;
        }
        int barX = layout.panelX1() - SCROLL_BAR_WIDTH - SCROLL_BAR_MARGIN;
        graphics.fill(barX, layout.listTop(), barX + SCROLL_BAR_WIDTH, layout.listBottom(), SCROLL_TRACK);
        // 滑块：高度按内容/可视比例，最小 12px；位置按滚动像素占比
        int thumbHeight = thumbHeight(trackHeight, state.totalContentHeight());
        int thumbY = layout.listTop()
                + (int) ((long) (trackHeight - thumbHeight) * Math.min(scrollY, scrollable) / scrollable);
        graphics.fill(barX, thumbY, barX + SCROLL_BAR_WIDTH, thumbY + thumbHeight, SCROLL_THUMB);
    }

    /** 滑块高度（渲染与拖拽换算共用一套公式） */
    public static int thumbHeight(int trackHeight, int totalContentHeight) {
        return Math.max(THUMB_MIN_HEIGHT, (int) ((long) trackHeight * trackHeight / totalContentHeight));
    }

    /** 滚动条命中区（x 覆盖滑块绘制区 + 命中加宽；调用方需先确认内容可滚动） */
    public static boolean isScrollbarArea(double mouseX, double mouseY, Layout layout) {
        return mouseX >= layout.panelX1() - SCROLL_BAR_WIDTH - SCROLL_BAR_MARGIN - SCROLL_HIT_PAD
                && mouseX <= layout.panelX1() - SCROLL_BAR_MARGIN
                && mouseY >= layout.listTop()
                && mouseY < layout.listBottom();
    }
}
