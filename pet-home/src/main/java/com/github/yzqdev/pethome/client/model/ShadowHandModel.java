package com.github.yzqdev.pethome.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/**
 * 暗影之手模型。已从 Tabula 体系（AdvancedEntityModel/AdvancedModelBox）迁移到原版 {@link ModelPart}/{@link
 * LayerDefinition}：几何与默认姿态数值逐字保留，动画公式经 {@link ModelPartAnim} 逐式移植，行为与旧 {@code ShadowHandModel} 一致。
 *
 * <p>多只手共用同一模型实例：26.3 的 submitModel 路径在延迟绘制时才调 {@link #setupAnim(ShadowHandState)}
 * 并渲染（ModelFeatureRenderer.prepareModel）， 因此每只手 submit 各自的 {@link ShadowHandState} 即可携带独立姿态。
 */
public class ShadowHandModel extends Model<ShadowHandModel.ShadowHandState> {

  public record ShadowHandState(
      float punch, int handIndex, int shadowHandCount, float ageInTicks) {}

  private final ModelPart palm;
  private final ModelPart finger1;
  private final ModelPart finger2;
  private final ModelPart finger3;
  private final ModelPart thumb;

  public ShadowHandModel(ModelPart root) {
    super(root, RenderTypes::entityCutout);
    this.palm = root.getChild("palm");
    this.finger1 = this.palm.getChild("finger1");
    this.finger2 = this.palm.getChild("finger2");
    this.finger3 = this.palm.getChild("finger3");
    this.thumb = this.palm.getChild("thumb");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();

    PartDefinition palm =
        root.addOrReplaceChild(
            "palm",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.5F, -2.0F, 6, 3, 5),
            PartPose.offsetAndRotation(0.0F, 19.0F, 0.0F, -0.5672F, 0.0F, 0.0F));

    palm.addOrReplaceChild(
        "finger1",
        CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -2.0F, -6.0F, 2, 2, 7),
        PartPose.offsetAndRotation(3.0F, 0.0F, -1.0F, 0.1745F, -0.3491F, 0.0F));

    palm.addOrReplaceChild(
        "finger2",
        CubeListBuilder.create().texOffs(14, 10).addBox(-1.0F, -2.0F, -6.7F, 2, 2, 7),
        PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.1745F, 0.0F, 0.0F));

    palm.addOrReplaceChild(
        "finger3",
        CubeListBuilder.create().texOffs(14, 19).addBox(0.0F, -2.0F, -6.0F, 2, 2, 7),
        PartPose.offsetAndRotation(-3.0F, 0.0F, -1.0F, 0.1745F, 0.3491F, 0.0F));

    palm.addOrReplaceChild(
        "thumb",
        CubeListBuilder.create().texOffs(1, 17).addBox(-2.0F, -2.0F, -4.0F, 2, 2, 4),
        PartPose.offsetAndRotation(3.0F, 0.0F, 3.0F, 0.1745F, -0.8727F, 0.0F));

    return LayerDefinition.create(mesh, 32, 32);
  }

  @Override
  public void setupAnim(ShadowHandState state) {
    this.animateShadowHand(state);
  }

  public void animateShadowHand(ShadowHandState state) {
    float punch = state.punch();
    int handIndex = state.handIndex();
    int shadowHandCount = state.shadowHandCount();
    float ageInTicks = state.ageInTicks();

    this.resetPose();
    boolean left = handIndex >= shadowHandCount / 2F;
    float leftMod = left ? -1 : 1;
    punch = Mth.clamp(punch, 0, 0.25F) * 4F;
    float still = 1F - punch;
    if (left) {
      this.thumb.setPos(-1.5F, 0.0F, 1.5F);
      ModelPartAnim.setRotationAngle(this.thumb, 0.1745F, 0.8727F, 0.0F);
    } else {
      this.thumb.setPos(3.0F, 0.0F, 3.0F);
      ModelPartAnim.setRotationAngle(this.thumb, 0.1745F, -0.8727F, 0.0F);
    }
    ModelPartAnim.walk(
        this.palm, leftMod * 0.2F, 0.1F, false, handIndex - 1F, 0F, ageInTicks, still);
    ModelPartAnim.walk(
        this.finger1, leftMod * 0.2F, 0.2F, false, 1F + handIndex, 0.2F, ageInTicks, still);
    ModelPartAnim.walk(
        this.finger2, leftMod * 0.2F, 0.2F, false, 3F + handIndex, 0.2F, ageInTicks, still);
    ModelPartAnim.walk(
        this.finger3, leftMod * 0.2F, 0.2F, false, 5F + handIndex, 0.2F, ageInTicks, still);
    ModelPartAnim.swing(
        this.thumb,
        leftMod * 0.2F,
        0.2F,
        false,
        5F + handIndex,
        leftMod * -0.2F,
        ageInTicks,
        still);

    ModelPartAnim.progressRotationPrev(this.finger1, punch, (float) Math.toRadians(90), 0, 0, 1F);
    ModelPartAnim.progressRotationPrev(this.finger2, punch, (float) Math.toRadians(90), 0, 0, 1F);
    ModelPartAnim.progressRotationPrev(this.finger3, punch, (float) Math.toRadians(90), 0, 0, 1F);
    ModelPartAnim.progressRotation(this.palm, punch, 0, 0, 0, 1F);
    ModelPartAnim.progressRotationPrev(
        this.thumb, punch, (float) Math.toRadians(90), 0, (float) Math.toRadians(30) * leftMod, 1F);
    ModelPartAnim.progressPositionPrev(this.finger1, punch, 0, -2, 0.5F, 1F);
    ModelPartAnim.progressPositionPrev(this.finger2, punch, 0, -2, 0.5F, 1F);
    ModelPartAnim.progressPositionPrev(this.finger3, punch, 0, -2, 0.5F, 1F);
    ModelPartAnim.progressPositionPrev(this.thumb, punch, leftMod * 2, -2, -1F, 1F);
    ModelPartAnim.progressPositionPrev(this.palm, punch, 0, 1, 1, 1F);
  }
}
