package com.github.yzqdev.pethome.client.render;


import com.github.yzqdev.pethome.server.entity.TameableUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** 宠物特效渲染层薄壳（挂在原版生物渲染器上）：绘制核心在 {@link PetOverlayRenderer}，与 GeckoLib 路径共用。 */
public class LayerPetOverlays extends RenderLayer {

    private final RenderLayerParent parent;

    public LayerPetOverlays(RenderLayerParent parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Entity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof LivingEntity living) || !TameableUtils.couldBeTamed(entity)) {
            return;
        }
        PetOverlayRenderer.renderOverlays(living, matrixStackIn, bufferIn, packedLightIn, partialTicks, (texture, r, g, b, a) -> {
            matrixStackIn.pushPose();
            if (texture == null) {
                this.getParentModel().renderToBuffer(matrixStackIn, bufferIn.getBuffer(PHRenderTypes.IFRAME_GLINT), packedLightIn, LivingEntityRenderer.getOverlayCoords(living, 0), r, g, b, a);
            } else {
                com.mojang.datafixers.util.Pair<Integer, Integer> dim = TextureSizer.getTextureWidth(texture);
                this.getParentModel().renderToBuffer(matrixStackIn, bufferIn.getBuffer(PHRenderTypes.getZombieOverlay(texture, dim.getFirst(), dim.getSecond())), packedLightIn, LivingEntityRenderer.getOverlayCoords(living, 0), r, g, b, a);
            }
            matrixStackIn.popPose();
        });
    }
}
