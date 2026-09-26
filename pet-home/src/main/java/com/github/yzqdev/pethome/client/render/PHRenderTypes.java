package com.github.yzqdev.pethome.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.joml.Matrix4f;

import java.util.function.Function;

public class PHRenderTypes {
    protected static final TextureTransform PSYCHIC_WALL_TEXTURING = new TextureTransform(
            "entity_glint_texturing", () -> {
      return   setupPsychicWallTexturing(0.5F, 40L);
    } );
    private static Matrix4f setupPsychicWallTexturing(float in, long time) {
        long i = Util.getMillis() * 2;
        float f1 = (float) (i % 30000L) / 30000.0F;
        float f2 = (float) Math.sin(i / 20000.0F) + 1.0F;
        float f3 = (float) Math.cos(i / 20000.0F) + 1.0F;
       Matrix4f matrix4f = (new Matrix4f()).translation(1 + f2, 1 + f3, 0.0F);
        matrix4f.scale(in);
      return matrix4f;
    }
    // 假设你的纹理坐标偏移（TEXTURING）逻辑需要保持，
// 如果 PSYCHIC_WALL_TEXTURING 是自定义的，请确保它实现了 TextureTransform 接口
    private static final Function<Identifier, RenderType> PSYCHIC_WALL;

    private static final Function<Identifier, RenderType> PSYCHIC_WALL_BORDER;

    public static RenderType psychicWall(Identifier identifier) {
        return PSYCHIC_WALL.apply(identifier);
    }

    public static RenderType psychicWallBorder(Identifier identifier) {
        return PSYCHIC_WALL_BORDER.apply(identifier);
    }

    static {

        PSYCHIC_WALL = Util.memoize((texture) -> {
            return RenderType.create(
                    "psychic_wall2",
                    RenderSetup.builder(RenderPipelines.GLINT)// 使用内置的 Glint 流水线
                            .withTexture("Sampler0", texture)
                            .setTextureTransform( PSYCHIC_WALL_TEXTURING )



                            .createRenderSetup()
            );
        });
        PSYCHIC_WALL_BORDER = Util.memoize(texture -> {
            var type = RenderType.create(
                    "psychic_wall_border",
                    RenderSetup.builder(RenderPipelines.ENTITY_CUTOUT) // 1. 使用预设的 Glint 流水线
                            .withTexture("Sampler0",
                                    texture,
                                    () -> RenderSystem.getSamplerCache()
                                            .getSampler(
                                                    AddressMode.REPEAT, // 对应原版的 blur/mipmap 设置
                                                    AddressMode.REPEAT,
                                                    FilterMode.LINEAR,
                                                    FilterMode.NEAREST,
                                                    true
                                            )
                            )
                            .setTextureTransform(TextureTransform.ENTITY_GLINT_TEXTURING)
                            .useLightmap()
                            .useOverlay()
                            .affectsCrumbling().setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                            .createRenderSetup()
            );
            return type;
        });
    }

}
