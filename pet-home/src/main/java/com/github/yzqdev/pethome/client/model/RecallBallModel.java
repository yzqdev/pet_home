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

/**
 * 召回球（宠物盒）模型。已从 Tabula 体系迁移到原版 {@link ModelPart}/{@link LayerDefinition}， box 数值/旋转点与旧版逐字一致。开合动画
 * {@link #animateBall(float, boolean)} 公式保留； bottom 的开合缩放对子件 top 同样生效（原版 ModelPart 的 scale 在
 * children 渲染前施加，等价于 旧 scaleChildren=true）。渲染走 submitModel（Unit 状态）。
 */
public class RecallBallModel extends Model<Unit> {

  private final ModelPart bottom;
  private final ModelPart top;

  public RecallBallModel(ModelPart root) {
    super(root, RenderTypes::entityCutout);
    this.bottom = root.getChild("bottom");
    this.top = this.bottom.getChild("top");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();
    PartDefinition bottom =
        root.addOrReplaceChild(
            "bottom",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -6.0F, -4.5F, 9, 6, 9),
            PartPose.offset(0.0F, 24.0F, 0.0F));
    bottom.addOrReplaceChild(
        "top",
        CubeListBuilder.create().texOffs(0, 15).addBox(-4.5F, -3.0F, -9.0F, 9, 3, 9),
        PartPose.offset(0.0F, -6.0F, 4.5F));
    return LayerDefinition.create(mesh, 64, 64);
  }

  public void animateBall(float open, boolean finished) {
    this.resetPose();
    this.top.xRot -= open * (float) Math.PI * 0.75F;
    this.bottom.xRot += open * (float) Math.PI * 0.25F;
    if (finished) {
      this.bottom.xScale = open;
      this.bottom.yScale = open * open;
      this.bottom.zScale = open;
    } else {
      this.bottom.xScale = 1;
      this.bottom.yScale = 1;
      this.bottom.zScale = 1;
    }
  }

  @Override
  public void setupAnim(Unit state) {}
}
