package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.entity.IComandableMob;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 宠物信息面板：准星对准自己的宠物并按住 Shift 时，在屏幕顶部居中显示
 *（名字 / 生命值 / 三态指令 / 宠物床 / 项圈附魔）。
 */
public final class PetInfoHudOverlay {

    private static final int LINE_HEIGHT = 11;
    private static final int PANEL_PAD = 4;
    private static final int PANEL_BACKGROUND = 0xF0101018;
    private static final int PANEL_BORDER = 0xFF55556A;
    /** 模组列表启动后不变，检测结果可缓存；Jade 在场时是否让位由配置逐帧判定（游戏内开关即时生效） */
    private static final boolean JADE_LOADED = FabricLoader.getInstance().isModLoaded("jade");

    private PetInfoHudOverlay() {
    }

    public static void init() {
        HudRenderCallback.EVENT.register(PetInfoHudOverlay::render);
    }

    private static void render(GuiGraphics graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (JADE_LOADED && !PetHomeMod.CONFIG.petInfoOverlayIgnoreJade.get()) {
            return;
        }
        if (!PetHomeMod.CONFIG.petInfoOverlay.get()) {
            return;
        }
        if (PetHomeMod.CONFIG.petInfoOverlayRequireShift.get() && !mc.options.keyShift.isDown()) {
            return;
        }
        if (!(mc.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity pet)
                || !TameableUtils.isPetOf(mc.player, pet)) {
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
        int panelX = (mc.getWindow().getGuiScaledWidth() - panelWidth) / 2;
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

        BlockPos bedPos = TameableUtils.getPetBedPos(pet);
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
