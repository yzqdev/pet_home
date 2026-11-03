package com.github.yzqdev.pethome.client.compass;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

/**
 * 罗盘列表的状态与几何：条目列表、像素级滚动位置、选中/待确认删除、行布局（变高行的累计 y）。
 * 不依赖任何渲染类；几何方法被绘制（CompassListRenderer）与点击命中（PetCompassScreen）共用， 保证「看得到」与「点得到」是同一套坐标。滚动位置在渲染/命中前经
 * {@link #clampScroll} 钳制，避免条目变少后残留旧位置越界。
 */
public class CompassListState {

    /** 行内上下内边距：文字块距行框上下各 4px */
    public static final int ROW_PADDING = 4;

    /** 行内相邻文字行之间的内间距 */
    public static final int ROW_LINE_PADDING = 3;

    /** 列表两侧外边距（行宽 = 面板宽 - 2 * LIST_MARGIN，行高亮框再外扩 6px）；与标题分隔线起点（面板边内缩 6px）对齐 */
    public static final int LIST_MARGIN = 6;

    /** 每个 item 内文字相对 item 左缘的内缩（渲染起点 = listLeft + 它；Screen 命中计算需同步加） */
    public static final int ITEM_LEFT_MARGIN = 3;

    /** 滚轮一格滚动的像素数（平滑滚动，不按行对齐） */
    public static final int WHEEL_SCROLL_PX = 20;

    private final List<CompassEntry> entries = new ArrayList<>();

    /** 每个条目相对列表顶的 y（渲染前 computeRowTops 按 内容高度 累计写入）；行命中/滚动条换算共用 */
    private int[] rowTops = new int[0];

    /** 列表内容总高度（computeRowTops 计算）；面板高度、滚动条显示与进度共用 */
    private int totalContentHeight;

    /** 滚动像素偏移（0 .. 内容高-列表高；渲染/命中前经 clampScroll 钳制） */
    private int scrollY;

    /** 选中条目（PetId）；左键行选中，换选清待确认删除态 */
    @Nullable private UUID selected;

    /** 待确认删除的宠物（点一次「删除」进待确认态，再点一次才发包），换选/关界面即清空 */
    @Nullable private UUID pendingForget;

    public List<CompassEntry> entries() {
        return this.entries;
    }

    public void clearEntries() {
        this.entries.clear();
    }

    public void addEntry(CompassEntry entry) {
        this.entries.add(entry);
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public int scrollY() {
        return this.scrollY;
    }

    public void scrollBy(int deltaPx) {
        this.scrollY += deltaPx;
    }

    /** 渲染/命中前调用：把滚动像素偏移钳回 [0, 内容高-列表高] */
    public void clampScroll(int trackHeight) {
        int scrollable = Math.max(0, this.totalContentHeight - trackHeight);
        this.scrollY = Math.max(0, Math.min(this.scrollY, scrollable));
    }

    public int totalContentHeight() {
        return this.totalContentHeight;
    }

    public int rowTop(int entryIndex) {
        return this.rowTops[entryIndex];
    }

    @Nullable
    public UUID selected() {
        return this.selected;
    }

    /** 选中并处理换选清待确认态；返回本次点击是否换选（选中行刷新不丢待确认态） */
    public boolean select(UUID petId) {
        boolean changed = this.selected == null || !this.selected.equals(petId);
        this.selected = petId;
        if (changed) {
            this.pendingForget = null;
        }
        return changed;
    }

    public boolean isSelected(CompassEntry entry) {
        return entry.petId().equals(this.selected);
    }

    @Nullable
    public UUID pendingForget() {
        return this.pendingForget;
    }

    /** 进入/推进待确认删除：已在待确认态返回 false（调用方发包），否则进入并返回 true */
    public boolean armForget(UUID petId) {
        if (this.pendingForget != null && this.pendingForget.equals(petId)) {
            this.pendingForget = null;
            return false;
        }
        this.pendingForget = petId;
        return true;
    }

    /** 关界面即丢弃待确认删除态：避免下次打开时残留「确认删除?」按钮 */
    public void resetTransient() {
        this.pendingForget = null;
    }

    /** 单行高度：文字行数 × 行高 + 行间 + 上下 padding（死亡 2 行 / 在线 3~4 行，随内容变高） */
    public int rowHeightOf(CompassEntry entry) {
        int lineCount = entry.alive() ? (CompassTexts.hasDetailLine(entry) ? 4 : 3) : 2;
        return lineCount * 9 + (lineCount - 1) * ROW_LINE_PADDING + 2 * ROW_PADDING;
    }

    /** 累计每个条目相对列表顶的 y（紧凑排列），并算内容总高；渲染帧/命中计算前调用 */
    public void computeRowTops() {
        int n = this.entries.size();
        if (this.rowTops.length != n) {
            this.rowTops = new int[n];
        }
        int y = 0;
        for (int i = 0; i < n; i++) {
            this.rowTops[i] = y;
            y += this.rowHeightOf(this.entries.get(i));
        }
        this.totalContentHeight = y;
    }

    /** 按内容 y 找命中条目索引（rowTops 是内容绝对 y；relY = 屏幕 y - 列表顶 + scrollY）；未命中返回 -1 */
    public int hitTest(int relY) {
        for (int i = 0; i < this.entries.size(); i++) {
            int rowTop = this.rowTops[i];
            if (relY >= rowTop && relY < rowTop + this.rowHeightOf(this.entries.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /** 视口顶滚到内容 y=scrollY 时第一个（部分）可见行的索引（行底 > scrollY 的最小 i）；完全滚出时返回 size */
    public int firstVisibleIndex(int scrollY) {
        for (int i = 0; i < this.entries.size(); i++) {
            if (this.rowTops[i] + this.rowHeightOf(this.entries.get(i)) > scrollY) {
                return i;
            }
        }
        return this.entries.size();
    }
}
