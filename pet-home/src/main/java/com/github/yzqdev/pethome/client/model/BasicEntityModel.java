package com.github.yzqdev.pethome.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * 自研模型体系（Tabula 风格 {@code BasicModelPart}）与原版 {@code EntityModel} 的桥接基类： {@link #renderToBuffer}
 * 改为遍历 {@link #parts()} 渲染，子类只需提供部件集合。
 */
public abstract class BasicEntityModel<T extends Entity> extends EntityModel<T> {
  public int textureWidth = 64;
  public int textureHeight = 32;

  protected BasicEntityModel() {
    this(RenderType::entityCutoutNoCull);
  }

  protected BasicEntityModel(Function<ResourceLocation, RenderType> renderType) {
    super(renderType);
  }

  @Override
  public void renderToBuffer(
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight,
      int packedOverlay,
      int color) {
    this.parts()
        .forEach(
            (pa) -> {
              pa.render(poseStack, vertexConsumer, packedLight, packedOverlay);
            });
  }

  public abstract Iterable<BasicModelPart> parts();

  @Override
  public abstract void setupAnim(
      T entity,
      float limbSwing,
      float limbSwingAmount,
      float ageInTicks,
      float netHeadYaw,
      float headPitch);

  @Override
  public void prepareMobModel(
      T entity, float limbSwing, float limbSwingAmount, float partialTick) {}
}
