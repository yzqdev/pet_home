package com.github.yzqdev.pethome.client.render;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;

public class PsychicWallRenderState extends EntityRenderState {
    public int lifespan;
    public int tickCount;
    public int blockWidth;
    public float partialTicks;
    public Direction wallDirection;
}
