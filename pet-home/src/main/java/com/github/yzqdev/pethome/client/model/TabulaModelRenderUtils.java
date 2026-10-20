package com.github.yzqdev.pethome.client.model;

import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

public class TabulaModelRenderUtils {

    @OnlyIn(Dist.CLIENT)
    static class PositionTextureVertex {
        public final Vector3f position;
        public final float textureU;
        public final float textureV;

        public PositionTextureVertex(float x, float y, float z, float textureU, float textureV) {
            this(new Vector3f(x, y, z), textureU, textureV);
        }

        public PositionTextureVertex setTextureUV(float textureU, float textureV) {
            return new PositionTextureVertex(this.position, textureU, textureV);
        }

        public PositionTextureVertex(Vector3f position, float textureU, float textureV) {
            this.position = position;
            this.textureU = textureU;
            this.textureV = textureV;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class TexturedQuad {
        public final PositionTextureVertex[] vertexPositions;
        public final Vector3f normal;

        public TexturedQuad(PositionTextureVertex[] vertices, float u1, float v1, float u2, float v2, float textureWidth, float textureHeight, boolean mirror, Direction direction) {
            this.vertexPositions = vertices;
            float uPadding = 0.0F / textureWidth;
            float vPadding = 0.0F / textureHeight;
            vertices[0] = vertices[0].setTextureUV(u2 / textureWidth - uPadding, v1 / textureHeight + vPadding);
            vertices[1] = vertices[1].setTextureUV(u1 / textureWidth + uPadding, v1 / textureHeight + vPadding);
            vertices[2] = vertices[2].setTextureUV(u1 / textureWidth + uPadding, v2 / textureHeight - vPadding);
            vertices[3] = vertices[3].setTextureUV(u2 / textureWidth - uPadding, v2 / textureHeight - vPadding);
            if (mirror) {
                int vertexCount = vertices.length;

                for (int i = 0; i < vertexCount / 2; ++i) {
                    PositionTextureVertex swapped = vertices[i];
                    vertices[i] = vertices[vertexCount - 1 - i];
                    vertices[vertexCount - 1 - i] = swapped;
                }
            }

            this.normal = direction.step();
            if (mirror) {
                this.normal.mul(-1.0F, 1.0F, 1.0F);
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ModelBox {
        public final TexturedQuad[] quads;
        public final float posX1;
        public final float posY1;
        public final float posZ1;
        public final float posX2;
        public final float posY2;
        public final float posZ2;

        public ModelBox(int texOffX, int texOffY, float x, float y, float z, float width, float height, float depth, float growX, float growY, float growZ, boolean mirror, float textureWidth, float textureHeight) {
            this.posX1 = x;
            this.posY1 = y;
            this.posZ1 = z;
            this.posX2 = x + width;
            this.posY2 = y + height;
            this.posZ2 = z + depth;
            this.quads = new TexturedQuad[6];
            float maxX = x + width;
            float maxY = y + height;
            float maxZ = z + depth;
            x -= growX;
            y -= growY;
            z -= growZ;
            maxX += growX;
            maxY += growY;
            maxZ += growZ;
            if (mirror) {
                float swapped = maxX;
                maxX = x;
                x = swapped;
            }

            PositionTextureVertex vertex000 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
            PositionTextureVertex vertex100 = new PositionTextureVertex(maxX, y, z, 0.0F, 8.0F);
            PositionTextureVertex vertex110 = new PositionTextureVertex(maxX, maxY, z, 8.0F, 8.0F);
            PositionTextureVertex vertex010 = new PositionTextureVertex(x, maxY, z, 8.0F, 0.0F);
            PositionTextureVertex vertex001 = new PositionTextureVertex(x, y, maxZ, 0.0F, 0.0F);
            PositionTextureVertex vertex101 = new PositionTextureVertex(maxX, y, maxZ, 0.0F, 8.0F);
            PositionTextureVertex vertex111 = new PositionTextureVertex(maxX, maxY, maxZ, 8.0F, 8.0F);
            PositionTextureVertex vertex011 = new PositionTextureVertex(x, maxY, maxZ, 8.0F, 0.0F);
            float u0 = (float) texOffX;
            float u1 = (float) texOffX + depth;
            float u2 = (float) texOffX + depth + width;
            float u3 = (float) texOffX + depth + width + width;
            float u4 = (float) texOffX + depth + width + depth;
            float u5 = (float) texOffX + depth + width + depth + width;
            float v0 = (float) texOffY;
            float v1 = (float) texOffY + depth;
            float v2 = (float) texOffY + depth + height;
            this.quads[2] = new TexturedQuad(new PositionTextureVertex[]{vertex101, vertex001, vertex000, vertex100}, u1, v0, u2, v1, textureWidth, textureHeight, mirror, Direction.DOWN);
            this.quads[3] = new TexturedQuad(new PositionTextureVertex[]{vertex110, vertex010, vertex011, vertex111}, u2, v1, u3, v0, textureWidth, textureHeight, mirror, Direction.UP);
            this.quads[1] = new TexturedQuad(new PositionTextureVertex[]{vertex000, vertex001, vertex011, vertex010}, u0, v1, u1, v2, textureWidth, textureHeight, mirror, Direction.WEST);
            this.quads[4] = new TexturedQuad(new PositionTextureVertex[]{vertex100, vertex000, vertex010, vertex110}, u1, v1, u2, v2, textureWidth, textureHeight, mirror, Direction.NORTH);
            this.quads[0] = new TexturedQuad(new PositionTextureVertex[]{vertex101, vertex100, vertex110, vertex111}, u2, v1, u4, v2, textureWidth, textureHeight, mirror, Direction.EAST);
            this.quads[5] = new TexturedQuad(new PositionTextureVertex[]{vertex001, vertex101, vertex111, vertex011}, u4, v1, u5, v2, textureWidth, textureHeight, mirror, Direction.SOUTH);
        }
    }
}
