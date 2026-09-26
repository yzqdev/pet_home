package com.github.yzqdev.pethome.client.render;


import com.github.yzqdev.pethome.PetHomeMod;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.joml.Matrix4f;

/**
 * 26.1: RenderStateShard/CompositeState 体系已被 RenderPipeline + RenderSetup 取代。
 * 原有的 TexturingStateShard 改为 {@link TextureTransform}。
 * 对应 1.21 的 RENDERTYPE_GLINT_SHADER + LEQUAL 深度测试的自定义发光管线在此注册
 * （见 {@link #registerPipelines}，由 ClientModEvents 在 RegisterRenderPipelinesEvent 时调用）。
 */
public class DIRenderTypes {

    public static final RenderPipeline GLOW_GLEQU = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET, RenderPipelines.FOG_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
            .withLocation(Identifier.parse(PetHomeMod.MODID + ":pipeline/glow_glequ"))
            .withVertexShader("core/glint")
            .withFragmentShader("core/glint")
            .withSampler("Sampler0")
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .build();
    // 1.21: RENDERTYPE_GLINT_SHADER + LIGHTNING_TRANSPARENCY + LEQUAL_DEPTH_TEST（灵能墙/治疗光环的叠加发光）
    public static final RenderPipeline GLOW_LIGHTNING_GLEQU = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET, RenderPipelines.FOG_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
            .withLocation(Identifier.parse(PetHomeMod.MODID + ":pipeline/glow_lightning_glequ"))
            .withVertexShader("core/glint")
            .withFragmentShader("core/glint")
            .withSampler("Sampler0")
            .withCull(false)
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
            .build();

    public static void registerPipelines(java.util.function.Consumer<RenderPipeline> registrar) {
        registrar.accept(GLOW_GLEQU);
        registrar.accept(GLOW_LIGHTNING_GLEQU);
    }

    protected static final TextureTransform IFRAME_TEXTURING = new TextureTransform("pethome_iframe_texturing",
            () -> setupIframeShading(3, 7L));

    protected static final TextureTransform SHADOW_HAND_TEXTURING = new TextureTransform("pethome_shadow_hand_texturing",
            () -> setupShadowHandShading(0.5F, 2L));

    protected static final TextureTransform PSYCHIC_WALL_TEXTURING = new TextureTransform("pethome_psychic_wall_texturing",
            () -> setupPsychicWallTexturing(0.5F, 40L));

    // 1.21: EQUAL_DEPTH_TEST + GLINT_TRANSPARENCY + 实体发光着色器 → 原版 GLINT 管线即为 EQUAL 深度测试
    public static final RenderType IFRAME_GLINT = RenderType.create("iframe_glint",
            RenderSetup.builder(RenderPipelines.GLINT)
                    .withTexture("Sampler0",
                            Identifier.parse(PetHomeMod.MODID + ":textures/immunity_frame_overlay.png"),
                            () -> RenderSystem.getSamplerCache().getSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.LINEAR, FilterMode.NEAREST, true))
                    .setTextureTransform(IFRAME_TEXTURING)
                    .createRenderSetup());
    public static final RenderType VOID_CLOUD = RenderType.create("void_cloud",
            RenderSetup.builder(RenderPipelines.END_PORTAL)
                    .withTexture("Sampler0", TheEndPortalRenderer.END_SKY_LOCATION)
                    .withTexture("Sampler1", Identifier.parse(PetHomeMod.MODID + ":textures/void_cloud.png"))
                    .createRenderSetup());
    public static final RenderType SHADOW_HAND_ENTITY = RenderType.create("shadow_hand_entity",
            RenderSetup.builder(GLOW_GLEQU)
                    .withTexture("Sampler0",
                            Identifier.parse(PetHomeMod.MODID + ":textures/shadow_hand.png"),
                            () -> RenderSystem.getSamplerCache().getSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.NEAREST, FilterMode.NEAREST, true))
                    .setTextureTransform(SHADOW_HAND_TEXTURING)
                    .createRenderSetup());
    public static final RenderType PSYCHIC_WALL = RenderType.create("psychic_wall2",
            RenderSetup.builder(GLOW_LIGHTNING_GLEQU)
                    .withTexture("Sampler0",
                            Identifier.parse(PetHomeMod.MODID + ":textures/psychic_wall_overlay.png"),
                            () -> RenderSystem.getSamplerCache().getSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.LINEAR, FilterMode.NEAREST, true))
                    .setTextureTransform(PSYCHIC_WALL_TEXTURING)
                    .createRenderSetup());
    public static final RenderType PSYCHIC_WALL_BORDER = RenderType.create("psychic_wall_border",
            RenderSetup.builder(GLOW_LIGHTNING_GLEQU)
                    .withTexture("Sampler0",
                            Identifier.parse(PetHomeMod.MODID + ":textures/psychic_wall_border.png"),
                            () -> RenderSystem.getSamplerCache().getSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.LINEAR, FilterMode.NEAREST, true))
                    .createRenderSetup());
    public static final RenderType HEALING_AURA = RenderType.create("healing_aura",
            RenderSetup.builder(GLOW_LIGHTNING_GLEQU)
                    .withTexture("Sampler0",
                            Identifier.parse(PetHomeMod.MODID + ":textures/healing_aura.png"),
                            () -> RenderSystem.getSamplerCache().getSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.LINEAR, FilterMode.NEAREST, true))
                    .createRenderSetup());

    private static Matrix4f setupShadowHandShading(float in, long time) {
        long i = Util.getMillis() * time;
        float f = (float) (i % 110000L) / 110000.0F;
        float f1 = (float) (i % 30000L) / 30000.0F;
        Matrix4f matrix4f = (new Matrix4f()).translation(0, -f1, 0.0F);
        matrix4f.scale(in);
        return matrix4f;
    }

    private static Matrix4f setupIframeShading(float in, long time) {
        long i = Util.getMillis() * time;
        float f1 = (float) (i % 30000L) / 30000.0F;
        Matrix4f matrix4f = (new Matrix4f()).translation(0, f1, 0.0F);
        matrix4f.rotateZ((float) (Math.PI / 4F))
                .scale(in);
        return matrix4f;
    }

    private static Matrix4f setupPsychicWallTexturing(float in, long time) {
        long i = Util.getMillis() * 2;
        float f1 = (float) (i % 30000L) / 30000.0F;
        float f2 = (float) Math.sin(i / 20000.0F) + 1.0F;
        float f3 = (float) Math.cos(i / 20000.0F) + 1.0F;
        Matrix4f matrix4f = (new Matrix4f()).translation(1 + f2, 1 + f3, 0.0F);
        matrix4f.scale(in);
        return matrix4f;
    }
}
