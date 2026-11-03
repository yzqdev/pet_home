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
import net.minecraft.world.entity.LivingEntity;

/**
 * 烈焰防护条模型。已从 Tabula 体系迁移到原版 {@link ModelPart}/{@link LayerDefinition}，数值与旧版一致。
 *
 * <p>1.20 渲染走即时 renderToBuffer：多条烈焰条在渲染循环内先 {@link #animateBar} 再 {@link #renderToBuffer}
 * （立即写顶点），动画-渲染成对进行，无共享实例姿态覆盖问题。
 */
public class BlazingBarModel extends EntityModel<LivingEntity> {
    private final ModelPart root;
    private final ModelPart bar;

    public BlazingBarModel(ModelPart root) {
        this.root = root;
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

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        this.resetPose();
    }

    private void resetPose() {
        this.root.getAllParts().forEach(ModelPart::resetPose);
    }

    public void animateBar(float rotY) {
        this.resetPose();
        this.bar.yRot -= (float) Math.toRadians(rotY);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay);
    }
}
