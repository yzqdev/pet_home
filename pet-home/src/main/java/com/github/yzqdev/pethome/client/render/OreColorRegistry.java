package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;


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
        // TODO(迁移中)：26.1 的 SpriteContents 不再提供公开的逐像素读取 API
        // （旧版 TextureAtlasSprite / SpriteContents#getPixelRGBA 已移除，
        //  contents() 只暴露尺寸与 originalImage 私有字段），
        //  这里先返回中性浅灰作为矿石高亮轮廓色。待确认 26.1 的贴图取色方案后恢复“按贴图平均色”的实现。
        return new Color(200, 200, 200);
    }

    private static TextureAtlasSprite getTextureAtlas(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
    }
}
