package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.util.TameableUtils;
import java.util.ArrayList;
import java.util.List;
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

/**
 * 准星指向自家宠物时的顶部信息面板（名字 / 血量 / 三态指令 / 宠物床坐标 / 项圈附魔列表）。 经 {@code HudRenderCallback}
 * 逐帧渲染；显示条件（开关、潜行触发、Jade 让位）逐帧读取配置缓存， 游戏内改配置立即生效。数据全部取自客户端已有的同步实体数据，不发网络包。
 */
public final class PetInfoHudOverlay {

  private static final int LINE_HEIGHT = 11;
  private static final int PANEL_PAD = 4;
  private static final int PANEL_BACKGROUND = 0xF0101018;
  private static final int PANEL_BORDER = 0xFF55556A;

  /** 模组列表启动后不变，检测结果可缓存；Jade 在场时是否让位由配置逐帧判定（游戏内开关即时生效） */
  private static final boolean JADE_LOADED = FabricLoader.getInstance().isModLoaded("jade");

  private PetInfoHudOverlay() {}

  /** 注册到 {@code HudRenderCallback.EVENT}；由 PetHomeModClient.onInitializeClient 调用一次。 */
  public static void init() {
    HudRenderCallback.EVENT.register(PetInfoHudOverlay::render);
  }

  /** 每帧回调：依次检查 Jade 让位、总开关、需潜行、准星目标是否为玩家的宠物， 全部通过才在屏幕顶部居中绘制半透明面板。 */
  private static void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker deltaTracker) {
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
    if (!(mc.hitResult instanceof EntityHitResult hit)
        || !(hit.getEntity() instanceof LivingEntity pet)
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

  /** 汇总面板文本行；三态指令行仅在开启三态指令系统且宠物已驯服时出现，附魔行取 {@code TameableUtils.getEnchantDescriptions}。 */
  private static List<Component> collectLines(LivingEntity pet) {
    List<Component> lines = new ArrayList<>();
    lines.add(Component.literal(pet.getName().getString()).withStyle(ChatFormatting.AQUA));

    lines.add(
        Component.translatable(LangDefinition.health_text)
            .append(
                Component.literal(
                        ": " + Math.ceil(pet.getHealth()) + " / " + Math.ceil(pet.getMaxHealth()))
                    .withStyle(ChatFormatting.RED)));

    if (pet instanceof IComandableMob cmd
        && TameableUtils.isTamed(pet)
        && PetHomeConfig.trinaryCommandSystem) {
      lines.add(
          Component.translatable("message.pet_home.command_" + cmd.getCommand(), pet.getName())
              .withStyle(ChatFormatting.YELLOW));
    }

    BlockPos bedPos = TameableUtils.getPetBedPos(pet);
    if (bedPos != null) {
      lines.add(
          Component.translatable(LangDefinition.has_pet_bed_at_pos, bedPos.toShortString())
              .withStyle(ChatFormatting.RED));
    }

    List<Component> enchants = TameableUtils.getEnchantDescriptions(pet);
    if (enchants.size() > 1) {
      lines.addAll(enchants);
    }
    return lines;
  }
}
