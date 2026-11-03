package com.github.yzqdev.pethome.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 设置界面背景：进世界时与原版 Screen.renderBackground 一致（半透明渐变）；
 * 未进世界（模组列表入口）时改用主菜单动态全景，替代 Forge 默认的静态泥土纹理。
 */
final class PanoramaBackground {
    private static final ResourceLocation OVERLAY =
            new ResourceLocation("minecraft", "textures/gui/title/background/panorama_overlay.png");
    private static final PanoramaRenderer PANORAMA = new PanoramaRenderer(TitleScreen.CUBE_MAP);

    private PanoramaBackground() {
    }

    static void render(Screen screen, GuiGraphics graphics, float partialTick) {
        if (Minecraft.getInstance().level != null) {
            graphics.fillGradient(0, 0, screen.width, screen.height, -1072689136, -804253680);
            return;
        }
        PANORAMA.render(partialTick, 1.0F);
        RenderSystem.enableBlend();
        graphics.blit(OVERLAY, 0, 0, screen.width, screen.height, 0.0F, 0.0F, 16, 128, 16, 128);
        RenderSystem.disableBlend();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
