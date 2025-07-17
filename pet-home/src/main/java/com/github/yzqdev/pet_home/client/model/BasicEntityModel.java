package com.github.yzqdev.pet_home.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

public abstract class BasicEntityModel<T extends Entity> extends EntityModel<T> {
    public int textureWidth = 64;
    public int textureHeight = 32;

    protected BasicEntityModel() {
        this(RenderType::entityCutoutNoCull);
    }

    protected BasicEntityModel(Function<ResourceLocation, RenderType> p_102613_) {
        super(p_102613_);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.parts().forEach((pa)->{
            pa.render(poseStack,vertexConsumer,packedLight,packedOverlay );
        });
    }



    public abstract Iterable<BasicModelPart> parts();

    @Override
    public abstract void setupAnim(T p_102618_, float p_102619_, float p_102620_, float p_102621_, float p_102622_, float p_102623_);

    @Override
    public void prepareMobModel(T p_102614_, float p_102615_, float p_102616_, float p_102617_) {
    }
}