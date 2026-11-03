package com.github.yzqdev.pethome.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;

/**
 * Tabula 风格动画函数在原版 {@link ModelPart} 上的等价实现（自 {@code AdvancedEntityModel}/{@code AdvancedModelBox}
 * 逐公式移植，movementScale 恒为 1）。供从 Tabula 体系迁出的模型使用，行为与原实现一致。
 */
public final class ModelPartAnim {

    private ModelPartAnim() {}

    /**
     * Rotates this box back and forth (xRot). Useful for arms and legs.
     *
     * @param speed is how fast the model runs
     * @param degree is how far the box will rotate
     * @param invert will invert the rotation
     * @param offset will offset the timing of the model
     * @param weight will make the model favor one direction more based on how fast the mob is moving
     * @param walk is the walked distance
     * @param walkAmount is the walk speed
     */
    public static void walk(ModelPart part, float speed, float degree, boolean invert, float offset, float weight, float walk, float walkAmount) {
        part.xRot += calculateRotation(speed, degree, invert, offset, weight, walk, walkAmount);
    }

    /**
     * Rotates this box side to side (yRot).
     *
     * @param swing is the swung distance
     * @param swingAmount is the swing speed
     */
    public static void swing(ModelPart part, float speed, float degree, boolean invert, float offset, float weight, float swing, float swingAmount) {
        part.yRot += calculateRotation(speed, degree, invert, offset, weight, swing, swingAmount);
    }

    /**
     * Rotates this box up and down (zRot). Useful for wings and ears.
     *
     * @param flap is the flapped distance
     * @param flapAmount is the flap speed
     */
    public static void flap(ModelPart part, float speed, float degree, boolean invert, float offset, float weight, float flap, float flapAmount) {
        part.zRot += calculateRotation(speed, degree, invert, offset, weight, flap, flapAmount);
    }

    private static float calculateRotation(float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
        float rotation = (Mth.cos(f * speed + offset) * degree * f1) + (weight * f1);
        return invert ? -rotation : rotation;
    }

    /** 从默认姿态向目标插值叠加（对应 Tabula 的 progressRotation，default 取 initialPose；1.21 PartPose 是公有字段非 record）。 */
    public static void progressRotation(ModelPart part, float progress, float rotX, float rotY, float rotZ, float divisor) {
        PartPose initial = part.getInitialPose();
        part.xRot += progress * (rotX - initial.xRot) / divisor;
        part.yRot += progress * (rotY - initial.yRot) / divisor;
        part.zRot += progress * (rotZ - initial.zRot) / divisor;
    }

    /** 从默认姿态向目标插值叠加位移（对应 Tabula 的 progressPosition）。 */
    public static void progressPosition(ModelPart part, float progress, float x, float y, float z, float divisor) {
        PartPose initial = part.getInitialPose();
        part.x += progress * (x - initial.x) / divisor;
        part.y += progress * (y - initial.y) / divisor;
        part.z += progress * (z - initial.z) / divisor;
    }

    /** 向当前姿态叠加旋转增量（对应 Tabula 的 progressRotationPrev）。 */
    public static void progressRotationPrev(ModelPart part, float progress, float rotX, float rotY, float rotZ, float divisor) {
        part.xRot += progress * rotX / divisor;
        part.yRot += progress * rotY / divisor;
        part.zRot += progress * rotZ / divisor;
    }

    /** 向当前姿态叠加位移增量（对应 Tabula 的 progressPositionPrev，单位 1/16 像素）。 */
    public static void progressPositionPrev(ModelPart part, float progress, float x, float y, float z, float divisor) {
        part.x += progress * x / divisor;
        part.y += progress * y / divisor;
        part.z += progress * z / divisor;
    }

    public static void setRotationAngle(ModelPart part, float x, float y, float z) {
        part.setRotation(x, y, z);
    }
}
