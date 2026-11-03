package com.github.yzqdev.pethome.client.model;

import com.github.yzqdev.pethome.server.entity.RecallBallEntity;
import com.google.common.collect.ImmutableList;

/** 矿脉嗅探高亮方块的模型：一个 16×16×16 的整方块盒子，渲染时按矿脉颜色半透明罩住目标方块。 */
public class HighlightedBlockModel extends AdvancedEntityModel<RecallBallEntity> {
  private final AdvancedModelBox box;

  public HighlightedBlockModel() {
    texWidth = 64;
    texHeight = 64;

    box = new AdvancedModelBox(this);
    box.setPos(0.0F, 0.0F, 0.0F);
    box.setTextureOffset(0, 0).addBox(-8, -8, -8, 16.0F, 16.0F, 16.0F, 0.0F, false);

    this.updateDefaultPose();
  }

  @Override
  public Iterable<BasicModelPart> parts() {
    return ImmutableList.of(box);
  }

  @Override
  public void setupAnim(
      RecallBallEntity recallBallEntity,
      float limbSwing,
      float limbSwingAmount,
      float age,
      float yaw,
      float pitch) {
    this.resetToDefaultPose();
  }

  @Override
  public Iterable<AdvancedModelBox> getAllParts() {
    return ImmutableList.of(box);
  }
}
