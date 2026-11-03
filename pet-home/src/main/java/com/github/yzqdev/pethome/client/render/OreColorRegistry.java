package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.PetHomeMod;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 矿脉嗅探高亮颜色注册表：给定 {@code BlockState} 计算一个“代表色”。优先走原版方块着色器；
 * 着色器未提供时取方块粒子贴图（纹理图集）逐像素平均色（剔除透明像素与近灰白像素——它们是石头底色， 会把矿脉色冲淡），结果缓存进 {@code TEXTURES_TO_COLOR}
 * 避免每帧读贴图。
 */
@Environment(EnvType.CLIENT)
public class OreColorRegistry {

  /** 无法取色时的兜底方块（铁矿石）。 */
  public static final BlockState FALLBACK_BLOCK = Blocks.IRON_ORE.defaultBlockState();

  /** 方块名字符串 → 代表色（ARGB）缓存。 */
  public static Map<String, Integer> TEXTURES_TO_COLOR = new HashMap<>();

  /** 取方块代表色：缓存命中直接返回；否则尝试方块着色器，失败再读贴图平均色并写缓存。 */
  public static int getBlockColor(BlockState stack) {
    String blockName = stack.toString();
    if (TEXTURES_TO_COLOR.get(blockName) != null) {
      return TEXTURES_TO_COLOR.get(blockName).intValue();
    } else {
      int colorizer = -1;
      try {
        colorizer = Minecraft.getInstance().getBlockColors().getColor(stack, null, null, 0);
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
          e.printStackTrace();
        }
      } else {
        color = colorizer;
      }
      TEXTURES_TO_COLOR.put(blockName, color);
      return color;
    }
  }

  private static Color getAverageColour(TextureAtlasSprite image) {
    float red = 0;
    float green = 0;
    float blue = 0;
    float count = 0;
    com.mojang.blaze3d.platform.NativeImage nativeImage = image.contents().originalImage;
    int uMax = nativeImage.getWidth();
    int vMax = nativeImage.getHeight();
    for (float i = 0; i < uMax; i++)
      for (float j = 0; j < vMax; j++) {
        int alpha = nativeImage.getPixelRGBA((int) i, (int) j) >> 24 & 0xFF;
        if (alpha == 0) {
          continue;
        }
        float localRed = nativeImage.getPixelRGBA((int) i, (int) j) >> 0 & 0xFF;
        float localGreen = nativeImage.getPixelRGBA((int) i, (int) j) >> 8 & 0xFF;
        float localBlue = nativeImage.getPixelRGBA((int) i, (int) j) >> 16 & 0xFF;
        if (Math.abs(
                Math.max(localRed, Math.max(localGreen, localBlue))
                    - Math.min(localRed, Math.min(localGreen, localBlue)))
            < 10) {
          continue;
        }
        red += localRed;
        green += localGreen;
        blue += localBlue;
        count++;
      }
    // Average color
    return new Color((int) (red / count), (int) (green / count), (int) (blue / count));
  }

  private static TextureAtlasSprite getTextureAtlas(BlockState state) {
    return Minecraft.getInstance()
        .getBlockRenderer()
        .getBlockModelShaper()
        .getBlockModel(state)
        .getParticleIcon();
  }
}
