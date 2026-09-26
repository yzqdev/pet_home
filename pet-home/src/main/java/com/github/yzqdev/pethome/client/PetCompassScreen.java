package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.network.PropertiesMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 宠物罗盘 GUI ：手持罗盘右键打开，列表展示玩家全部宠物
 * （名称/类型/位置/维度/状态），左键选中，选中行提供「传送至宠物」「召回宠物」操作——
 * 两个按钮分别由 teleportPlayerToPet / teleportPetToPlayer 配置独立显示（服务端权威下发）。
 * 数据由服务端在 C2S pet_compass_open 后打包 S2C 下发，本类不向服务端查询任何东西。
 */
@Environment(EnvType.CLIENT)
public class PetCompassScreen extends Screen {

    private static final int ROW_HEIGHT = 44;
    private static final int BUTTON_WIDTH = 64;
    private static final int PANEL_HALF_WIDTH = 190;
    private static final int PANEL_TOP = 24;
    /** 列表第一行的 y（PANEL_TOP + 30）；渲染与点击命中共用 */
    private static final int LIST_TOP = 54;

    public record Entry(UUID petId, String name, String type, String dim, int x, int y, int z, int dist,
                        boolean loaded, boolean alive, boolean sameDim, long lastTime) {
    }

    private final List<Entry> entries = new ArrayList<>();
    private boolean cfgToPet;
    private boolean cfgToPlayer;
    private int scrollOffset;
    @Nullable
    private UUID selected;

    public PetCompassScreen() {
        super(Component.translatable(LangDefinition.message("pet_compass.title")));
    }

    /** S2C pet_compass_data 入口（PropertiesMessage.handleClient 分发） */
    public static void handleData(@Nullable CompoundTag tag) {
        Minecraft minecraft = Minecraft.getInstance();
        if (tag == null || minecraft.player == null) {
            return;
        }
        PetCompassScreen screen = new PetCompassScreen();
        screen.cfgToPet = tag.getBooleanOr("CfgToPet", false);
        screen.cfgToPlayer = tag.getBooleanOr("CfgToPlayer", false);
        String playerDim = minecraft.player.level().dimension().identifier().toString();
        int count = tag.getIntOr("Count", 0);
        for (int i = 0; i < count; i++) {
            CompoundTag entry = tag.getCompoundOrEmpty("Pet" + i);
            screen.entries.add(new Entry(
                    parse(entry.getStringOr("PetId", "")),
                    entry.getStringOr("Name", "?"),
                    entry.getStringOr("Type", "?"),
                    entry.getStringOr("Dim", "?"),
                    entry.getIntOr("X", 0), entry.getIntOr("Y", 0), entry.getIntOr("Z", 0),
                    entry.getIntOr("Dist", 0),
                    entry.getBooleanOr("Loaded", false),
                    entry.getBooleanOr("Alive", false),
                    entry.getStringOr("Dim", "").equals(playerDim),
                    entry.getLongOr("LastTime", 0L)));
        }
        minecraft.setScreen(screen);
    }

