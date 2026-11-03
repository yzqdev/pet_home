package com.github.yzqdev.pethome.client.model;

import net.minecraft.core.Direction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector3f;

/** Tabula 模型渲染工具（自 Citadel 模型库移植）：ModelBox / TexturedQuad / PositionTextureVertex 的复制实现，供 AdvancedModelBox 绘制立方体。 */
public class TabulaModelRenderUtils {

    @Environment(EnvType.CLIENT)
    static class PositionTextureVertex {
        public final Vector3f position;
        public final float textureU;
        public final float textureV;

        public PositionTextureVertex(float x, float y, float z, float texU, float texV) {
            this(new Vector3f(x, y, z), texU, texV);
        }

        public TabulaModelRenderUtils.PositionTextureVertex setTextureUV(float texU, float texV) {
            return new TabulaModelRenderUtils.PositionTextureVertex(this.position, texU, texV);
        }

        public PositionTextureVertex(Vector3f position, float texU, float texV) {
            this.position = position;
            this.textureU = texU;
            this.textureV = texV;
        }
    }

    @Environment(EnvType.CLIENT)
    static class TexturedQuad {
        public final TabulaModelRenderUtils.PositionTextureVertex[] vertexPositions;
        public final Vector3f normal;

        public TexturedQuad(PositionTextureVertex[] vertices, float u1, float v1, float u2, float v2, float texWidth, float texHeight, boolean mirrorIn, Direction direction) {
            this.vertexPositions = vertices;
            float uOffset = 0.0F / texWidth;
            float vOffset = 0.0F / texHeight;
            vertices[0] = vertices[0].setTextureUV(u2 / texWidth - uOffset, v1 / texHeight + vOffset);
            vertices[1] = vertices[1].setTextureUV(u1 / texWidth + uOffset, v1 / texHeight + vOffset);
            vertices[2] = vertices[2].setTextureUV(u1 / texWidth + uOffset, v2 / texHeight - vOffset);
            vertices[3] = vertices[3].setTextureUV(u2 / texWidth - uOffset, v2 / texHeight - vOffset);
            if (mirrorIn) {
                int vertexCount = vertices.length;

                for(int vertexIndex = 0; vertexIndex < vertexCount / 2; ++vertexIndex) {
                    TabulaModelRenderUtils.PositionTextureVertex swapped = vertices[vertexIndex];
                    vertices[vertexIndex] = vertices[vertexCount - 1 - vertexIndex];
                    vertices[vertexCount - 1 - vertexIndex] = swapped;
                }
            }

            this.normal = direction.step();
            if (mirrorIn) {
                this.normal.mul(-1.0F, 1.0F, 1.0F);
            }

        }
    }

    @Environment(EnvType.CLIENT)
    public static class ModelBox {
        public final TabulaModelRenderUtils.TexturedQuad[] quads;
        public final float posX1;
        public final float posY1;
        public final float posZ1;
        public final float posX2;
        public final float posY2;
        public final float posZ2;

        public ModelBox(int texOffX, int texOffY, float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ, boolean mirrorIn, float texWidth, float texHeight) {
            this.posX1 = x;
            this.posY1 = y;
            this.posZ1 = z;
            this.posX2 = x + width;
            this.posY2 = y + height;
            this.posZ2 = z + depth;
            this.quads = new TabulaModelRenderUtils.TexturedQuad[6];
            float maxX = x + width;
            float maxY = y + height;
            float maxZ = z + depth;
            x -= deltaX;
            y -= deltaY;
            z -= deltaZ;
            maxX += deltaX;
            maxY += deltaY;
            maxZ += deltaZ;
            if (mirrorIn) {
                float swapped = maxX;
                maxX = x;
                x = swapped;
            }

            TabulaModelRenderUtils.PositionTextureVertex v000 = new TabulaModelRenderUtils.PositionTextureVertex(x, y, z, 0.0F, 0.0F);
            TabulaModelRenderUtils.PositionTextureVertex v100 = new TabulaModelRenderUtils.PositionTextureVertex(maxX, y, z, 0.0F, 8.0F);
            TabulaModelRenderUtils.PositionTextureVertex v110 = new TabulaModelRenderUtils.PositionTextureVertex(maxX, maxY, z, 8.0F, 8.0F);
            TabulaModelRenderUtils.PositionTextureVertex v010 = new TabulaModelRenderUtils.PositionTextureVertex(x, maxY, z, 8.0F, 0.0F);
            TabulaModelRenderUtils.PositionTextureVertex v001 = new TabulaModelRenderUtils.PositionTextureVertex(x, y, maxZ, 0.0F, 0.0F);
            TabulaModelRenderUtils.PositionTextureVertex v101 = new TabulaModelRenderUtils.PositionTextureVertex(maxX, y, maxZ, 0.0F, 8.0F);
            TabulaModelRenderUtils.PositionTextureVertex v111 = new TabulaModelRenderUtils.PositionTextureVertex(maxX, maxY, maxZ, 8.0F, 8.0F);
            TabulaModelRenderUtils.PositionTextureVertex v011 = new TabulaModelRenderUtils.PositionTextureVertex(x, maxY, maxZ, 8.0F, 0.0F);
            float u0 = (float)texOffX;
            float u1 = (float)texOffX + depth;
            float u2 = (float)texOffX + depth + width;
            float u3 = (float)texOffX + depth + width + width;
            float u4 = (float)texOffX + depth + width + depth;
            float u5 = (float)texOffX + depth + width + depth + width;
            float v0 = (float)texOffY;
            float v1 = (float)texOffY + depth;
            float v2 = (float)texOffY + depth + height;
            this.quads[2] = new TabulaModelRenderUtils.TexturedQuad(new TabulaModelRenderUtils.PositionTextureVertex[]{v101, v001, v000, v100}, u1, v0, u2, v1, texWidth, texHeight, mirrorIn, Direction.DOWN);
            this.quads[3] = new TabulaModelRenderUtils.TexturedQuad(new TabulaModelRenderUtils.PositionTextureVertex[]{v110, v010, v011, v111}, u2, v1, u3, v0, texWidth, texHeight, mirrorIn, Direction.UP);
            this.quads[1] = new TabulaModelRenderUtils.TexturedQuad(new TabulaModelRenderUtils.PositionTextureVertex[]{v000, v001, v011, v010}, u0, v1, u1, v2, texWidth, texHeight, mirrorIn, Direction.WEST);
            this.quads[4] = new TabulaModelRenderUtils.TexturedQuad(new TabulaModelRenderUtils.PositionTextureVertex[]{v100, v000, v010, v110}, u1, v1, u2, v2, texWidth, texHeight, mirrorIn, Direction.NORTH);
            this.quads[0] = new TabulaModelRenderUtils.TexturedQuad(new TabulaModelRenderUtils.PositionTextureVertex[]{v101, v100, v110, v111}, u2, v1, u4, v2, texWidth, texHeight, mirrorIn, Direction.EAST);
            this.quads[5] = new TabulaModelRenderUtils.TexturedQuad(new TabulaModelRenderUtils.PositionTextureVertex[]{v001, v101, v111, v011}, u4, v1, u5, v2, texWidth, texHeight, mirrorIn, Direction.SOUTH);
        }
    }
}