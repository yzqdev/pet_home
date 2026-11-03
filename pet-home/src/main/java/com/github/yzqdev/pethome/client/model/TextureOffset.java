package com.github.yzqdev.pethome.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** 纹理 UV 偏移值对象：记录部件在贴图集上的起始坐标（模型代码按名称查询用）。 */
@Environment(EnvType.CLIENT)
public class TextureOffset {
    /**
     * The x coordinate offset of the texture
     */
    public final int textureOffsetX;
    /**
     * The y coordinate offset of the texture
     */
    public final int textureOffsetY;

    public TextureOffset(int textureOffsetXIn, int textureOffsetYIn) {
        this.textureOffsetX = textureOffsetXIn;
        this.textureOffsetY = textureOffsetYIn;
    }
}