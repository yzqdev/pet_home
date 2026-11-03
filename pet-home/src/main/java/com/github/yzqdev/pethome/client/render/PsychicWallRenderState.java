package com.github.yzqdev.pethome.client.render;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;

/** 念力墙（PsychicWallEntity）的渲染状态：朝向 + 随展开/消散缩放的墙宽。 */
public class PsychicWallRenderState extends EntityRenderState {
    public Direction direction;
    // blockWidth * growth：墙在展开/消散期随 tick 缩放的尺寸（边框 UV 固定用 1）
    public float size;
}
