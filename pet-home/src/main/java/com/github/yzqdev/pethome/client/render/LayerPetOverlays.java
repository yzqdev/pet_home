package com.github.yzqdev.pethome.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import com.github.yzqdev.pethome.util.TameableUtils;

public class LayerPetOverlays extends RenderLayer {

  private final RenderLayerParent parent;

  public LayerPetOverlays(RenderLayerParent parent) {
    super(parent);
    this.parent = parent;
  }

  @Override
  public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Entity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
    if (!(entity instanceof LivingEntity living) || !TameableUtils.isTamed(entity)) {
      return;
    }
    PetOverlayRenderer.renderOverlays(living, matrixStackIn, bufferIn, packedLightIn, partialTicks, color -> {
      matrixStackIn.pushPose();
      this.getParentModel().renderToBuffer(matrixStackIn, bufferIn.getBuffer(PHRenderTypes.IFRAME_GLINT), packedLightIn, LivingEntityRenderer.getOverlayCoords(living, 0));
      matrixStackIn.popPose();
    });
  }
}
