package com.github.yzqdev.pethome.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

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