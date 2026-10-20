package com.github.yzqdev.pethome.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;


/**
 * An enhanced RendererModel
 *
 * @author gegy1000
 * @since 1.0.0
 */
@OnlyIn(Dist.CLIENT)
public class AdvancedModelBox extends BasicModelPart {
    public float defaultRotationX, defaultRotationY, defaultRotationZ;
    public float defaultOffsetX, defaultOffsetY, defaultOffsetZ;
    public float defaultPositionX, defaultPositionY, defaultPositionZ;
    public float scaleX = 1.0F, scaleY = 1.0F, scaleZ = 1.0F;
    public int textureOffsetX, textureOffsetY;
    public boolean scaleChildren;
    private AdvancedEntityModel model;
    private AdvancedModelBox parent;
    private int displayList;
    private boolean compiled;
    public ObjectList<TabulaModelRenderUtils.ModelBox> cubeList;
    public ObjectList<BasicModelPart> childModels;
    private float textureWidth;
    private float textureHeight;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public String boxName = "";

    public AdvancedModelBox(AdvancedEntityModel model, String name) {
        super(model);
        this.textureWidth = model.texWidth;
        this.textureHeight = model.texHeight;
        this.model = model;
        this.cubeList = new ObjectArrayList();
        this.childModels = new ObjectArrayList();
        this.boxName = name;
    }

    public AdvancedModelBox(AdvancedEntityModel model) {
        this(model, null);
        this.textureWidth = model.texWidth;
        this.textureHeight = model.texHeight;
        this.cubeList = new ObjectArrayList();
        this.childModels = new ObjectArrayList();
    }

    public AdvancedModelBox(AdvancedEntityModel model, int textureOffsetX, int textureOffsetY) {
        this(model);
        this.textureWidth = model.texWidth;
        this.textureHeight = model.texHeight;
        this.setTextureOffset(textureOffsetX, textureOffsetY);
        this.cubeList = new ObjectArrayList();
        this.childModels = new ObjectArrayList();
    }

    public BasicModelPart setTexSize(int textureWidthIn, int textureHeightIn) {
        this.textureWidth = (float) textureWidthIn;
        this.textureHeight = (float) textureHeightIn;
        return this;
    }

