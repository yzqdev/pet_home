package com.github.yzqdev.pethome.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 26.1: {@link Model} 的 renderToBuffer 已 final 且以 ModelPart 为根。
 * 本工程自带的 Tabula 风格模型体系（{@link BasicModelPart}）不走 ModelPart，
 * 因此根节点用空的 ModelPart 占位，渲染走 {@link #renderParts}。
 */
public abstract class BasicEntityModel extends Model<Unit> {
    public int textureWidth = 64;
    public int textureHeight = 32;

    protected BasicEntityModel() {
        this(RenderTypes::entityCutout);
    }

    protected BasicEntityModel(Function<Identifier, net.minecraft.client.renderer.rendertype.RenderType> p_102613_) {
        super(new ModelPart(List.of(), Map.of()), p_102613_);
    }

    /**
     * 替代 1.21 中被覆写的 renderToBuffer（26.1 中该方法已 final）。
     */
    public void renderParts(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        this.parts().forEach((pa) -> {
            pa.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        });
    }

    public abstract Iterable<BasicModelPart> parts();
}
