package com.github.yzqdev.pethome.client.model;

import com.github.yzqdev.pethome.server.entity.RecallBallEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * 召回球（宠物盒）模型。已从 Tabula 体系迁移到原版 {@link ModelPart}/{@link LayerDefinition}， box 数值/旋转点与旧版逐字一致。开合动画
 * {@link #animateBall(RecallBallEntity, float)} 公式保留； bottom 的开合缩放对子件 top 同样生效（原版 ModelPart 的 scale 在
 * children 渲染前施加，等价于 旧 scaleChildren=true，已核实 1.21 translateAndRotate 顺序）。
 */
public class RecallBallModel extends EntityModel<RecallBallEntity> {
    private final ModelPart root;
    private final ModelPart bottom;
    private final ModelPart top;

    public RecallBallModel(ModelPart root) {
        this.root = root;
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

    @Override
    public void setupAnim(RecallBallEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        this.resetPose();
    }

    private void resetPose() {
        this.root.getAllParts().forEach(ModelPart::resetPose);
    }

    public void animateBall(RecallBallEntity entity, float partialTick) {
        this.resetPose();
        float open = entity.getOpenProgress(partialTick);
        this.top.xRot -= open * (float) Math.PI * 0.75F;
        this.bottom.xRot += open * (float) Math.PI * 0.25F;
        if (entity.isFinished()) {
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
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        // 旧 Tabula 渲染忽略调用点传入的 color（顶点固定白色）；原版 Cube.compile 会把 color 写入顶点，
        // 调用点的 1（不透明黑）会把模型染黑——固定 -1 与旧行为一致
        this.root.render(poseStack, buffer, packedLight, packedOverlay, -1);
    }
}
