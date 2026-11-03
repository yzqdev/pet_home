package com.github.yzqdev.pethome.client.gui;

import com.github.yzqdev.pethome.datagen.LangDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * 字符串列表编辑子界面（物品/维度 × 黑名单/白名单 四个列表共用），行布局对齐 NeoForge 原版 ConfigurationScreen 的 {@code
 * ConfigurationListScreen}：每行 = 上移 ⏶ | 下移 ⏷ | 删除 ❌ | 索引标签 "0:"（左列）+ 输入框（右列）；列表尾部 = "+" 添加行（同 NF 的
 * createAddElementButton，随内容滚动）；底部固定 = 完成按钮。原版底部还有 Undo/Reset，本模组条目单一（仅一个 List），与 drop-craft 主界面的每行
 * Reset 重复，故不搬。 修改写回 ModConfigSpec 内存；TOML 落盘由 PetHomeConfigScreen.onClose 的 saveSpecs 统一处理。
 */
public class ListEditorScreen extends Screen {
  private static final int ROW_HEIGHT = 24;
  private static final int BUTTON_SIZE = Button.DEFAULT_HEIGHT;

  /** NF ListLabelWidget 左列宽：Button.DEFAULT_WIDTH（150），容纳 上/下/删 三按钮 + "0:" 标签 */
  private static final int LABEL_WIDTH = Button.DEFAULT_WIDTH;

  /** NF 按钮步长：x、x+height+2、x+2*(height+2)，删除按钮锚右缘（x+width-height） */
  private static final int BUTTON_STEP = BUTTON_SIZE + 2;

  /** NF ListLabelWidget：标签文本 x + 2 * 22 */
  private static final int LABEL_TEXT_X = 2 * BUTTON_STEP;

  private static final int COLUMN_GAP = 8;
  private static final int TOP = 50;
  private static final int BOTTOM_MARGIN = 56;

  private final Screen parent;
  private final Component editorTitle;
  private final Supplier<List<String>> getter;
  private final Consumer<List<String>> setter;

  private final List<String> working = new ArrayList<>();

  /** 滚动内容：每行 4 控件（上/下/删/输入框）+ 末尾一个 "+" 添加按钮（NF 把它做成列表行） */
  private final List<AbstractWidget> scrollWidgets = new ArrayList<>();

  private Button addButton;
  private int scrollOffset = 0;
  private int contentHeight = 0;

  public ListEditorScreen(
      Screen parent,
      Component editorTitle,
      Supplier<List<String>> getter,
      Consumer<List<String>> setter) {
    super(editorTitle);
    this.parent = parent;
    this.editorTitle = editorTitle;
    this.getter = getter;
    this.setter = setter;
    this.working.addAll(getter.get());
  }

  private void commit() {
    setter.accept(new ArrayList<>(working));
  }

  private int getLeft() {
    return Math.max(20, this.width / 2 - 205);
  }

  private int getContentWidth() {
    return this.width - getLeft() * 2;
  }

  @Override
  protected void init() {
    scrollWidgets.clear();
    for (int i = 0; i < working.size(); i++) {
      final int index = i;
      // NF 原版：MOVE_LIST_ELEMENT_UP/DOWN/REMOVE（⏶ ⏷ ❌）+ ListLabelWidget（"0:" 索引标签）
      Button moveUp = Button.builder(Component.literal("⏶"), b -> move(index, -1)).build();
      Button moveDown = Button.builder(Component.literal("⏷"), b -> move(index, 1)).build();
      Button remove = Button.builder(Component.literal("❌"), b -> remove(index)).build();
      EditBox box = new EditBox(this.font, 0, 0, LABEL_WIDTH, BUTTON_SIZE, Component.empty());
      box.setMaxLength(128);
      box.setValue(working.get(index));
      // 同 NF：编辑只动工作副本，关闭界面时统一写回 spec
      box.setResponder(s -> working.set(index, s.trim()));
      for (AbstractWidget w : List.of(moveUp, moveDown, remove, box)) {
        this.addWidget(w);
        scrollWidgets.add(w);
      }
    }

    // NF createAddElementButton：空标签 + "+" 按钮组成一行挂在列表末尾，随内容滚动
    addButton =
        Button.builder(
                Component.translatable(LangDefinition.GUI_LIST_ADD),
                b -> {
                  working.add("");
                  commit();
                  rebuildWidgets();
                })
            .build();
    this.addWidget(addButton);

    // + 行 + 完成按钮之外的内容高度（含 add 行）
    contentHeight = (working.size() + 1) * ROW_HEIGHT;

    this.addRenderableWidget(
        Button.builder(CommonComponents.GUI_DONE, b -> onClose())
            .bounds(this.width / 2 - LABEL_WIDTH / 2, this.height - 28, LABEL_WIDTH, BUTTON_SIZE)
            .build());
    repositionRows();
  }

