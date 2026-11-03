package com.github.yzqdev.pethome.client.gui;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Pet Home 设置界面（Tab 单屏风格，与 1.20 侧自研界面同构，组件移植自 drop-craft）：顶部 Tab 切换分类（友好保护 / 通用 / 可驯化 / 战利品）；每行 =
 * 标签 | 值控件 | 独立重置按钮。打开方式：模组列表"配置"按钮（IConfigScreenFactory）。 数据源为 {@link PetHomeConfig} 的 {@link
 * ModConfigSpec}（LOCAL）；1.21 的 {@code ConfigValue.set()} 只写内存， 关闭界面时经 {@link #onClose()} 统一 {@code
 * save()} 落盘 TOML。
 */
public class PetHomeConfigScreen extends Screen {
  private record Category(Component name, List<ConfigEntry> entries) {}

  private static final int ROW_HEIGHT = ConfigEntry.ROW_HEIGHT;
  private static final int VALUE_WIDTH = 150;

  /** 整数条目滑条可用跨度上限：跨度 ≥ 此值改用数字输入框（滑条 150px 宽，每像素档位太多无法精确调） */
  private static final int SLIDER_MAX_SPAN = 200;

  private static final int RESET_WIDTH = 50;
  private static final int TAB_Y = 42;
  private static final int TAB_HEIGHT = 20;

  /** 悬停说明翻译键后缀：条目翻译键 + 此后缀（en/zh lang 由 datagen 生成） */
  static final String TOOLTIP_KEY_SUFFIX = ".tooltip";

  /** 说明折行宽度（vanilla tooltip 上限） */
  private static final int TOOLTIP_MAX_WIDTH = 170;

  /** 内容区上边界 = Tab 行下缘：Tab 只占左侧一段宽度，若裁剪线放在 Tab 上缘， 右侧无 Tab 遮挡的行控件会在 Tab 带里裸露出来 */
  private static final int CONTENT_TOP = TAB_Y + TAB_HEIGHT;

  private static final int TOP = 64;
  private static final int BOTTOM_MARGIN = 40;

  private final Screen parent;
  private final List<Category> categories = new ArrayList<>();

  /** 当前行控件：只加入 children（参与点击/键盘），渲染由 extract 在剪刀裁剪内手动做， 避免滚动时行控件盖在顶部 Tab 与底部完成按钮上 */
  private final List<AbstractWidget> rowWidgets = new ArrayList<>();

  private int selectedTab = 0;
  private int scrollOffset = 0;
  private int contentHeight = 0;

  public PetHomeConfigScreen(Screen parent) {
    super(Component.translatable(LangDefinition.GUI_TITLE));
    this.parent = parent;
    buildCategories();
  }

