package com.github.yzqdev.pethome.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.Builder;
import lombok.Value;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4f;

/**
 * 封装顶点绘制所需的参数
 */
@Value
@Builder(toBuilder = true)
public class VertexData {
    public PoseStack.Pose pose;

    public float offsetX, offsetY, offsetZ;
    public float textureX, textureY;
    public float alpha;
    public int packedLight;

    public float normalX, normalY, normalZ;

    /**
     * 流式绘制方法：将当前对象存储的所有属性写入 VertexConsumer
     * 模仿原本 drawVertex 的逻辑
     */
    public void draw(VertexConsumer consumer) {
        // 如果 matrix 为空，则尝试从 pose 中获取（兼容性处理）


        consumer.addVertex(this.pose, this.offsetX, this.offsetY, this.offsetZ)
                .setColor(255, 255, 255, (int)(this.alpha * 255)) // 转换为 0-255 整数
                .setUv(this.textureX, this.textureY)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setUv2(this.packedLight, this.packedLight)
                .setNormal(this.pose, this.normalX,  this.normalY,this.normalZ);
    }

    /**
     * 静态辅助方法：如果你不想先实例化对象，可以直接通过类似原本的参数列表调用
     * 但更推荐使用 VertexData.builder()...build().draw(consumer)
     */
    public static void drawImmediate(Matrix4f matrix, PoseStack.Pose pose, VertexConsumer consumer,
                                     float x, float y, float z, float u, float v,
                                     float nX, float nY, float nZ, int light) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setUv2(light, light)
                .setNormal(pose, nX, nZ, nY);
    }
}