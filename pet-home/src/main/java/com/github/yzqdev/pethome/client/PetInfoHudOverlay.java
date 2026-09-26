package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;


public final class PetInfoHudOverlay implements HudElement {

    private static final int LINE_HEIGHT = 11;
    private static final int PANEL_PAD = 4;
    private static final int PANEL_BACKGROUND = 0xF0101018;
    private static final int PANEL_BORDER = 0xFF55556A;
    /** 模组列表启动后不变，检测结果可缓存；Jade 在场时是否让位由配置逐帧判定（游戏内开关即时生效） */
    private static final boolean JADE_LOADED = FabricLoader.getInstance().isModLoaded("jade");

    private PetInfoHudOverlay() {
    }

    public static void init() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_info_overlay"),
                new PetInfoHudOverlay());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (JADE_LOADED && !PetHomeConfig.petInfoOverlayIgnoreJade) {
            return;
        }
        if (!PetHomeConfig.petInfoOverlay) {
            return;
        }
        if (PetHomeConfig.petInfoOverlayRequireShift && !mc.options.keyShift.isDown()) {
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
        graphics.outline(panelX, panelY, panelWidth, panelHeight, PANEL_BORDER);

        int textX = panelX + PANEL_PAD;
        int textY = panelY + PANEL_PAD - 1;
        for (Component line : lines) {
            graphics.text(font, line, textX, textY, 0xFFFFFFFF);
            textY += LINE_HEIGHT;
        }
    }

    private static List<Component> collectLines(LivingEntity pet) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(pet.getName().getString()).withStyle(ChatFormatting.AQUA));

        lines.add(Component.translatable(LangDefinition.health_text)
                .append(Component.literal(": " + Math.ceil(pet.getHealth()) + " / " + Math.ceil(pet.getMaxHealth()))
                        .withStyle(ChatFormatting.RED)));

        if (pet instanceof IComandableMob cmd && TameableUtils.isTamed(pet) && PetHomeConfig.trinaryCommandSystem) {
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
