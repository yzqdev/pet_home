package com.github.yzqdev.pet_home.client.render;

import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.client.model.HighlightedBlockModel;
import com.github.yzqdev.pet_home.server.entity.HighlightedBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderHighlightedBlock extends EntityRenderer<HighlightedBlockEntity> {



    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "textures/highlighted_block.png");
    private final HighlightedBlockModel highlightedBlockModel = new HighlightedBlockModel();

    public RenderHighlightedBlock(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    public void render(HighlightedBlockEntity entity, float f1, float f2, PoseStack stack, MultiBufferSource source, int packedLight) {
        stack.pushPose();
        stack.translate(0, 0.5F, 0);
        VertexConsumer vertexconsumer = source.getBuffer(RenderType.outline(this.getTextureLocation(entity)));
        this.highlightedBlockModel.renderToBuffer(stack,vertexconsumer,packedLight, OverlayTexture.NO_OVERLAY, 1  );
//        this.highlightedBlockModel.renderToBuffer(stack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        stack.popPose();
    }

    public ResourceLocation getTextureLocation(HighlightedBlockEntity block) {
        return TEXTURE;
    }
}