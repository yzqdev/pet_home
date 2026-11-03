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

/** 高亮方块实体模型。已从 Tabula 体系迁移到原版 ModelPart/LayerDefinition，静态几何无动画。 */
public class HighlightedBlockModel extends EntityModel<RecallBallEntity> {
    private final ModelPart root;

    public HighlightedBlockModel(ModelPart root) {
        this.root = root;
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
    public void setupAnim(RecallBallEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        this.resetPose();
    }

    private void resetPose() {
        this.root.getAllParts().forEach(ModelPart::resetPose);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        // 旧 Tabula 渲染忽略调用点传入的 color（顶点固定白色）；原版 Cube.compile 会把 color 写入顶点，
        // 调用点的 1（不透明黑）会把模型染黑——固定 -1 与旧行为一致
        this.root.render(poseStack, buffer, packedLight, packedOverlay, -1);
    }
}
