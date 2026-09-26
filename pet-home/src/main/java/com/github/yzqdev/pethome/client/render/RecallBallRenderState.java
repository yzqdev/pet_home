package com.github.yzqdev.pethome.client.render;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class RecallBallRenderState extends EntityRenderState {
    public float yRot;
    public float yRotO;
    public float xRot;
    public float xRotO;
    public int tickCount;
    public float open;
    public boolean finished;
    public float partialTick;
}