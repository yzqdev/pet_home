package com.github.yzqdev.pethome.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * 烈焰防护条模型。已从 Tabula 体系迁移到原版 {@link ModelPart}/{@link LayerDefinition}，数值与旧版一致。
 *
 * <p>多条烈焰条共用同一模型实例：26.3 的 submitModel 路径在延迟绘制时才调 {@link #setupAnim(Float)}
 * 并渲染（ModelFeatureRenderer.prepareModel），因此每条 submit 各自的 rotY 即可携带独立姿态（与 ShadowHandModel 同一模式）。
 */
public class BlazingBarModel extends Model<Float> {

  private final ModelPart bar;

  public BlazingBarModel(ModelPart root) {
    super(root, RenderTypes::entityCutout);
    this.bar = root.getChild("bar");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();
    root.addOrReplaceChild(
        "bar",
        CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -4.0F, -1.0F, 2, 8, 2),
        PartPose.offset(0.0F, 14.0F, 0.0F));
    return LayerDefinition.create(mesh, 64, 32);
  }

  public void animateBar(float rotY) {
    this.resetPose();
    this.bar.yRot -= (float) Math.toRadians(rotY);
  }

  @Override
  public void setupAnim(Float rotY) {
    this.animateBar(rotY);
  }
}
