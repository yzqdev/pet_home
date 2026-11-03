package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.mixin.SpriteContentsAccessor;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;


/**
 * 矿块高亮色注册（"矿脉嗅探"附魔）：按方块优先取原版 {@code BlockColors} 染色，
 * 否则取贴图平均色，结果缓存进 {@code TEXTURES_TO_COLOR}（取色异常时回退铁矿石）。
 */
public class OreColorRegistry {

    public static final BlockState FALLBACK_BLOCK = Blocks.IRON_ORE.defaultBlockState();
    public static Map<String, Integer> TEXTURES_TO_COLOR = new HashMap<>();

    public static int getBlockColor(BlockState stack) {
        String blockName = stack.toString();
        if (TEXTURES_TO_COLOR.get(blockName) != null) {
            return TEXTURES_TO_COLOR.get(blockName).intValue();
        } else {
            int colorizer = -1;
            try {
                colorizer = Minecraft.getInstance().getBlockColors().getTintSource(stack, 0).color(stack);
            } catch (Exception e) {
                PetHomeMod.LOGGER.warn("Another mod did not use block colorizers correctly.");
            }
            int color = 0XFFFFFF;
            if (colorizer == -1) {
                BufferedImage texture = null;
                try {
                    Color texColour = getAverageColour(getTextureAtlas(stack));
                    color = texColour.getRGB();
                } catch (NullPointerException e) {
                    PetHomeMod.LOGGER.error("Failed to read texture atlas for block {}: {}", blockName, e.toString());
                }
            } else {
                color = colorizer;
            }
            TEXTURES_TO_COLOR.put(blockName, color);
            return color;
        }
    }

    private static Color getAverageColour(TextureAtlasSprite image) {
        // 26.1：TextureAtlasSprite#getPixelRGBA 已移除，经 SpriteContentsAccessor 拿
        // SpriteContents.originalImage（NativeImage）逐像素读取；
        // 注意 NativeImage#getPixel 返回 ARGB 布局（旧 API 是 ABGR），通道位移要按 ARGB 取
        NativeImage pixels = ((SpriteContentsAccessor) (Object) image.contents()).pet_home$getOriginalImage();
        float red = 0;
        float green = 0;
        float blue = 0;
        float count = 0;
        int uMax = image.contents().width();
        int vMax = image.contents().height();
        for (int i = 0; i < uMax; i++)
            for (int j = 0; j < vMax; j++) {
                int argb = pixels.getPixel(i, j);
                int alpha = argb >> 24 & 0xFF;
                if (alpha == 0) {
                    continue;
                }
                int localRed = argb >> 16 & 0xFF;
                int localGreen = argb >> 8 & 0xFF;
                int localBlue = argb >> 0 & 0xFF;
                // 跳过接近灰白的像素（通道差 <10），避免贴图留白把平均色冲淡
                if (Math.abs(Math.max(localRed, Math.max(localGreen, localBlue)) - Math.min(localRed, Math.min(localGreen, localBlue))) < 10) {
                    continue;
                }
                red += localRed;
                green += localGreen;
                blue += localBlue;
                count++;
            }
        // 求平均颜色
        return new Color((int) (red / count), (int) (green / count), (int) (blue / count));
    }

    private static TextureAtlasSprite getTextureAtlas(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
    }
}
