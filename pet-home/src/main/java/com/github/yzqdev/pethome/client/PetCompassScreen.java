package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.client.compass.CompassEntry;
import com.github.yzqdev.pethome.client.compass.CompassListRenderer;
import com.github.yzqdev.pethome.client.compass.CompassListState;
import com.github.yzqdev.pethome.client.compass.CompassPacket;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 手持罗盘右键打开的宠物列表 GUI（纯 UI 编排层）：布局/渲染交给 {@link CompassListRenderer}， 列表状态与几何在 {@link
 * CompassListState}，文案规则在 {@code CompassTexts}，网络收发在 {@link CompassPacket}。
 * 本类只做生命周期、输入事件分发与渲染编排；业务规则（按钮显隐配置、失效判定）由服务端权威下发，客户端不做判断。 数据由服务端在 C2S pet_compass_open 后打包 S2C
 * 下发，本类不向服务端查询任何东西。
 */
public class PetCompassScreen extends Screen {

  private final CompassListState state = new CompassListState();

  /** 传送/召回按钮显隐（服务端权威下发，handleData 更新） */
  private boolean cfgToPet;

  private boolean cfgToPlayer;

  /** 滚动条拖拽中（mouseDragged） */
  private boolean draggingScrollbar;

  private double dragOffset;

  public PetCompassScreen() {
    super(Component.translatable(LangDefinition.message("pet_compass.title")));
  }

  /** 关界面即丢弃待确认删除态：避免下次打开时残留「确认删除?」按钮 */
  @Override
  public void removed() {
    this.state.resetTransient();
    super.removed();
  }

  /**
   * S2C pet_compass_data 入口（PetCompassDataPacket.handle 分发，主线程）：GUI 已在场则原位刷新
   * （保留滚动与选中，召回后服务端补发的刷新包走这条路），否则解析数据后开新屏。
   */
  public static void handleData(@Nullable CompoundTag tag) {
    Minecraft minecraft = Minecraft.getInstance();
    if (tag == null || minecraft.player == null) {
      return;
    }
    if (minecraft.screen instanceof PetCompassScreen openScreen) {
      openScreen.applyData(tag);
    } else if (tag.getBoolean("NoOpen")) {
      // 服务端标注仅刷新（召回到位补发）：GUI 不在场时丢弃，不开屏
      return;
    } else {
      PetCompassScreen screen = new PetCompassScreen();
      screen.applyData(tag);
      minecraft.setScreen(screen);
    }
  }

  /** 解析包并替换条目列表；GUI 已在场时保留滚动与选中状态（clamp 由渲染帧兜底）。 */
  private void applyData(CompoundTag tag) {
    CompassPacket.Payload payload =
        CompassPacket.parse(
            tag, Minecraft.getInstance().player.level().dimension().location().toString());
    if (payload == null) {
      return;
    }
    this.cfgToPet = payload.cfgToPet();
    this.cfgToPlayer = payload.cfgToPlayer();
    this.state.clearEntries();
    for (CompassEntry entry : payload.entries()) {
      this.state.addEntry(entry);
    }
  }

  @Override
  public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.render(graphics, mouseX, mouseY, partialTick);

    // 行高随内容不同（死亡 2 行 / 在线 3~4 行），先累计行 y；面板高度随之伸缩（内容少时收缩贴列表）
    this.state.computeRowTops();
    CompassListRenderer.Layout layout =
        CompassListRenderer.layoutOf(this.width, this.height, this.state.totalContentHeight());
    if (!CompassListRenderer.renderPanel(graphics, this.font, this.title, this.state, layout)) {
      return;
    }

    // 像素级平滑滚动：滚轮/拖拽写入 scrollY，渲染前钳回 [0, 内容高-列表高]
    this.state.clampScroll(layout.trackHeight());
    int scrollY = this.state.scrollY();

