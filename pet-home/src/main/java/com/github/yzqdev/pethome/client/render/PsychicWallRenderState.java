package com.github.yzqdev.pethome.client.render;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;

public class PsychicWallRenderState extends EntityRenderState {
    public Direction direction;
    // blockWidth * growth：墙在展开/消散期随 tick 缩放的尺寸（边框 UV 固定用 1）
    public float size;
}
