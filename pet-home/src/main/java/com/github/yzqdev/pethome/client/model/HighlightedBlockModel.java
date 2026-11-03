package com.github.yzqdev.pethome.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Unit;

/** 高亮方块实体模型。已从 Tabula 体系迁移到原版 ModelPart/LayerDefinition，静态几何无动画。 渲染走 submitModel（Unit 状态）。 */
public class HighlightedBlockModel extends Model<Unit> {

  public HighlightedBlockModel(ModelPart root) {
    super(root, RenderTypes::entityCutout);
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();
    root.addOrReplaceChild(
        "box",
        CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16),
        PartPose.ZERO);
    return LayerDefinition.create(mesh, 64, 64);
  }

  @Override
  public void setupAnim(Unit state) {}
}