    private static UUID parse(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return UUID.randomUUID();
        }
    }

    /** 键统一带 pet_compass. 段（与语言文件注册键一致） */
    private static MutableComponent msg(String key, Object... args) {
        return Component.translatable(LangDefinition.message("pet_compass." + key), args);
    }

    /** 维度友好名：三个主维度用语言键，其余回退原始 id */
    private static MutableComponent dimName(String dim) {
        return switch (dim) {
            case "minecraft:overworld" -> msg("dim_overworld");
            case "minecraft:the_nether" -> msg("dim_the_nether");
            case "minecraft:the_end" -> msg("dim_the_end");
            default -> Component.literal(dim);
        };
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int panelX0 = this.width / 2 - PANEL_HALF_WIDTH;
        int panelX1 = this.width / 2 + PANEL_HALF_WIDTH;
        int panelY1 = this.height - 12;
        // 面板底 + 边框 + 标题 + 分隔线
        graphics.fill(panelX0, PANEL_TOP, panelX1, panelY1, 0xF0101018);
        graphics.outline(panelX0, PANEL_TOP, panelX1 - panelX0, panelY1 - PANEL_TOP, 0xFF55556A);
        graphics.centeredText(this.font, this.title, this.width / 2, PANEL_TOP + 6, 0xFFFFFFFF);
        graphics.horizontalLine(panelX0 + 6, panelX1 - 6, PANEL_TOP + 24, 0xFF55556A);

        int listBottom = panelY1 - 8;
        if (this.entries.isEmpty()) {
            graphics.centeredText(this.font, msg("empty").withStyle(ChatFormatting.GRAY), this.width / 2, (PANEL_TOP + panelY1) / 2 - 8, 0xFFFFFFFF);
            return;
        }

        int rowWidth = panelX1 - panelX0 - 20;
        int listLeft = panelX0 + 10;
        int maxVisible = Math.max(1, (listBottom - LIST_TOP) / ROW_HEIGHT);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.entries.size() - maxVisible));

        graphics.enableScissor(panelX0 + 2, LIST_TOP, panelX1 - 2, listBottom);
        for (int visibleIndex = 0; visibleIndex < maxVisible; visibleIndex++) {
            int entryIndex = this.scrollOffset + visibleIndex;
            if (entryIndex >= this.entries.size()) {
                break;
            }
            renderEntry(graphics, this.entries.get(entryIndex), listLeft, LIST_TOP + visibleIndex * ROW_HEIGHT, rowWidth, mouseX, mouseY);
        }
        graphics.disableScissor();
    }

    private void renderEntry(GuiGraphicsExtractor graphics, Entry entry, int left, int top, int rowWidth, int mouseX, int mouseY) {
        boolean hovered = mouseX >= left - 6 && mouseX <= left + rowWidth + 6 && mouseY >= top && mouseY < top + ROW_HEIGHT - 4;
        boolean isSelected = entry.petId.equals(this.selected);
        if (isSelected) {
            graphics.fill(left - 6, top, left + rowWidth + 6, top + ROW_HEIGHT - 4, 0x66395C8F);
        } else if (hovered) {
            graphics.fill(left - 6, top, left + rowWidth + 6, top + ROW_HEIGHT - 4, 0x24FFFFFF);
        }

        graphics.text(this.font, Component.literal(entry.name).withStyle(ChatFormatting.GOLD), left, top, 0xFFFFFFFF);
        graphics.text(this.font,
                Component.translatable("entity." + entry.type).withStyle(ChatFormatting.DARK_GRAY),
                left + this.font.width(entry.name) + 8, top, 0xFFFFFFFF);

        if (!entry.alive) {
            graphics.text(this.font, msg("status_dead").withStyle(ChatFormatting.RED), left, top + 12, 0xFFFFFFFF);
        } else {
            String posLabel = entry.loaded ? msg("position").getString() : msg("last_known_pos").getString();
            String posText = posLabel + entry.x + ", " + entry.y + ", " + entry.z;
            graphics.text(this.font, Component.literal(posText).withStyle(ChatFormatting.GRAY), left, top + 12, 0xFFFFFFFF);
            graphics.text(this.font, msg("distance", entry.dist).withStyle(ChatFormatting.AQUA),
                    left + this.font.width(posText) + 8, top + 12, 0xFFFFFFFF);

            Component status = entry.loaded
                    ? (entry.sameDim ? msg("status_loaded").withStyle(ChatFormatting.GREEN) : msg("status_wrong_dim").withStyle(ChatFormatting.YELLOW))
                    : msg("status_not_loaded").withStyle(ChatFormatting.YELLOW);
            graphics.text(this.font, dimName(entry.dim).withStyle(ChatFormatting.GRAY).append("  ").append(status), left, top + 24, 0xFFFFFFFF);
        }

        // 选中行的操作按钮（按配置独立显示 ）
        if (isSelected && entry.alive) {
            int buttonY = top + 5;
            int buttonX = left + rowWidth - BUTTON_WIDTH;
            if (this.cfgToPet) {
                renderButton(graphics, "button_tp", buttonX, buttonY, mouseX, mouseY);
                buttonX -= BUTTON_WIDTH + 6;
            }
            if (this.cfgToPlayer) {
                renderButton(graphics, "button_recall", buttonX, buttonY, mouseX, mouseY);
            }
        }
    }

    private void renderButton(GuiGraphicsExtractor graphics, String key, int x, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + BUTTON_WIDTH && mouseY >= y && mouseY <= y + 16;
        graphics.fill(x, y, x + BUTTON_WIDTH, y + 16, hovered ? 0xFF44598A : 0xFF2E3542);
        graphics.outline(x, y, BUTTON_WIDTH, 16, hovered ? 0xFFA9BEE8 : 0xFF7A8FB5);
        graphics.centeredText(this.font, msg(key), x + BUTTON_WIDTH / 2, y + 4, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int panelX0 = this.width / 2 - PANEL_HALF_WIDTH;
        int panelX1 = this.width / 2 + PANEL_HALF_WIDTH;
        int listBottom = this.height - 20;
        if (event.button() == 0 && mouseX >= panelX0 && mouseX <= panelX1 && mouseY >= LIST_TOP && mouseY < listBottom && !this.entries.isEmpty()) {
            int rowWidth = panelX1 - panelX0 - 20;
            int listLeft = panelX0 + 10;
            int visibleIndex = (int) ((mouseY - LIST_TOP) / ROW_HEIGHT);
            int entryIndex = this.scrollOffset + visibleIndex;
            if (entryIndex < this.entries.size()) {
                Entry entry = this.entries.get(entryIndex);
                this.selected = entry.petId;
                if (entry.alive) {
                    int buttonX = listLeft + rowWidth - BUTTON_WIDTH;
                    int buttonY = LIST_TOP + visibleIndex * ROW_HEIGHT + 5;
                    // 按钮从右往左排：toPet 最右，toPlayer 在其左侧
                    if (this.cfgToPlayer && mouseX >= buttonX - BUTTON_WIDTH - 6 && mouseX <= buttonX - 6 && mouseY >= buttonY && mouseY <= buttonY + 16) {
                        sendAction(entry, "to_player");
                        return true;
                    }
                    if (this.cfgToPet && mouseX >= buttonX && mouseX <= buttonX + BUTTON_WIDTH && mouseY >= buttonY && mouseY <= buttonY + 16) {
                        sendAction(entry, "to_pet");
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void sendAction(Entry entry, String action) {
        CompoundTag tag = new CompoundTag();
        tag.putString("PetId", entry.petId.toString());
        tag.putString("Action", action);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new PropertiesMessage(PHConstants.petCompassAction, tag, 0));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xAmount, double yAmount) {
        this.scrollOffset -= (int) Math.signum(yAmount);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
