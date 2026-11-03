package com.github.yzqdev.pethome.client.model;


import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.LivingEntity;

/** 烈焰庇护（Blazing Bar 火焰条）模型：单根竖条部件，随附魔触发实体的朝向旋转（见 {@link #animateBar}）。 */
public class BlazingBarModel extends AdvancedEntityModel<LivingEntity> {
    private final AdvancedModelBox bar;

    public BlazingBarModel() {
        texWidth = 64;
        texHeight = 32;
        bar = new AdvancedModelBox(this);
        bar.setRotationPoint(0.0F, 14.0F, 0.0F);
        bar.setTextureOffset(0, 16).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, false);
        this.updateDefaultPose();
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(bar);
    }

    @Override
    public Iterable<BasicModelPart> parts() {
        return ImmutableList.of(bar);
    }

    @Override
    public void setupAnim(LivingEntity recallBallEntity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        this.resetToDefaultPose();
    }

    /** 按实体 yaw 旋转竖条（渲染器每帧调用）。 */
    public void animateBar(float rotY) {
        this.resetToDefaultPose();
        this.bar.rotateAngleY -= (float)Math.toRadians(rotY);
    }
}