  private void buildCategories() {
    // ==================== 友好保护 ====================
    List<ConfigEntry> friendly = new ArrayList<>();
    friendly.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_F_PROTECT_OWNER),
            () -> PetHomeConfig.protectPetsFromOwner,
            v -> PetHomeConfig.specProtectOwner().set(v),
            () -> PetHomeConfig.specProtectOwner().getDefault()));
    friendly.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_F_PROTECT_PETS),
            () -> PetHomeConfig.protectPetsFromPets,
            v -> PetHomeConfig.specProtectPets().set(v),
            () -> PetHomeConfig.specProtectPets().getDefault()));
    friendly.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_F_PROTECT_CHILDREN),
            () -> PetHomeConfig.protectChildren,
            v -> PetHomeConfig.specProtectChildren().set(v),
            () -> PetHomeConfig.specProtectChildren().getDefault()));
    friendly.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_F_REFLECT_DAMAGE),
            () -> PetHomeConfig.reflectDamage,
            v -> PetHomeConfig.specReflectDamage().set(v),
            () -> PetHomeConfig.specReflectDamage().getDefault()));
    friendly.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_F_DISPLAY_WARNING),
            () -> PetHomeConfig.displayHitWarning,
            v -> PetHomeConfig.specDisplayWarning().set(v),
            () -> PetHomeConfig.specDisplayWarning().getDefault()));
    friendly.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_F_PROTECT_TEAM),
            () -> PetHomeConfig.protectTeamMembers,
            v -> PetHomeConfig.specProtectTeam().set(v),
            () -> PetHomeConfig.specProtectTeam().getDefault()));
    friendly.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_F_RESPECT_TEAM),
            () -> PetHomeConfig.respectTeamRules,
            v -> PetHomeConfig.specRespectTeam().set(v),
            () -> PetHomeConfig.specRespectTeam().getDefault()));
    friendly.add(
        listEntry(
            Component.translatable(LangDefinition.GUI_F_CAN_HURT_PET_ITEMS),
            () -> List.copyOf(PetHomeConfig.specCanHurtPetItems().get()),
            v -> PetHomeConfig.specCanHurtPetItems().set(v),
            () -> PetHomeConfig.specCanHurtPetItems().getDefault(),
            LangDefinition.GUI_F_CAN_HURT_PET_ITEMS));
    friendly.add(
        listEntry(
            Component.translatable(LangDefinition.GUI_F_CAN_HURT_ALL_ITEMS),
            () -> List.copyOf(PetHomeConfig.specCanHurtAllItems().get()),
            v -> PetHomeConfig.specCanHurtAllItems().set(v),
            () -> PetHomeConfig.specCanHurtAllItems().getDefault(),
            LangDefinition.GUI_F_CAN_HURT_ALL_ITEMS));
    friendly.add(
        listEntry(
            Component.translatable(LangDefinition.GUI_F_NO_PROTECTION),
            () -> List.copyOf(PetHomeConfig.specNoProtection().get()),
            v -> PetHomeConfig.specNoProtection().set(v),
            () -> PetHomeConfig.specNoProtection().getDefault(),
            LangDefinition.GUI_F_NO_PROTECTION));
    friendly.add(
        listEntry(
            Component.translatable(LangDefinition.GUI_F_EXTRA_PROTECTED),
            () -> List.copyOf(PetHomeConfig.specExtraProtected().get()),
            v -> PetHomeConfig.specExtraProtected().set(v),
            () -> PetHomeConfig.specExtraProtected().getDefault(),
            LangDefinition.GUI_F_EXTRA_PROTECTED));
    friendly.add(
        listEntry(
            Component.translatable(LangDefinition.GUI_F_PLAYER_PROTECTED),
            () -> List.copyOf(PetHomeConfig.specPlayerProtected().get()),
            v -> PetHomeConfig.specPlayerProtected().set(v),
            () -> PetHomeConfig.specPlayerProtected().getDefault(),
            LangDefinition.GUI_F_PLAYER_PROTECTED));
    categories.add(
        new Category(Component.translatable(LangDefinition.GUI_CAT_FRIENDLY_NAME), friendly));

    // ==================== 通用 ====================
    List<ConfigEntry> general = new ArrayList<>();
    general.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_G_ROTTEN_APPLE),
            () -> PetHomeConfig.rottenApple,
            v -> PetHomeConfig.specRottenApple().set(v),
            () -> PetHomeConfig.specRottenApple().getDefault()));
    general.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_G_PET_BED_RESPAWN),
            () -> PetHomeConfig.petBedRespawns,
            v -> PetHomeConfig.specPetBedRespawns().set(v),
            () -> PetHomeConfig.specPetBedRespawns().getDefault()));
    general.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_G_RABBITS_RAVAGERS),
            () -> PetHomeConfig.rabbitsScareRavagers,
            v -> PetHomeConfig.specRabbitsRavagers().set(v),
            () -> PetHomeConfig.specRabbitsRavagers().getDefault()));
    general.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_G_TRINARY_COMMAND),
            () -> PetHomeConfig.trinaryCommandSystem,
            v -> PetHomeConfig.specTrinaryCommand().set(v),
            () -> PetHomeConfig.specTrinaryCommand().getDefault()));
    general.add(
        intEntry(
            Component.translatable(LangDefinition.GUI_G_PETSTORE_WEIGHT),
            () -> PetHomeConfig.petstoreVillageWeight,
            v -> PetHomeConfig.specPetstoreWeight().set(v),
            () -> PetHomeConfig.specPetstoreWeight().getDefault(),
            0,
            1000));
    general.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_G_MOB_TAMABLE_ONLY),
            () -> PetHomeConfig.mobcatcherOnlyTamableAnimal,
            v -> PetHomeConfig.specMobcatcherOnlyTamable().set(v),
            () -> PetHomeConfig.specMobcatcherOnlyTamable().getDefault()));
    general.add(
        listEntry(
            Component.translatable(LangDefinition.GUI_G_MOB_BLACKLIST),
            () -> List.copyOf(PetHomeConfig.specMobcatcherBlacklist().get()),
            v -> PetHomeConfig.specMobcatcherBlacklist().set(v),
            () -> PetHomeConfig.specMobcatcherBlacklist().getDefault(),
            LangDefinition.GUI_G_MOB_BLACKLIST));
    // 宠物罗盘：总开关 + 两个独立传送（与 1.20 侧条目一一对应）
    general.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_G_PET_COMPASS_ENABLE),
            () -> PetHomeConfig.petCompassEnable,
            v -> PetHomeConfig.specPetCompassEnable().set(v),
            () -> PetHomeConfig.specPetCompassEnable().getDefault()));
    general.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_G_TELEPORT_PLAYER_TO_PET),
            () -> PetHomeConfig.petCompassTeleportPlayerToPet,
            v -> PetHomeConfig.specTeleportPlayerToPet().set(v),
            () -> PetHomeConfig.specTeleportPlayerToPet().getDefault()));
    general.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_G_RECALL_PET_TO_PLAYER),
            () -> PetHomeConfig.petCompassTeleportPetToPlayer,
            v -> PetHomeConfig.specRecallPetToPlayer().set(v),
            () -> PetHomeConfig.specRecallPetToPlayer().getDefault()));
    // 宠物信息悬浮面板
    general.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_G_PET_INFO_OVERLAY),
            () -> PetHomeConfig.petInfoOverlay,
            v -> PetHomeConfig.specPetInfoOverlay().set(v),
            () -> PetHomeConfig.specPetInfoOverlay().getDefault()));
    general.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT),
            () -> PetHomeConfig.petInfoOverlayRequireShift,
            v -> PetHomeConfig.specPetInfoOverlayRequireShift().set(v),
            () -> PetHomeConfig.specPetInfoOverlayRequireShift().getDefault()));
    general.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_G_PET_INFO_OVERLAY_IGNORE_JADE),
            () -> PetHomeConfig.petInfoOverlayIgnoreJade,
            v -> PetHomeConfig.specPetInfoOverlayIgnoreJade().set(v),
            () -> PetHomeConfig.specPetInfoOverlayIgnoreJade().getDefault()));
    // 调试日志总开关（PHModLogger.debugLog 门控）
    general.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_G_DEBUG_MODE),
            () -> PetHomeConfig.debugMode,
            v -> PetHomeConfig.specDebugMode().set(v),
            () -> PetHomeConfig.specDebugMode().getDefault()));
    categories.add(
        new Category(Component.translatable(LangDefinition.GUI_CAT_GENERAL_NAME), general));

    // ==================== 可驯化 ====================
    List<ConfigEntry> tameable = new ArrayList<>();
    tameable.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_T_AXOLOTL),
            () -> PetHomeConfig.tameableAxolotl,
            v -> PetHomeConfig.specTameableAxolotl().set(v),
            () -> PetHomeConfig.specTameableAxolotl().getDefault()));
    tameable.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_T_FOX),
            () -> PetHomeConfig.tameableFox,
            v -> PetHomeConfig.specTameableFox().set(v),
            () -> PetHomeConfig.specTameableFox().getDefault()));
    tameable.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_T_FROG),
            () -> PetHomeConfig.tameableFrog,
            v -> PetHomeConfig.specTameableFrog().set(v),
            () -> PetHomeConfig.specTameableFrog().getDefault()));
    tameable.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_T_HORSE),
            () -> PetHomeConfig.tameableHorse,
            v -> PetHomeConfig.specTameableHorse().set(v),
            () -> PetHomeConfig.specTameableHorse().getDefault()));
    tameable.add(
        boolEntry(
            Component.translatable(LangDefinition.GUI_T_RABBIT),
            () -> PetHomeConfig.tameableRabbit,
            v -> PetHomeConfig.specTameableRabbit().set(v),
            () -> PetHomeConfig.specTameableRabbit().getDefault()));
    categories.add(
        new Category(Component.translatable(LangDefinition.GUI_CAT_TAMEABLE_NAME), tameable));

    // ==================== 战利品 ====================
    // 标签复用既有 *_conf 翻译键（datagen 已维护中英文文案，与内置界面共用一套）
    List<ConfigEntry> loot = new ArrayList<>();
    loot.add(
        doubleEntry(
            Component.translatable(LangDefinition.sinister_carrot_loot_chance_conf),
            () -> PetHomeConfig.sinisterCarrotLootChance,
            v -> PetHomeConfig.specSinisterCarrot().set(v),
            () -> PetHomeConfig.specSinisterCarrot().getDefault(),
            0d,
            1d));
    loot.add(
        doubleEntry(
            Component.translatable(LangDefinition.bubbling_loot_chance_conf),
            () -> PetHomeConfig.bubblingLootChance,
            v -> PetHomeConfig.specBubbling().set(v),
            () -> PetHomeConfig.specBubbling().getDefault(),
            0d,
            1d));
    loot.add(
        doubleEntry(
            Component.translatable(LangDefinition.vampirism_loot_chance_conf),
            () -> PetHomeConfig.vampirismLootChance,
            v -> PetHomeConfig.specVampirism().set(v),
            () -> PetHomeConfig.specVampirism().getDefault(),
            0d,
            1d));
    loot.add(
        doubleEntry(
            Component.translatable(LangDefinition.sonic_boom_loot_chance_conf),
            () -> PetHomeConfig.sonicBoomLootChance,
            v -> PetHomeConfig.specSonicBoom().set(v),
            () -> PetHomeConfig.specSonicBoom().getDefault(),
            0d,
            1d));
    loot.add(
        doubleEntry(
            Component.translatable(LangDefinition.paralysis_loot_chance_conf),
            () -> PetHomeConfig.paralysisLootChance,
            v -> PetHomeConfig.specParalysis().set(v),
            () -> PetHomeConfig.specParalysis().getDefault(),
            0d,
            1d));
    loot.add(
        doubleEntry(
            Component.translatable(LangDefinition.tough_loot_chance_conf),
            () -> PetHomeConfig.toughLootChance,
            v -> PetHomeConfig.specTough().set(v),
            () -> PetHomeConfig.specTough().getDefault(),
            0d,
            1d));
    loot.add(
        doubleEntry(
            Component.translatable(LangDefinition.share_loot_chance_conf),
            () -> PetHomeConfig.shareLootChance,
            v -> PetHomeConfig.specShare().set(v),
            () -> PetHomeConfig.specShare().getDefault(),
            0d,
            1d));
    loot.add(
        doubleEntry(
            Component.translatable(LangDefinition.ore_scenting_loot_chance_conf),
            () -> PetHomeConfig.oreScentingLootChance,
            v -> PetHomeConfig.specOreScenting().set(v),
            () -> PetHomeConfig.specOreScenting().getDefault(),
            0d,
            1d));
    loot.add(
        doubleEntry(
            Component.translatable(LangDefinition.blazing_protection_loot_chance_conf),
            () -> PetHomeConfig.blazingProtectionLootChance,
            v -> PetHomeConfig.specBlazingProtection().set(v),
            () -> PetHomeConfig.specBlazingProtection().getDefault(),
            0d,
            1d));
    loot.add(
        doubleEntry(
            Component.translatable(LangDefinition.muffled_loot_chance_conf),
            () -> PetHomeConfig.muffledLootChance,
            v -> PetHomeConfig.specMuffled().set(v),
            () -> PetHomeConfig.specMuffled().getDefault(),
            0d,
            1d));
    categories.add(new Category(Component.translatable(LangDefinition.GUI_CAT_LOOT_NAME), loot));
  }

  private static ConfigEntry.BooleanEntry boolEntry(
      Component label, Supplier<Boolean> get, Consumer<Boolean> set, Supplier<Boolean> def) {
    return new ConfigEntry.BooleanEntry(label, specAccess(get, set, def));
  }

  /** 整数条目工厂：跨度 < 200 用滑条（精度可接受），跨度 ≥ 200 用数字输入框（滑条每像素档位太多无法精确调）。 */
  private static ConfigEntry intEntry(
      Component label,
      Supplier<Integer> get,
      Consumer<Integer> set,
      Supplier<Integer> def,
      int min,
      int max) {
    var access = specAccess(get, set, def);
    if ((long) max - min >= SLIDER_MAX_SPAN) {
      return new ConfigEntry.IntTextEntry(label, access, min, max);
    }
    return new ConfigEntry.IntEntry(label, access, min, max);
  }

  private static ConfigEntry.DoubleEntry doubleEntry(
      Component label,
      Supplier<Double> get,
      Consumer<Double> set,
      Supplier<Double> def,
      double min,
      double max) {
    return new ConfigEntry.DoubleEntry(label, specAccess(get, set, def), min, max);
  }

  /** 列表编辑入口：按钮显示当前条目数，点击进入 ListEditorScreen；Reset 恢复默认（空列表） */
  private static ConfigEntry.ScreenEntry listEntry(
      Component label,
      Supplier<List<String>> get,
      Consumer<List<? extends String>> set,
      Supplier<List<? extends String>> def,
      String editorTitleKey) {
    return new ConfigEntry.ScreenEntry(
        label,
        () -> String.valueOf(get.get().size()),
        current ->
            new ListEditorScreen(
                current, Component.translatable(editorTitleKey), get, list -> set.accept(list)),
        () -> set.accept(new ArrayList<>()));
  }

  private static <T> ConfigEntry.FieldAccess<T> specAccess(
      Supplier<T> get, Consumer<T> set, Supplier<T> def) {
    return new ConfigEntry.FieldAccess<>() {
      @Override
      public T get() {
        return get.get();
      }

      @Override
      public void set(T value) {
        set.accept(value);
      }

      @Override
      public T getDefault() {
        return def.get();
      }
    };
  }

  private Category currentCategory() {
    return categories.get(Mth.clamp(selectedTab, 0, categories.size() - 1));
  }

  @Override
  protected void init() {
    int left = getLeft();
    int contentWidth = getContentWidth();

    // 顶部 Tab：宽度自适应文本的紧凑样式，当前选中的置灰不可点（按下状态样式）
    int tabX = left;
    for (int i = 0; i < categories.size(); i++) {
      Category category = categories.get(i);
      final int index = i;
      boolean selected = i == selectedTab;
      int tabWidth = this.font.width(category.name()) + 8;
      Button tab =
          Button.builder(
                  category.name(),
                  b -> {
                    if (selectedTab != index) {
                      selectedTab = index;
                      scrollOffset = 0;
                      rebuildWidgets();
                    }
                  })
              .bounds(tabX, TAB_Y, tabWidth, TAB_HEIGHT)
              .build();
      tab.active = !selected;
      if (selected) {
        tab.setMessage(category.name().copy().withStyle(ChatFormatting.YELLOW));
      }
      this.addRenderableWidget(tab);
      tabX += tabWidth + 4;
    }

    // 当前行控件：值控件 + 每行独立重置按钮（只进 children，渲染走剪刀裁剪）
    rowWidgets.clear();
    int valueX = left + contentWidth - VALUE_WIDTH - RESET_WIDTH - 10;
    int resetX = left + contentWidth - RESET_WIDTH;
    int y = TOP - scrollOffset;
    for (ConfigEntry entry : currentCategory().entries()) {
      AbstractWidget valueWidget = createValueWidget(entry, valueX, y);
      this.addWidget(valueWidget);
      rowWidgets.add(valueWidget);
      AbstractWidget resetButton =
          Button.builder(
                  Component.translatable(LangDefinition.GUI_RESET),
                  b -> {
                    entry.resetToDefault();
                    rebuildWidgets();
                  })
              .bounds(resetX, y, RESET_WIDTH, 20)
              .build();
      this.addWidget(resetButton);
      rowWidgets.add(resetButton);
      y += ROW_HEIGHT;
    }
    this.contentHeight = currentCategory().entries().size() * ROW_HEIGHT;

    // 底部完成按钮
    this.addRenderableWidget(
        Button.builder(CommonComponents.GUI_DONE, b -> onClose())
            .bounds(this.width / 2 - 100, this.height - 28, 200, 20)
            .build());
  }

  /** 为单个条目创建值控件（布尔=开关按钮、整数/小数=滑条、整数文本=输入框、枚举=循环按钮） */
  private AbstractWidget createValueWidget(ConfigEntry entry, int x, int y) {
    if (entry instanceof ConfigEntry.BooleanEntry boolEntry) {
      return Button.builder(
              booleanText(boolEntry.get()),
              b -> {
                boolean newValue = !boolEntry.get();
                boolEntry.set(newValue);
                b.setMessage(booleanText(newValue));
              })
          .bounds(x, y, VALUE_WIDTH, 20)
          .build();
    }
    if (entry instanceof ConfigEntry.IntEntry intEntry) {
      return new AbstractSliderButton(
          x,
          y,
          VALUE_WIDTH,
          20,
          Component.empty(),
          toSlider(intEntry.get(), intEntry.getMin(), intEntry.getMax())) {
        {
          updateMessage();
        }

        @Override
        protected void updateMessage() {
          setMessage(Component.literal(String.valueOf(intEntry.get())));
        }

        @Override
        protected void applyValue() {
          intEntry.set(
              (int)
                  Math.round(
                      intEntry.getMin() + (intEntry.getMax() - intEntry.getMin()) * this.value));
        }
      };
    }
    if (entry instanceof ConfigEntry.DoubleEntry doubleEntry) {
      return new AbstractSliderButton(
          x,
          y,
          VALUE_WIDTH,
          20,
          Component.empty(),
          toSlider(doubleEntry.get(), doubleEntry.getMin(), doubleEntry.getMax())) {
        {
          updateMessage();
        }

        @Override
        protected void updateMessage() {
          setMessage(Component.literal(String.format("%.2f", doubleEntry.get())));
        }

        @Override
        protected void applyValue() {
          doubleEntry.set(
              doubleEntry.getMin() + (doubleEntry.getMax() - doubleEntry.getMin()) * this.value);
        }
      };
    }
    if (entry instanceof ConfigEntry.IntTextEntry intTextEntry) {
      EditBox box =
          new EditBox(
              this.font,
              x,
              y,
              VALUE_WIDTH,
              20,
              Component.literal(intTextEntry.getLabel().getString()));
      box.setMaxLength(12);
      box.setValue(String.valueOf(intTextEntry.get()));
      // 非法输入由 IntTextEntry.trySet 校验拒绝（不用 NeoForge 的 EditBox.setFilter，保持与 1.20 侧同构）
      box.setResponder(
          s -> {
            if (!s.isEmpty()) {
              intTextEntry.trySet(s);
            }
          });
      return box;
    }
    if (entry instanceof ConfigEntry.ScreenEntry screenEntry) {
      return Button.builder(
              Component.literal(screenEntry.getSummary()),
              b -> this.minecraft.setScreen(screenEntry.open(this)))
          .bounds(x, y, VALUE_WIDTH, 20)
          .build();
    }
    throw new IllegalStateException("unknown entry: " + entry.getClass());
  }

  private static Component booleanText(boolean v) {
    return v
        ? Component.translatable(LangDefinition.GUI_ON).withStyle(ChatFormatting.GREEN)
        : Component.translatable(LangDefinition.GUI_OFF).withStyle(ChatFormatting.RED);
  }

  private static double toSlider(double v, double min, double max) {
    return Mth.clamp((v - min) / (max - min), 0.0, 1.0);
  }

  private int getLeft() {
    return Math.max(20, this.width / 2 - 205);
  }

  private int getContentWidth() {
    return this.width - getLeft() * 2;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    // 基类 render 先行（背景 + Tab/完成按钮），自绘内容全部后置；行标签用带 alpha 的颜色
    super.render(graphics, mouseX, mouseY, partialTick);
    graphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);

    int left = getLeft();
    int contentWidth = getContentWidth();

    // 行控件与标签裁剪在内容区内渲染：滚动越界时从 Tab 下缘被整齐裁掉
    graphics.enableScissor(
        left - 4, CONTENT_TOP, left + contentWidth + 4, this.height - BOTTOM_MARGIN + 4);
    int y = TOP - scrollOffset;
    int hoveredIndex = -1;
    for (int i = 0; i < currentCategory().entries().size(); i++) {
      ConfigEntry entry = currentCategory().entries().get(i);
      graphics.drawString(this.font, entry.getLabel(), left, y + 6, 0xFFE0E0E0);
      // 命中检测：行矩形 = 标签区左缘到重置按钮右缘 × ROW_HEIGHT
      if (mouseX >= left
          && mouseX <= left + contentWidth
          && mouseY >= y
          && mouseY < y + ROW_HEIGHT) {
        hoveredIndex = i;
      }
      y += ROW_HEIGHT;
    }
    for (AbstractWidget row : rowWidgets) {
      row.render(graphics, mouseX, mouseY, partialTick);
    }
    graphics.disableScissor();

    // 悬停说明：标题 + .tooltip 翻译键折行；deferred tooltip 通道在帧末 scissor 外绘制
    if (hoveredIndex >= 0) {
      ConfigEntry entry = currentCategory().entries().get(hoveredIndex);
      this.renderEntryTooltip(graphics, entry, mouseX, mouseY);
    }
  }

  /**
   * 组装单个条目的悬停说明（Scissor 已解除，绘制不受内容区裁剪影响）。 说明文本走多语言：翻译键 = 条目标签键 + ".tooltip"（如
   * pet_home.configuration.gui.general.debug_mode.tooltip）；缺失则只显示标签。
   */
  private void renderEntryTooltip(
      GuiGraphics graphics, ConfigEntry entry, int mouseX, int mouseY) {
    List<FormattedCharSequence> lines = new ArrayList<>();
    lines.add(entry.getLabel().getVisualOrderText());
    String labelKey = entry.getTranslationKey();
    if (labelKey != null && Language.getInstance().has(labelKey + TOOLTIP_KEY_SUFFIX)) {
      lines.addAll(
          this.font.split(
              Component.translatable(labelKey + TOOLTIP_KEY_SUFFIX), TOOLTIP_MAX_WIDTH));
    }
    graphics.renderTooltip(this.font, lines, mouseX, mouseY);
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

  /** 滚动后仅调整行控件 X/Y（渲染裁剪由 extract 的剪刀区处理，无需隐藏控件） */
  private void repositionRows() {
    int left = getLeft();
    int contentWidth = getContentWidth();
    int valueX = left + contentWidth - VALUE_WIDTH - RESET_WIDTH - 10;
    int resetX = left + contentWidth - RESET_WIDTH;
    int y = TOP - scrollOffset;
    int rowIndex = 0;
    for (AbstractWidget widget : rowWidgets) {
      widget.setX(rowIndex % 2 == 0 ? valueX : resetX);
      widget.setY(y);
      if (rowIndex % 2 == 1) {
        y += ROW_HEIGHT;
      }
      rowIndex++;
    }
  }

  @Override
  public void onClose() {
    // 1.21 的 ConfigValue.set() 只写内存不落盘（javadoc: "without writing the config to disk"），
    // 关闭界面时统一 save() 写 TOML
    if (PetHomeConfig.SPEC.isLoaded()) {
      PetHomeConfig.SPEC.save();
    }
    this.minecraft.setScreen(parent);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
