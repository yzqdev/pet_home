package com.github.yzqdev.pethome.client.gui;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.config.BooleanValue;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Environment(EnvType.CLIENT)
public class PetHomeConfigScreen extends Screen {
    private record Category(String nameKey, List<ConfigEntry> entries) {
    }

    private static final int ROW_HEIGHT = 24;
    private static final int VALUE_WIDTH = 150;
    private static final int RESET_WIDTH = 64;
    private static final int TAB_Y = 42;
    private static final int TAB_HEIGHT = 20;
    /** 内容区上边界 = Tab 行下缘：Tab 只占左侧一段宽度，若裁剪线放在 Tab 上缘，
     *  右侧无 Tab 遮挡的行控件会在 Tab 带里裸露出来 */
    private static final int CONTENT_TOP = TAB_Y + TAB_HEIGHT;
    private static final int TOP = 64;
    private static final int BOTTOM_MARGIN = 40;

    private final Screen parent;
    private final List<Category> categories = new ArrayList<>();
    /** 当前行控件：只加入 children（参与点击/键盘），渲染由 render() 在剪刀裁剪内手动做，
     *  避免滚动时行控件盖在顶部 Tab 与底部完成按钮上 */
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
        List<ConfigEntry> friendly = new ArrayList<>();
        friendly.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_F_PROTECT_OWNER, LangDefinition.GUI_F_PROTECT_OWNER_DESC, PetHomeConfig.PROTECT_PETS_FROM_OWNER));
        friendly.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_F_PROTECT_PETS, LangDefinition.GUI_F_PROTECT_PETS_DESC, PetHomeConfig.PROTECT_PETS_FROM_PETS));
        friendly.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_F_PROTECT_CHILDREN, LangDefinition.GUI_F_PROTECT_CHILDREN_DESC, PetHomeConfig.PROTECT_CHILDREN));
        friendly.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_F_REFLECT_DAMAGE, LangDefinition.GUI_F_REFLECT_DAMAGE_DESC, PetHomeConfig.REFLECT_DAMAGE));
        friendly.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_F_DISPLAY_WARNING, LangDefinition.GUI_F_DISPLAY_WARNING_DESC, PetHomeConfig.DISPLAY_HIT_WARNING));
        friendly.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_F_PROTECT_TEAM, LangDefinition.GUI_F_PROTECT_TEAM_DESC, PetHomeConfig.PROTECT_TEAM_MEMBERS));
        friendly.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_F_RESPECT_TEAM, LangDefinition.GUI_F_RESPECT_TEAM_DESC, PetHomeConfig.RESPECT_TEAM_RULES));
        friendly.add(new ConfigEntry.StringListEntry(LangDefinition.GUI_F_CAN_HURT_PET_ITEMS, LangDefinition.GUI_F_CAN_HURT_PET_ITEMS_DESC, PetHomeConfig.CAN_HURT_PET_ITEM));
        friendly.add(new ConfigEntry.StringListEntry(LangDefinition.GUI_F_CAN_HURT_ALL_ITEMS, LangDefinition.GUI_F_CAN_HURT_ALL_ITEMS_DESC, PetHomeConfig.CAN_HURT_ALL_ITEM));
        friendly.add(new ConfigEntry.StringListEntry(LangDefinition.GUI_F_NO_PROTECTION, LangDefinition.GUI_F_NO_PROTECTION_DESC, PetHomeConfig.NO_PROTECTION_ENTITY));
        friendly.add(new ConfigEntry.StringListEntry(LangDefinition.GUI_F_EXTRA_PROTECTED, LangDefinition.GUI_F_EXTRA_PROTECTED_DESC, PetHomeConfig.OTHER_SHOULD_PROTECT_ENTITY));
        friendly.add(new ConfigEntry.StringListEntry(LangDefinition.GUI_F_PLAYER_PROTECTED, LangDefinition.GUI_F_PLAYER_PROTECTED_DESC, PetHomeConfig.PLAYER_CANT_HURT_ENTITY));
        categories.add(new Category(LangDefinition.GUI_CAT_FRIENDLY_NAME, friendly));

        List<ConfigEntry> general = new ArrayList<>();
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_ROTTEN_APPLE, LangDefinition.GUI_G_ROTTEN_APPLE_DESC, PetHomeMod.CONFIG.rottenApple));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_PET_BED_RESPAWN, LangDefinition.GUI_G_PET_BED_RESPAWN_DESC, PetHomeMod.CONFIG.petBedRespawns));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_RABBITS_RAVAGERS, LangDefinition.GUI_G_RABBITS_RAVAGERS_DESC, PetHomeMod.CONFIG.rabbitsScareRavagers));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_TRINARY_COMMAND, LangDefinition.GUI_G_TRINARY_COMMAND_DESC, PetHomeMod.CONFIG.trinaryCommandSystem));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_PET_INFO_OVERLAY, LangDefinition.GUI_G_PET_INFO_OVERLAY_DESC, PetHomeMod.CONFIG.petInfoOverlay));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT, LangDefinition.GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT_DESC, PetHomeMod.CONFIG.petInfoOverlayRequireShift));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_PET_INFO_OVERLAY_IGNORE_JADE, LangDefinition.GUI_G_PET_INFO_OVERLAY_IGNORE_JADE_DESC, PetHomeMod.CONFIG.petInfoOverlayIgnoreJade));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_ANIMAL_TAMER, LangDefinition.GUI_G_ANIMAL_TAMER_DESC, PetHomeMod.CONFIG.animalTamerVillager));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_PET_COMPASS, LangDefinition.GUI_G_PET_COMPASS_DESC, PetHomeMod.CONFIG.petCompassEnable));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_PET_COMPASS_TP_P, LangDefinition.GUI_G_PET_COMPASS_TP_P_DESC, PetHomeMod.CONFIG.petCompassTeleportPlayerToPet));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_PET_COMPASS_TP_R, LangDefinition.GUI_G_PET_COMPASS_TP_R_DESC, PetHomeMod.CONFIG.petCompassTeleportPetToPlayer));
        general.add(new ConfigEntry.IntEntry(LangDefinition.GUI_G_PETSTORE_WEIGHT, LangDefinition.GUI_G_PETSTORE_WEIGHT_DESC, PetHomeMod.CONFIG.petstoreVillageWeight, 0, 100));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_MOB_TAMABLE_ONLY, LangDefinition.GUI_G_MOB_TAMABLE_ONLY_DESC, PetHomeConfig.MOBCATCHER_ONLY_TAMABLE_ANIMAL));
        general.add(new ConfigEntry.StringListEntry(LangDefinition.GUI_G_MOB_BLACKLIST, LangDefinition.GUI_G_MOB_BLACKLIST_DESC, PetHomeConfig.MOBCATCHER_BLACKLIST));
        categories.add(new Category(LangDefinition.GUI_CAT_GENERAL_NAME, general));

        List<ConfigEntry> tameable = new ArrayList<>();
        tameable.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_T_AXOLOTL, LangDefinition.GUI_T_AXOLOTL_DESC, PetHomeMod.CONFIG.tameableAxolotl));
        tameable.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_T_FOX, LangDefinition.GUI_T_FOX_DESC, PetHomeMod.CONFIG.tameableFox));
        tameable.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_T_FROG, LangDefinition.GUI_T_FROG_DESC, PetHomeMod.CONFIG.tameableFrog));
        tameable.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_T_HORSE, LangDefinition.GUI_T_HORSE_DESC, PetHomeMod.CONFIG.tameableHorse));
        tameable.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_T_RABBIT, LangDefinition.GUI_T_RABBIT_DESC, PetHomeMod.CONFIG.tameableRabbit));
        categories.add(new Category(LangDefinition.GUI_CAT_TAMEABLE_NAME, tameable));

        List<ConfigEntry> loot = new ArrayList<>();
        loot.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_L_CURSE_LOOT_ONLY, LangDefinition.GUI_L_CURSE_LOOT_ONLY_DESC, PetHomeMod.CONFIG.petCurseEnchantmentsLootOnly));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.ITEM_SINISTER_CARROT, LangDefinition.GUI_L_SINISTER_CARROT_DESC, PetHomeMod.CONFIG.sinisterCarrotLootChance, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.ENCHANT_BUBBLING, LangDefinition.GUI_L_BUBBLING_DESC, PetHomeMod.CONFIG.bubblingLootChance, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.ENCHANT_VAMPIRE, LangDefinition.GUI_L_VAMPIRISM_DESC, PetHomeMod.CONFIG.vampirismLootChance, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.ENCHANT_VOID_CLOUD, LangDefinition.GUI_L_VOID_CLOUD_DESC, PetHomeMod.CONFIG.voidCloudLootChance, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.ENCHANT_ORE_SCENTING, LangDefinition.GUI_L_ORE_SCENTING_DESC, PetHomeMod.CONFIG.oreScentingLootChance, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.ENCHANT_MUFFLED, LangDefinition.GUI_L_MUFFLED_DESC, PetHomeMod.CONFIG.muffledLootChance, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.ENCHANT_BLAZING_PROTECTION, LangDefinition.GUI_L_BLAZING_DESC, PetHomeMod.CONFIG.blazingProtectionLootChance, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.ENCHANT_SHARE, LangDefinition.GUI_L_SHARE_DESC, PetHomeMod.CONFIG.shareLootChance, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.ENCHANT_SONIC_BOOM, LangDefinition.GUI_L_SONIC_BOOM_DESC, PetHomeMod.CONFIG.sonicBoomLootChance, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.ENCHANT_PARALYSIS, LangDefinition.GUI_L_PARALYSIS_DESC, PetHomeMod.CONFIG.paralysisLootChance, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.ENCHANT_TOUGH, LangDefinition.GUI_L_TOUGH_DESC, PetHomeMod.CONFIG.toughLootChance, 0, 1, true));
        categories.add(new Category(LangDefinition.GUI_CAT_LOOT_NAME, loot));

        List<ConfigEntry> enchants = new ArrayList<>();
        for (Map.Entry<String, BooleanValue> e : PetHomeMod.CONFIG.enabledEnchantments.entrySet()) {
            String registryName = e.getKey();
            // 标签/说明复用附魔本身的翻译键：enchantment.pet_home.<name>（与 .desc）
            String labelKey = "enchantment.pet_home." + registryName;
            String descKey = labelKey + ".desc";
            enchants.add(new ConfigEntry.BooleanEntry(labelKey, descKey, e.getValue()));
        }
        categories.add(new Category(LangDefinition.GUI_CAT_ENCHANTS_NAME, enchants));
    }

    private Category currentCategory() {
        return categories.get(Mth.clamp(selectedTab, 0, categories.size() - 1));
    }

    @Override
    protected void init() {
        int left = getLeft();
        int contentWidth = getContentWidth();

        // 顶部 Tab：宽度自适应文本的紧凑样式（对齐 BoccHUD），当前选中的置灰不可点（按下状态样式）
        int tabX = left;
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            final int index = i;
            boolean selected = i == selectedTab;
            Component tabName = Component.translatable(category.nameKey());
            int tabWidth = this.font.width(tabName) + 8;
            Button tab = Button.builder(tabName, b -> {
                if (selectedTab != index) {
                    selectedTab = index;
                    scrollOffset = 0;
                    rebuildWidgets();
                }
            }).bounds(tabX, TAB_Y, tabWidth, TAB_HEIGHT).build();
            tab.active = !selected;
            if (selected) {
                tab.setMessage(Component.translatable(category.nameKey()).withStyle(style -> style.withColor(0xFFFF55)));
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
            AbstractWidget resetButton = Button.builder(Component.translatable(LangDefinition.GUI_RESET), b -> {
                entry.resetToDefault();
                rebuildWidgets();
            }).bounds(resetX, y, RESET_WIDTH, 20).build();
            this.addWidget(resetButton);
            rowWidgets.add(resetButton);
            y += ROW_HEIGHT;
        }
        this.contentHeight = currentCategory().entries().size() * ROW_HEIGHT;

        // 底部完成按钮
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    private AbstractWidget createBooleanWidget(java.util.function.Supplier<Boolean> getter, java.util.function.Consumer<Boolean> setter, int x, int y) {
        return Button.builder(booleanText(getter.get()), b -> {
            boolean newValue = !getter.get();
            setter.accept(newValue);
            b.setMessage(booleanText(newValue));
        }).bounds(x, y, VALUE_WIDTH, 20).build();
    }

    /** 为单个条目创建值控件（布尔=开关按钮、数值=滑条、列表=文本框） */
    private AbstractWidget createValueWidget(ConfigEntry entry, int x, int y) {
        if (entry instanceof ConfigEntry.BooleanEntry boolEntry) {
            return createBooleanWidget(boolEntry::get, boolEntry::set, x, y);
        }
        if (entry instanceof ConfigEntry.DoubleEntry doubleEntry) {
            return new AbstractSliderButton(x, y, VALUE_WIDTH, 20, Component.empty(), toSlider(doubleEntry.get(), doubleEntry.getMin(), doubleEntry.getMax())) {
                {
                    updateMessage();
                }

                @Override
                protected void updateMessage() {
                    setMessage(doubleEntry.isPercent()
                            ? Component.literal(String.format("%.0f%%", doubleEntry.get() * 100))
                            : Component.literal(String.format("%.2f", doubleEntry.get())));
                }

                @Override
                protected void applyValue() {
                    double v = doubleEntry.getMin() + (doubleEntry.getMax() - doubleEntry.getMin()) * this.value;
                    doubleEntry.set(Math.round(v * 1000.0) / 1000.0);
                }
            };
        }
        if (entry instanceof ConfigEntry.IntEntry intEntry) {
            return new AbstractSliderButton(x, y, VALUE_WIDTH, 20, Component.empty(), toSlider(intEntry.get(), intEntry.getMin(), intEntry.getMax())) {
                {
                    updateMessage();
                }

                @Override
                protected void updateMessage() {
                    setMessage(Component.literal(String.valueOf(intEntry.get())));
                }

                @Override
                protected void applyValue() {
                    intEntry.set((int) Math.round(intEntry.getMin() + (intEntry.getMax() - intEntry.getMin()) * this.value));
                }
            };
        }
        if (entry instanceof ConfigEntry.StringListEntry listEntry) {
            EditBox box = new EditBox(this.font, x, y, VALUE_WIDTH, 20, Component.empty());
            box.setValue(listEntry.toText());
            box.setMaxLength(1024);
            box.setResponder(listEntry::fromText);
            return box;
        }
        throw new IllegalStateException("unknown entry: " + entry.getClass());
    }

    private static Component booleanText(boolean v) {
        // 开 绿 / 关 红；文案走本地化键，中文显示“开启/关闭”，英文显示“ON/OFF”
        return v ? Component.translatable(LangDefinition.GUI_ON).withStyle(style -> style.withColor(0x55FF55))
                : Component.translatable(LangDefinition.GUI_OFF).withStyle(style -> style.withColor(0xFF5555));
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
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0x55FF9F);
        graphics.drawCenteredString(this.font, Component.translatable(LangDefinition.GUI_SUBTITLE), this.width / 2, 28, 0x888888);

        int left = getLeft();
        int contentWidth = getContentWidth();

        // 行控件与标签裁剪在内容区内渲染：滚动越界时从 Tab 下缘被整齐裁掉，
        // 不会盖住顶部 Tab 与底部完成按钮
        graphics.enableScissor(left - 4, CONTENT_TOP, left + contentWidth + 4, this.height - BOTTOM_MARGIN + 4);
        int y = TOP - scrollOffset;
        for (ConfigEntry entry : currentCategory().entries()) {
            graphics.drawString(this.font, entry.getLabel(), left, y + 6, 0xE0E0E0);
            y += ROW_HEIGHT;
        }
        for (AbstractWidget row : rowWidgets) {
            row.render(graphics, mouseX, mouseY, partialTick);
        }
        graphics.disableScissor();

        // 悬浮标签显示说明 tooltip
        ConfigEntry hovered = getHoveredEntry(mouseX, mouseY);
        if (hovered != null && hovered.getTooltip() != null) {
            graphics.renderTooltip(this.font, this.font.split(hovered.getTooltip(), 170), mouseX, mouseY);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private ConfigEntry getHoveredEntry(double mouseX, double mouseY) {
        if (mouseY < CONTENT_TOP || mouseY > this.height - BOTTOM_MARGIN + 4) {
            return null;
        }
        int left = getLeft();
        int labelWidth = getContentWidth() - VALUE_WIDTH - RESET_WIDTH - 20;
        int y = TOP - scrollOffset;
        for (ConfigEntry entry : currentCategory().entries()) {
            if (mouseX >= left && mouseX <= left + labelWidth && mouseY >= y && mouseY < y + ROW_HEIGHT) {
                return entry;
            }
            y += ROW_HEIGHT;
        }
        return null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int viewport = this.height - TOP - BOTTOM_MARGIN;
        int maxScroll = Math.max(0, contentHeight - viewport);
        if (maxScroll > 0) {
            scrollOffset = Mth.clamp(scrollOffset - (int) (delta * ROW_HEIGHT), 0, maxScroll);
            repositionRows();
        }
        return true;
    }

    /** 滚动后仅调整行控件 X/Y（渲染裁剪由 render() 的剪刀区处理，无需隐藏控件） */
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
        PetHomeConfig.refreshCachedValues();
        PetHomeConfig.save();
        this.minecraft.setScreen(parent);
    }
}
