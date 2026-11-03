package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.server.entity.ChainLightningEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Vector4f;

/**
 * 连锁闪电（CHAIN_LIGHTNING 附魔）渲染器：闪电本体是“从实体 A 到实体 B”的连接线，渲染时 平移 {@code -pos} 把世界坐标归零后在原位重建 Bolt 数据，用共享的
 * {@link LightningRender} 绘制。
 */
public class ChainLightningRender extends EntityRenderer<ChainLightningEntity> {

  private LightningRender lightningRender = new LightningRender();

  /** 蓝色系闪电外观参数（分叉幅度/粗细/透明度等）。 */
  private LightningBoltData.BoltRenderInfo lightningBoltData =
      new LightningBoltData.BoltRenderInfo(
          1.3F, 0.15F, 0.5F, 0.25F, new Vector4f(0.1F, 0.3F, 0.5F, 0.5F), 0.45F);

  public ChainLightningRender(EntityRendererProvider.Context renderManagerIn) {
    super(renderManagerIn);
  }

  /** 视锥剔除扩展：闪电要连同起点实体一起判断（AABB minmax），否则镜头在中间时会整条消失。 */
  @Override
  public boolean shouldRender(
      ChainLightningEntity entity, Frustum frustum, double x, double y, double z) {
    Entity next = entity.getFromEntity();
    return next != null && frustum.isVisible(entity.getBoundingBox().minmax(next.getBoundingBox()))
        || super.shouldRender(entity, frustum, x, y, z);
  }

  @Override
  public void render(
      ChainLightningEntity entity,
      float yaw,
      float partialTicks,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int light) {
    super.render(entity, yaw, partialTicks, poseStack, buffer, light);
    poseStack.pushPose();
    Entity from = entity.getFromEntity();
    float x = (float) Mth.lerp(partialTicks, entity.xo, entity.getX());
    float y = (float) Mth.lerp(partialTicks, entity.yo, entity.getY());
    float z = (float) Mth.lerp(partialTicks, entity.zo, entity.getZ());
    if (from != null) {
      LightningBoltData bolt =
          new LightningBoltData(lightningBoltData, from.getEyePosition(), entity.position(), 5)
              .size(0.1F)
              .lifespan(2)
              .spawn(LightningBoltData.SpawnFunction.NO_DELAY);
      lightningRender.update(null, bolt, partialTicks);
      poseStack.translate(-x, -y, -z);
      lightningRender.render(partialTicks, poseStack, buffer);
    }
    poseStack.popPose();
  }

  @Override
  public ResourceLocation getTextureLocation(ChainLightningEntity entity) {
    return null;
  }
}