    public BasicModelPart addBox(String partName, float x, float y, float z, int width, int height, int depth, float delta, int texX, int texY) {
        this.setTextureOffset(texX, texY);
        this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, (float) width, (float) height, (float) depth, delta, delta, delta, this.mirror, false);
        return this;
    }

    public BasicModelPart addBox(float x, float y, float z, float width, float height, float depth) {
        this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, 0.0F, 0.0F, 0.0F, this.mirror, false);
        return this;
    }

    public BasicModelPart addBox(float x, float y, float z, float width, float height, float depth, boolean mirrorIn) {
        this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, 0.0F, 0.0F, 0.0F, mirrorIn, false);
        return this;
    }

    public void addBox(float x, float y, float z, float width, float height, float depth, float delta) {
        this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, delta, delta, delta, this.mirror, false);
    }

    public void addBox(float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ) {
        this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, deltaX, deltaY, deltaZ, this.mirror, false);
    }

    public void addBox(float x, float y, float z, float width, float height, float depth, float delta, boolean mirrorIn) {
        this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, delta, delta, delta, mirrorIn, false);
    }

    private void addBox(int texOffX, int texOffY, float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ, boolean mirrorIn, boolean disableMirroring) {
        this.cubeList.add(new TabulaModelRenderUtils.ModelBox(texOffX, texOffY, x, y, z, width, height, depth, deltaX, deltaY, deltaZ, mirrorIn, this.textureWidth, this.textureHeight));
    }


    /**
     * If true, when using setScale, the children of this model part will be scaled as well as just this part. If false, just this part will be scaled.
     *
     * @param scaleChildren true if this parent should scale the children
     * @since 1.1.0
     */
    public void setShouldScaleChildren(boolean scaleChildren) {
        this.scaleChildren = scaleChildren;
    }

    /**
     * Sets the scale for this AdvancedModelBox to be rendered at. (Performs a call to GLStateManager.scale()).
     *
     * @param scaleX the x scale
     * @param scaleY the y scale
     * @param scaleZ the z scale
     * @since 1.1.0
     */
    public void setScale(float scaleX, float scaleY, float scaleZ) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public void setScaleZ(float scaleZ) {
        this.scaleZ = scaleZ;
    }

    /**
     * Sets this RendererModel's default pose to the current pose.
     */
    public void updateDefaultPose() {
        this.defaultRotationX = this.rotateAngleX;
        this.defaultRotationY = this.rotateAngleY;
        this.defaultRotationZ = this.rotateAngleZ;

        // this.defaultOffsetX = this.offsetX;
        // this.defaultOffsetY = this.offsetY;
        // this.defaultOffsetZ = this.offsetZ;

        this.defaultPositionX = this.rotationPointX;
        this.defaultPositionY = this.rotationPointY;
        this.defaultPositionZ = this.rotationPointZ;
    }

    public void setPos(float xIn, float yIn, float zIn) {
        this.rotationPointX = xIn;
        this.rotationPointY = yIn;
        this.rotationPointZ = zIn;
    }

    /**
     * Sets the current pose to the previously set default pose.
     */
    public void resetToDefaultPose() {
        this.rotateAngleX = this.defaultRotationX;
        this.rotateAngleY = this.defaultRotationY;
        this.rotateAngleZ = this.defaultRotationZ;

        // this.offsetX = this.defaultOffsetX;
        // this.offsetY = this.defaultOffsetY;
        // this.offsetZ = this.defaultOffsetZ;

        this.rotationPointX = this.defaultPositionX;
        this.rotationPointY = this.defaultPositionY;
        this.rotationPointZ = this.defaultPositionZ;
    }

    @Override
    public void addChild(BasicModelPart child) {
        super.addChild(child);
        this.childModels.add(child);
        if (child instanceof AdvancedModelBox) {
            AdvancedModelBox advancedChild = (AdvancedModelBox) child;
            advancedChild.setParent(this);
        }
    }

    /**
     * @return the parent of this box
     */
    public AdvancedModelBox getParent() {
        return this.parent;
    }

    /**
     * Sets the parent of this box
     *
     * @param parent the new parent
     */
    public void setParent(AdvancedModelBox parent) {
        this.parent = parent;
    }

    /**
     * Post renders this box with all its parents
     *
     * @param scale the render scale
     */
    public void parentedPostRender(float scale) {
        if (this.parent != null) {
            this.parent.parentedPostRender(scale);
        }
        // this.postRender(scale);
    }

    /**
     * Renders this box with all it's parents
     *
     * @param scale the render scale
     */
    public void renderWithParents(float scale) {
        if (this.parent != null) {
            this.parent.renderWithParents(scale);
        }
        //this.render(scale);
    }

    public void translateAndRotate(PoseStack matrixStackIn) {
        matrixStackIn.translate((double) (this.rotationPointX / 16.0F), (double) (this.rotationPointY / 16.0F), (double) (this.rotationPointZ / 16.0F));
        if (this.rotateAngleZ != 0.0F) {
            matrixStackIn.mulPose(Axis.ZP.rotation(this.rotateAngleZ));
        }

        if (this.rotateAngleY != 0.0F) {
            matrixStackIn.mulPose(Axis.YP.rotation(this.rotateAngleY));
        }

        if (this.rotateAngleX != 0.0F) {
            matrixStackIn.mulPose(Axis.XP.rotation(this.rotateAngleX));
        }

        matrixStackIn.scale(this.scaleX, this.scaleY, this.scaleZ);
    }

    @Override
    public void render(PoseStack poseStack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if (this.showModel) {
            if (!this.cubeList.isEmpty() || !this.childModels.isEmpty()) {
                poseStack.pushPose();
                this.translateAndRotate(poseStack);
                this.doRender(poseStack.last(), bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                ObjectListIterator childIterator = this.childModels.iterator();
                if (!scaleChildren) {
                    poseStack.scale(1F / Math.max(this.scaleX, 0.0001F), 1F / Math.max(this.scaleY, 0.0001F), 1F / Math.max(this.scaleZ, 0.0001F));
                }
                while (childIterator.hasNext()) {
                    BasicModelPart child = (BasicModelPart) childIterator.next();
                    child.render(poseStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                }

                poseStack.popPose();
            }
        }
    }

    private void doRender(PoseStack.Pose pose, VertexConsumer vertexConsumer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        Matrix4f matrix4f = pose.pose();
        Matrix3f normal = pose.normal();
        ObjectListIterator cubeIterator = this.cubeList.iterator();

        while (cubeIterator.hasNext()) {
            TabulaModelRenderUtils.ModelBox cube = (TabulaModelRenderUtils.ModelBox) cubeIterator.next();
            TabulaModelRenderUtils.TexturedQuad[] quads = cube.quads;
            int quadCount = quads.length;

            for (int quadIndex = 0; quadIndex < quadCount; ++quadIndex) {
                TabulaModelRenderUtils.TexturedQuad quad = quads[quadIndex];
                Vector3f transformedNormal = new Vector3f(quad.normal);
                transformedNormal.mul(normal);
                float normalX = transformedNormal.x();
                float normalY = transformedNormal.y();
                float normalZ = transformedNormal.z();

                for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex) {
                    TabulaModelRenderUtils.PositionTextureVertex positionTextureVertex = quad.vertexPositions[vertexIndex];
                    float x = positionTextureVertex.position.x() / 16.0F;
                    float y = positionTextureVertex.position.y() / 16.0F;
                    float z = positionTextureVertex.position.z() / 16.0F;
                    Vector4f transformedPosition = new Vector4f(x, y, z, 1.0F);
                    transformedPosition.mul(matrix4f);
                    vertexConsumer.addVertex(transformedPosition.x(), transformedPosition.y(), transformedPosition.z()).setColor(red, green, blue, alpha).setNormal(normalX, normalY, normalZ).setLight(packedLightIn).setOverlay(packedOverlayIn).setUv(positionTextureVertex.textureU, positionTextureVertex.textureV);


                }
            }
        }
    }

    public AdvancedEntityModel getModel() {
        return this.model;
    }

    private float calculateRotation(float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
        float movementScale = this.model.getMovementScale();
        float rotation = (Mth.cos(f * (speed * movementScale) + offset) * (degree * movementScale) * f1) + (weight * f1);
        return invert ? -rotation : rotation;
    }

    /**
     * Rotates this box back and forth (rotateAngleX). Useful for arms and legs.
     *
     * @param speed      is how fast the model runs
     * @param degree     is how far the box will rotate;
     * @param invert     will invert the rotation
     * @param offset     will offset the timing of the model
     * @param weight     will make the model favor one direction more based on how fast the mob is moving
     * @param walk       is the walked distance
     * @param walkAmount is the walk speed
     */
    public void walk(float speed, float degree, boolean invert, float offset, float weight, float walk, float walkAmount) {
        this.rotateAngleX += this.calculateRotation(speed, degree, invert, offset, weight, walk, walkAmount);
    }

    /**
     * Rotates this box up and down (rotateAngleZ). Useful for wing and ears.
     *
     * @param speed      is how fast the model runs
     * @param degree     is how far the box will rotate;
     * @param invert     will invert the rotation
     * @param offset     will offset the timing of the model
     * @param weight     will make the model favor one direction more based on how fast the mob is moving
     * @param flap       is the flapped distance
     * @param flapAmount is the flap speed
     */
    public void flap(float speed, float degree, boolean invert, float offset, float weight, float flap, float flapAmount) {
        this.rotateAngleZ += this.calculateRotation(speed, degree, invert, offset, weight, flap, flapAmount);
    }

    /**
     * Rotates this box side to side (rotateAngleY).
     *
     * @param speed       is how fast the model runs
     * @param degree      is how far the box will rotate;
     * @param invert      will invert the rotation
     * @param offset      will offset the timing of the model
     * @param weight      will make the model favor one direction more based on how fast the mob is moving
     * @param swing       is the swung distance
     * @param swingAmount is the swing speed
     */
    public void swing(float speed, float degree, boolean invert, float offset, float weight, float swing, float swingAmount) {
        this.rotateAngleY += this.calculateRotation(speed, degree, invert, offset, weight, swing, swingAmount);
    }

    /**
     * Moves this box up and down (rotationPointY). Useful for bodies.
     *
     * @param speed  is how fast the model runs;
     * @param degree is how far the box will move;
     * @param bounce will make the box bounce;
     * @param f      is the walked distance;
     * @param f1     is the walk speed.
     */
    public void bob(float speed, float degree, boolean bounce, float f, float f1) {
        float movementScale = this.model.getMovementScale();
        degree *= movementScale;
        speed *= movementScale;
        float bob = (float) (Math.sin(f * speed) * f1 * degree - f1 * degree);
        if (bounce) {
            bob = (float) -Math.abs((Math.sin(f * speed) * f1 * degree));
        }
        this.rotationPointY += bob;
    }

    @Override
    public AdvancedModelBox setTextureOffset(int textureOffsetX, int textureOffsetY) {
        this.textureOffsetX = textureOffsetX;
        this.textureOffsetY = textureOffsetY;
        return this;
    }

    public void transitionTo(AdvancedModelBox to, float timer, float maxTime) {
        this.rotateAngleX += ((to.rotateAngleX - this.rotateAngleX) / maxTime) * timer;
        this.rotateAngleY += ((to.rotateAngleY - this.rotateAngleY) / maxTime) * timer;
        this.rotateAngleZ += ((to.rotateAngleZ - this.rotateAngleZ) / maxTime) * timer;

        this.rotationPointX += ((to.rotationPointX - this.rotationPointX) / maxTime) * timer;
        this.rotationPointY += ((to.rotationPointY - this.rotationPointY) / maxTime) * timer;
        this.rotationPointZ += ((to.rotationPointZ - this.rotationPointZ) / maxTime) * timer;

        this.offsetX += ((to.offsetX - this.offsetX) / maxTime) * timer;
        this.offsetY += ((to.offsetY - this.offsetY) / maxTime) * timer;
        this.offsetZ += ((to.offsetZ - this.offsetZ) / maxTime) * timer;
    }


}