package com.github.yzqdev.pethome.client.render;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** 召回球渲染状态：朝向、tick 计数、开盖进度 {@code open} 与是否释放完成 {@code finished}。 */
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