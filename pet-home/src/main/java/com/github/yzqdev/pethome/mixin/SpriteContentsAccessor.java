package com.github.yzqdev.pethome.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * {@code SpriteContents} 访问器：{@link Accessor} 暴露私有的 {@code originalImage}
 * （矿脉嗅探高亮色的贴图平均色取色需要逐像素读取；
 * 26.1 的 {@code TextureAtlasSprite#getPixelRGBA} 已移除，像素只能从该字段拿）。
 */
@Mixin(SpriteContents.class)
public interface SpriteContentsAccessor {

    @Accessor("originalImage")
    NativeImage pet_home$getOriginalImage();
}