  /** 上移/下移（越界无操作）：NF 的 swap——交换后整列重建 */
  private void move(int index, int delta) {
    int target = index + delta;
    if (target < 0 || target >= working.size()) {
      return;
    }
    working.add(target, working.remove(index));
    commit();
    rebuildWidgets();
  }

  private void remove(int index) {
    working.remove(index);
    commit();
    rebuildWidgets();
  }

  @Override
  public void extractRenderState(
      GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    // 与 DropCraftConfigScreen 同理：super 先行（背景 + 一次模糊 + 完成按钮），自绘内容后置
    super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    graphics.centeredText(this.font, this.editorTitle, this.width / 2, 14, 0xFFFFFFFF);

    int left = getLeft();
    int contentWidth = getContentWidth();
    graphics.enableScissor(
        left - 4, TOP - 4, left + contentWidth + 4, this.height - BOTTOM_MARGIN + 4);

    int y = TOP - scrollOffset;
    int rowIndex = 0;
    for (AbstractWidget w : scrollWidgets) {
      if (rowIndex % 4 == 0) {
        // NF ListLabelWidget 的 "0:" 索引标签（第 0 列起点绘制）
        graphics.text(
            this.font,
            Component.translatable(LangDefinition.GUI_LIST_ELEMENT_INDEX, rowIndex / 4),
            left + LABEL_TEXT_X,
            y + (ROW_HEIGHT - this.font.lineHeight) / 2,
            0xFFE0E0E0);
      }
      w.extractRenderState(graphics, mouseX, mouseY, partialTick);
      if (rowIndex % 4 == 3) {
        y += ROW_HEIGHT;
      }
      rowIndex++;
    }
    addButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
    graphics.disableScissor();
  }

  /**
   * 行布局（对齐 NF）：左列 LABEL_WIDTH 内 上(x)、下(x+22)、删(右缘-20) 三按钮；右列输入框从 左列+间距 起、撑到内容区右缘（drop-craft 界面比 NF
   * 宽，输入框拉宽便于编辑 id 字符串）。
   */
  private void repositionRows() {
    int left = getLeft();
    int contentWidth = getContentWidth();
    int boxX = left + LABEL_WIDTH + COLUMN_GAP;
    int boxWidth = contentWidth - LABEL_WIDTH - COLUMN_GAP;
    int y = TOP - scrollOffset;
    int rowIndex = 0;
    for (AbstractWidget w : scrollWidgets) {
      int by = y + (ROW_HEIGHT - BUTTON_SIZE) / 2;
      switch (rowIndex % 4) {
        case 0 -> {
          w.setX(left);
          w.setWidth(BUTTON_SIZE);
        }
        case 1 -> {
          w.setX(left + BUTTON_STEP);
          w.setWidth(BUTTON_SIZE);
        }
        case 2 -> {
          w.setX(left + LABEL_WIDTH - BUTTON_SIZE);
          w.setWidth(BUTTON_SIZE);
        }
        default -> {
          w.setX(boxX);
          w.setWidth(boxWidth);
        }
      }
      w.setY(by);
      if (rowIndex % 4 == 3) {
        y += ROW_HEIGHT;
      }
      rowIndex++;
    }
    // add 行：NF 里位于列表末尾右列；drop-craft 的 add 按钮文字较宽（"添加"/"Add"），占整行
    addButton.setX(left);
    addButton.setY(y + (ROW_HEIGHT - BUTTON_SIZE) / 2);
    addButton.setWidth(LABEL_WIDTH);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
    int viewport = this.height - TOP - BOTTOM_MARGIN;
    int maxScroll = Math.max(0, contentHeight - viewport);
    if (maxScroll > 0) {
      scrollOffset = Mth.clamp(scrollOffset - (int) (yDelta * ROW_HEIGHT), 0, maxScroll);
      repositionRows();
    }
    return true;
  }

  @Override
  public void onClose() {
    // 丢弃还没填内容的空行；写回内存（落盘由 DropCraftConfigScreen.saveSpecs 统一做）
    working.removeIf(String::isEmpty);
    commit();
    this.minecraft.setScreen(parent);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
