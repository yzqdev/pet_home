package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.entity.IComandableMob;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;


/** 潜行+准星指向自家宠物时，在屏幕顶部显示信息面板（名字/血量/指令状态/宠物床/附魔）；移植自 fabric 侧同名类 */
public final class PetInfoHudOverlay implements IGuiOverlay {

    private static final int LINE_HEIGHT = 11;
    private static final int PANEL_PAD = 4;
    private static final int PANEL_BACKGROUND = 0xF0101018;
    private static final int PANEL_BORDER = 0xFF55556A;

    private static final boolean JADE_LOADED = ModList.get().isLoaded("jade");

    private PetInfoHudOverlay() {
    }

    /** 注册 HUD 层（mod 事件总线调用） */
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("pet_info_overlay", new PetInfoHudOverlay());
    }

    @Override
    public void render(net.minecraftforge.client.gui.overlay.ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        // Jade 安装且未开启 ignoreJade 时不显示——由 Jade tooltip 展示宠物信息
        if (JADE_LOADED && !PetHomeMod.CONFIG.petInfoOverlayIgnoreJade.get()) {
            return;
        }
        if (!PetHomeMod.CONFIG.petInfoOverlay.get()
                || (PetHomeMod.CONFIG.petInfoOverlayRequireShift.get() && !mc.options.keyShift.isDown())) {
            return;
        }
        if (!(mc.crosshairPickEntity instanceof LivingEntity pet) || !TameableUtils.isPetOf(mc.player, pet)) {
            return;
        }

        List<Component> lines = collectLines(pet);
        if (lines.isEmpty()) {
            return;
        }
        Font font = mc.font;

        int width = 0;
        for (Component line : lines) {
            width = Math.max(width, font.width(line));
        }
        int panelWidth = width + PANEL_PAD * 2;
        int panelHeight = lines.size() * LINE_HEIGHT + PANEL_PAD * 2 - 2;
        int panelX = (graphics.guiWidth() - panelWidth) / 2;
        int panelY = PANEL_PAD;
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL_BACKGROUND);
        graphics.renderOutline(panelX, panelY, panelWidth, panelHeight, PANEL_BORDER);

        int textX = panelX + PANEL_PAD;
        int textY = panelY + PANEL_PAD - 1;
        for (Component line : lines) {
            graphics.drawString(font, line, textX, textY, 0xFFFFFFFF);
            textY += LINE_HEIGHT;
        }
    }

    private static List<Component> collectLines(LivingEntity pet) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(pet.getName().getString()).withStyle(ChatFormatting.AQUA));

        lines.add(Component.translatable(LangDefinition.health_text)
                .append(Component.literal(": " + Math.ceil(pet.getHealth()) + " / " + Math.ceil(pet.getMaxHealth()))
                        .withStyle(ChatFormatting.RED)));

        if (pet instanceof IComandableMob cmd && TameableUtils.isTamed(pet) && PetHomeMod.CONFIG.trinaryCommandSystem.get()) {
            lines.add(Component.translatable("message.pet_home.command_" + cmd.getCommand(), pet.getName())
                    .withStyle(ChatFormatting.YELLOW));
        }

        var bedPos = TameableUtils.getPetBedPos(pet);
        if (bedPos != null) {
            lines.add(Component.translatable(LangDefinition.has_pet_bed_at_pos, bedPos.toShortString())
                    .withStyle(ChatFormatting.RED));
        }

        List<Component> enchants = TameableUtils.getEnchantDescriptions(pet);
        if (enchants.size() > 1) {
            lines.addAll(enchants);
        }
        return lines;
    }
}