    graphics.enableScissor(
        layout.panelX0() + 2, layout.listTop(), layout.panelX1() - 2, layout.listBottom());
    CompassListRenderer.renderRows(
        graphics,
        this.font,
        this.state,
        layout,
        scrollY,
        this.cfgToPet,
        this.cfgToPlayer,
        mouseX,
        mouseY);
    graphics.disableScissor();
    CompassListRenderer.renderScrollbar(graphics, this.state, layout, scrollY);
  }

  /** 左键：滚动条拖拽优先 → 行命中（选中 + 行内按钮发包）；几何全部来自渲染同款 Layout/State，保证所见即所点。 */
  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    this.state.computeRowTops();
    CompassListRenderer.Layout layout =
        CompassListRenderer.layoutOf(this.width, this.height, this.state.totalContentHeight());
    this.state.clampScroll(layout.trackHeight());
    int scrollY = this.state.scrollY();
    int trackHeight = layout.trackHeight();
    int scrollable = this.state.totalContentHeight() - trackHeight;

    // 滚动条滑块命中：进入拖拽（优先于行命中）
    if (scrollable > 0 && CompassListRenderer.isScrollbarArea(mouseX, mouseY, layout)) {
      this.draggingScrollbar = true;
      int thumbHeight =
          CompassListRenderer.thumbHeight(trackHeight, this.state.totalContentHeight());
      int thumbY =
          layout.listTop()
              + (int)
                  ((long) (trackHeight - thumbHeight) * Math.min(scrollY, scrollable) / scrollable);
      this.dragOffset =
          mouseY >= thumbY && mouseY <= thumbY + thumbHeight ? mouseY - thumbY : thumbHeight / 2d;
      return true;
    }

    if (button == 0
        && mouseX >= layout.panelX0()
        && mouseX <= layout.panelX1()
        && mouseY >= layout.listTop()
        && mouseY < layout.listBottom()
        && !this.state.isEmpty()) {
      // 变高行：按累计 y 找命中行（rowTops 是内容绝对 y，relY = 屏幕 y - 列表顶 + scrollY）
      int relY = (int) (mouseY - layout.listTop()) + scrollY;
      int entryIndex = this.state.hitTest(relY);
      if (entryIndex >= 0) {
        this.handleClickOnEntry(layout, entryIndex, scrollY, mouseX, mouseY);
        return true;
      }
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  /** 单行点击：选中（换选清待确认态）→ 失效行删除按钮（二段确认）→ 存活行传送/召回按钮。 */
  private void handleClickOnEntry(
      CompassListRenderer.Layout layout,
      int entryIndex,
      int scrollY,
      double mouseX,
      double mouseY) {
    CompassEntry entry = this.state.entries().get(entryIndex);
    this.state.select(entry.petId());
    int screenRowTop = layout.listTop() + this.state.rowTop(entryIndex) - scrollY;
    int buttonY =
        screenRowTop + (this.state.rowHeightOf(entry) - CompassListRenderer.BUTTON_HEIGHT) / 2;
    // 与渲染端 renderRows 的 listLeft 同一套偏移（LIST_MARGIN + ITEM_LEFT_MARGIN），保证按钮命中与绘制一致
    int buttonX =
        layout.listLeft()
            + CompassListState.ITEM_LEFT_MARGIN
            + layout.rowWidth()
            - CompassListRenderer.BUTTON_WIDTH;

    // 失效条目的删除按钮（占最右侧，与传送按钮同位）；第一次点进待确认态，再点才发包
    if (entry.stale()) {
      if (CompassListRenderer.isInButton(mouseX, mouseY, buttonX, buttonY)
          && !this.state.armForget(entry.petId())) {
        CompassPacket.sendForget(entry.petId());
      }
      return;
    }
    if (!entry.alive()) {
      return;
    }
    // 按钮从右往左排：toPet 最右，toPlayer 在其左侧
    if (this.cfgToPlayer
        && CompassListRenderer.isInButton(
            mouseX,
            mouseY,
            buttonX - CompassListRenderer.BUTTON_WIDTH - CompassListRenderer.BUTTON_GAP,
            buttonY)) {
      CompassPacket.sendAction(entry.petId(), "to_player");
      return;
    }
    if (this.cfgToPet && CompassListRenderer.isInButton(mouseX, mouseY, buttonX, buttonY)) {
      CompassPacket.sendAction(entry.petId(), "to_pet");
    }
  }

  /** 滚轮平滑滚动（每次 WHEEL_SCROLL_PX 像素；渲染时钳制到有效范围），滚动条滑块随之同步。 */
  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
    this.state.scrollBy(-(int) Math.signum(yAmount) * CompassListState.WHEEL_SCROLL_PX);
    return true;
  }

  /** 滑块拖拽：按 Y 位移换算 scrollY；未在拖拽中不处理。 */
  @Override
  public boolean mouseDragged(
      double mouseX, double mouseY, int button, double dragX, double dragY) {
    if (this.draggingScrollbar) {
      scrollToMouseY(mouseY);
      return true;
    }
    return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
  }

  /** 把滑块垂直位置换算为滚动像素偏移（滚动条拖拽共用）。 */
  private void scrollToMouseY(double mouseY) {
    this.state.computeRowTops();
    CompassListRenderer.Layout layout =
        CompassListRenderer.layoutOf(this.width, this.height, this.state.totalContentHeight());
    int trackHeight = layout.trackHeight();
    int scrollable = this.state.totalContentHeight() - trackHeight;
    if (scrollable <= 0) {
      return;
    }
    double rel =
        Mth.clamp(
            (mouseY - layout.listTop() - this.dragOffset)
                / (double)
                    (trackHeight
                        - CompassListRenderer.thumbHeight(
                            trackHeight, this.state.totalContentHeight())),
            0d,
            1d);
    this.state.clampScroll(trackHeight);
    this.state.scrollBy((int) Math.round(rel * scrollable) - this.state.scrollY());
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    this.draggingScrollbar = false;
    return super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
