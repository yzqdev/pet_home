package com.github.yzqdev.pethome.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * 暗影之手模型。已从 Tabula 体系（AdvancedEntityModel/AdvancedModelBox）迁移到原版 {@link ModelPart}/{@link
 * LayerDefinition}：几何与默认姿态数值逐字保留，动画公式经 {@link ModelPartAnim} 逐式移植，行为与旧 {@code ShadowHandModel} 一致。
 *
 * <p>1.20 渲染走即时 renderToBuffer：多只手在每帧渲染循环内先 {@link #animateShadowHand} 再
 * {@link #renderToBuffer}（立即写顶点），动画-渲染成对进行，无共享实例姿态覆盖问题。
 */
public class ShadowHandModel extends EntityModel<LivingEntity> {
    private final ModelPart palm;
    private final ModelPart finger1;
    private final ModelPart finger2;
    private final ModelPart finger3;
    private final ModelPart thumb;
    private final ModelPart root;

    public ShadowHandModel(ModelPart root) {
        this.root = root;
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
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        this.resetPose();
    }

    private void resetPose() {
        this.root.getAllParts().forEach(ModelPart::resetPose);
    }

    public void animateShadowHand(float punch, int handIndex, int shadowHandCount, float ageInTicks) {
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
        ModelPartAnim.walk(this.palm, leftMod * 0.2F, 0.1F, false, handIndex - 1F, 0F, ageInTicks, still);
        ModelPartAnim.walk(this.finger1, leftMod * 0.2F, 0.2F, false, 1F + handIndex, 0.2F, ageInTicks, still);
        ModelPartAnim.walk(this.finger2, leftMod * 0.2F, 0.2F, false, 3F + handIndex, 0.2F, ageInTicks, still);
        ModelPartAnim.walk(this.finger3, leftMod * 0.2F, 0.2F, false, 5F + handIndex, 0.2F, ageInTicks, still);
        ModelPartAnim.swing(this.thumb, leftMod * 0.2F, 0.2F, false, 5F + handIndex, leftMod * -0.2F, ageInTicks, still);

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

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay);
    }
}
