package com.github.yzqdev.pethome.client.gui;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.config.BooleanValue;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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

    private static final int WIDGETS_PER_ROW = 3;
    private static final int TAB_Y = 42;
    private static final int TAB_HEIGHT = 20;

    private static final int CONTENT_TOP = TAB_Y + TAB_HEIGHT;

    private static final int TOP = 64;
    private static final int BOTTOM_MARGIN = 40;

    private static final int CONTENT_BOTTOM_MARGIN = BOTTOM_MARGIN - 4;

    private static final int HEADER_HEIGHT = 42;

    private static final int HEADER_BG = 0xFF101010;

    private static final int HEADER_LINE = 0x40FFFFFF;

    private static final int CONTENT_BG = 0xE8101010;
    private static final int TITLE_COLOR =  0xFF55FF9F;
    private static final int SUBTITLE_COLOR = 0xFFB0B0B0;

    private static final int LABEL_COLOR = 0xFFE0E0E0;

    private final Screen parent;
    private final List<Category> categories = new ArrayList<>();

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
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_ROTTEN_APPLE, LangDefinition.GUI_G_ROTTEN_APPLE_DESC, PetHomeConfig.ROTTEN_APPLE));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_PET_BED_RESPAWN, LangDefinition.GUI_G_PET_BED_RESPAWN_DESC, PetHomeConfig.PET_BED_RESPAWNS));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_RABBITS_RAVAGERS, LangDefinition.GUI_G_RABBITS_RAVAGERS_DESC, PetHomeConfig.RABBITS_SCARE_RAVAGERS));
        
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_PET_COMPASS, LangDefinition.GUI_G_PET_COMPASS_DESC, PetHomeConfig.PET_COMPASS_ENABLE));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_PET_COMPASS_TP_P, LangDefinition.GUI_G_PET_COMPASS_TP_P_DESC, PetHomeConfig.PET_COMPASS_TELEPORT_PLAYER_TO_PET));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_PET_COMPASS_TP_R, LangDefinition.GUI_G_PET_COMPASS_TP_R_DESC, PetHomeConfig.PET_COMPASS_TELEPORT_PET_TO_PLAYER));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_TRINARY_COMMAND, LangDefinition.GUI_G_TRINARY_COMMAND_DESC, PetHomeConfig.TRINARY_COMMAND_SYSTEM));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_PET_INFO_OVERLAY, LangDefinition.GUI_G_PET_INFO_OVERLAY_DESC, PetHomeConfig.PET_INFO_OVERLAY));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT, LangDefinition.GUI_G_PET_INFO_OVERLAY_REQUIRE_SHIFT_DESC, PetHomeConfig.PET_INFO_OVERLAY_REQUIRE_SHIFT));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_PET_INFO_OVERLAY_IGNORE_JADE, LangDefinition.GUI_G_PET_INFO_OVERLAY_IGNORE_JADE_DESC, PetHomeConfig.PET_INFO_OVERLAY_IGNORE_JADE));
        general.add(new ConfigEntry.IntEntry(LangDefinition.GUI_G_PETSTORE_WEIGHT, LangDefinition.GUI_G_PETSTORE_WEIGHT_DESC, PetHomeConfig.PETSTORE_VILLAGE_WEIGHT, 0, 100));
        general.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_G_MOB_TAMABLE_ONLY, LangDefinition.GUI_G_MOB_TAMABLE_ONLY_DESC, PetHomeConfig.MOBCATCHER_ONLY_TAMABLE_ANIMAL));
        general.add(new ConfigEntry.StringListEntry(LangDefinition.GUI_G_MOB_BLACKLIST, LangDefinition.GUI_G_MOB_BLACKLIST_DESC, PetHomeConfig.MOBCATCHER_BLACKLIST));
        categories.add(new Category(LangDefinition.GUI_CAT_GENERAL_NAME, general));

        List<ConfigEntry> tameable = new ArrayList<>();
        tameable.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_T_AXOLOTL, LangDefinition.GUI_T_AXOLOTL_DESC, PetHomeConfig.TAMEABLE_AXOLOTL));
        tameable.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_T_FOX, LangDefinition.GUI_T_FOX_DESC, PetHomeConfig.TAMEABLE_FOX));
        tameable.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_T_FROG, LangDefinition.GUI_T_FROG_DESC, PetHomeConfig.TAMEABLE_FROG));
        tameable.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_T_HORSE, LangDefinition.GUI_T_HORSE_DESC, PetHomeConfig.TAMEABLE_HORSE));
        tameable.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_T_RABBIT, LangDefinition.GUI_T_RABBIT_DESC, PetHomeConfig.TAMEABLE_RABBIT));
        categories.add(new Category(LangDefinition.GUI_CAT_TAMEABLE_NAME, tameable));

        List<ConfigEntry> loot = new ArrayList<>();
        loot.add(new ConfigEntry.BooleanEntry(LangDefinition.GUI_L_CURSE_LOOT_ONLY, LangDefinition.GUI_L_CURSE_LOOT_ONLY_DESC, PetHomeConfig.PET_CURSE_ENCHANTMENTS_LOOT_ONLY));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.item("sinister_carrot"), LangDefinition.GUI_L_SINISTER_CARROT_DESC, PetHomeConfig.SINISTER_CARROT_LOOT_CHANCE, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.enchantment("bubbling"), LangDefinition.GUI_L_BUBBLING_DESC, PetHomeConfig.BUBBLING_LOOT_CHANCE, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.enchantment("vampire"), LangDefinition.GUI_L_VAMPIRISM_DESC, PetHomeConfig.VAMPIRISM_LOOT_CHANCE, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.enchantment("void_cloud"), LangDefinition.GUI_L_VOID_CLOUD_DESC, PetHomeConfig.VOID_CLOUD_LOOT_CHANCE, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.enchantment("ore_scenting"), LangDefinition.GUI_L_ORE_SCENTING_DESC, PetHomeConfig.ORE_SCENTING_LOOT_CHANCE, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.enchantment("blazing_protection"), LangDefinition.GUI_L_BLAZING_DESC, PetHomeConfig.BLAZING_PROTECTION_LOOT_CHANCE, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.enchantment("share"), LangDefinition.GUI_L_SHARE_DESC, PetHomeConfig.SHARE_LOOT_CHANCE, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.enchantment("sonic_boom"), LangDefinition.GUI_L_SONIC_BOOM_DESC, PetHomeConfig.SONIC_BOOM_LOOT_CHANCE, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.enchantment("paralysis"), LangDefinition.GUI_L_PARALYSIS_DESC, PetHomeConfig.PARALYSIS_LOOT_CHANCE, 0, 1, true));
        loot.add(new ConfigEntry.DoubleEntry(LangDefinition.enchantment("tough"), LangDefinition.GUI_L_TOUGH_DESC, PetHomeConfig.TOUGH_LOOT_CHANCE, 0, 1, true));
        categories.add(new Category(LangDefinition.GUI_CAT_LOOT_NAME, loot));

        List<ConfigEntry> enchants = new ArrayList<>();
        for (Map.Entry<String, BooleanValue> e : PetHomeConfig.ENABLED_ENCHANTMENTS.entrySet()) {
            String registryName = e.getKey();
            
            String labelKey = LangDefinition.enchantment(registryName);
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
        int valueX = left + contentWidth - VALUE_WIDTH - RESET_WIDTH - 10;
        int resetX = left + contentWidth - RESET_WIDTH;

        
        int labelWidth = getLabelWidth();
        rowWidgets.clear();
        int y = TOP - scrollOffset;
        for (ConfigEntry entry : currentCategory().entries()) {
            RowLabel label = new RowLabel(this.font, left, y, labelWidth, 20, entry.getLabel());
            this.addRenderableWidget(label);
            rowWidgets.add(label);

            AbstractWidget valueWidget = createValueWidget(entry, valueX, y);
            this.addRenderableWidget(valueWidget);
            rowWidgets.add(valueWidget);

            AbstractWidget resetButton = Button.builder(Component.translatable(LangDefinition.GUI_RESET), b -> {
                entry.resetToDefault();
                rebuildWidgets();
            }).bounds(resetX, y, RESET_WIDTH, 20).build();
            this.addRenderableWidget(resetButton);
            rowWidgets.add(resetButton);
            y += ROW_HEIGHT;
        }
        this.contentHeight = currentCategory().entries().size() * ROW_HEIGHT;

        
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

        
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());

        
        updateRowVisibility();
    }

    private AbstractWidget createBooleanWidget(java.util.function.Supplier<Boolean> getter, java.util.function.Consumer<Boolean> setter, int x, int y) {
        return Button.builder(booleanText(getter.get()), b -> {
            boolean newValue = !getter.get();
            setter.accept(newValue);
            b.setMessage(booleanText(newValue));
        }).bounds(x, y, VALUE_WIDTH, 20).build();
    }

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
        
        return v ? Component.translatable(LangDefinition.GUI_ON).withStyle(style -> style.withColor(0x55FF55))
                : Component.translatable(LangDefinition.GUI_OFF).withStyle(style -> style.withColor(0xFF5555));
    }

    private static double toSlider(double v, double min, double max) {
        return max <= min ? 0.0 : Mth.clamp((v - min) / (max - min), 0.0, 1.0);
    }

    private int getLeft() {
        return Math.max(20, this.width / 2 - 205);
    }

    private int getContentWidth() {
        return this.width - getLeft() * 2;
    }

    private int getLabelWidth() {
        return getContentWidth() - VALUE_WIDTH - RESET_WIDTH - 20;
    }

    private int getContentBottom() {
        return this.height - CONTENT_BOTTOM_MARGIN;
    }

    private boolean isRowFullyVisible(int rowY) {
        return rowY >= CONTENT_TOP && rowY + 20 <= getContentBottom();
    }

    private void updateRowVisibility() {
        for (AbstractWidget widget : rowWidgets) {
            widget.visible = isRowFullyVisible(widget.getY());
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        
        graphics.fill(0, 0, this.width, HEADER_HEIGHT, HEADER_BG);
        graphics.fill(0, HEADER_HEIGHT - 1, this.width, HEADER_HEIGHT, HEADER_LINE);

        
        int left = getLeft();
        graphics.fill(left - 6, CONTENT_TOP, left + getContentWidth() + 6, getContentBottom() + 6, CONTENT_BG);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        
        super.render(graphics, mouseX, mouseY, partialTick);

        
        
        drawCenteredStringWithShadow(graphics, this.title, 14, TITLE_COLOR);
        drawCenteredStringWithShadow(graphics, Component.translatable(LangDefinition.GUI_SUBTITLE), 28, SUBTITLE_COLOR);

        
        ConfigEntry hovered = getHoveredEntry(mouseX, mouseY);
        if (hovered != null && hovered.getTooltip() != null) {
            graphics.renderTooltip(this.font, this.font.split(hovered.getTooltip(), 170), mouseX, mouseY);
        }
    }

    private void drawCenteredStringWithShadow(GuiGraphics graphics, Component text, int y, int color) {
        graphics.drawString(this.font, text, (this.width - this.font.width(text)) / 2, y, color, true);
    }

    private ConfigEntry getHoveredEntry(double mouseX, double mouseY) {
        int left = getLeft();
        int labelWidth = getLabelWidth();
        int y = TOP - scrollOffset;
        for (ConfigEntry entry : currentCategory().entries()) {
            if (isRowFullyVisible(y)
                    && mouseX >= left && mouseX <= left + labelWidth
                    && mouseY >= y && mouseY < y + ROW_HEIGHT) {
                return entry;
            }
            y += ROW_HEIGHT;
        }
        return null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int viewport = this.height - TOP - BOTTOM_MARGIN;
        int maxScroll = Math.max(0, contentHeight - viewport);
        if (maxScroll > 0) {
            int next = Mth.clamp(scrollOffset - (int) (scrollY * ROW_HEIGHT), 0, maxScroll);
            
            scrollOffset = Mth.clamp(Math.round(next / (float) ROW_HEIGHT) * ROW_HEIGHT, 0, maxScroll);
            repositionRows();
        }
        return true;
    }

    private void repositionRows() {
        int left = getLeft();
        int contentWidth = getContentWidth();
        int valueX = left + contentWidth - VALUE_WIDTH - RESET_WIDTH - 10;
        int resetX = left + contentWidth - RESET_WIDTH;
        int y = TOP - scrollOffset;
        for (int i = 0; i < rowWidgets.size(); i++) {
            AbstractWidget widget = rowWidgets.get(i);
            switch (i % WIDGETS_PER_ROW) {
                case 0 -> widget.setX(left);
                case 1 -> widget.setX(valueX);
                default -> widget.setX(resetX);
            }
            widget.setY(y);
            if (i % WIDGETS_PER_ROW == WIDGETS_PER_ROW - 1) {
                y += ROW_HEIGHT;
            }
        }
        updateRowVisibility();
    }

    private static class RowLabel extends AbstractWidget {
        private final Font font;

        RowLabel(Font font, int x, int y, int width, int height, Component message) {
            super(x, y, width, height, message);
            this.font = font;
            this.active = false; 
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.drawString(this.font, this.getMessage(), this.getX(), this.getY() + 6, LABEL_COLOR, true);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            
        }
    }

    @Override
    public void onClose() {
        PetHomeConfig.refreshCachedValues();
        PetHomeConfig.save();
        this.minecraft.setScreen(parent);
    }
}