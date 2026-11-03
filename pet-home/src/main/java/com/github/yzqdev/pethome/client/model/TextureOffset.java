package com.github.yzqdev.pethome.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** 纹理 UV 偏移的简单值对（Tabula 模型导入时的贴图坐标记录）。 */
@Environment(EnvType.CLIENT)
public class TextureOffset {
  /** The x coordinate offset of the texture */
  public final int textureOffsetX;

  /** The y coordinate offset of the texture */
  public final int textureOffsetY;

  public TextureOffset(int textureOffsetXIn, int textureOffsetYIn) {
    this.textureOffsetX = textureOffsetXIn;
    this.textureOffsetY = textureOffsetYIn;
  }
}
