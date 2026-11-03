package com.github.yzqdev.pethome.client.render.state;

import com.github.yzqdev.pethome.server.entity.ChainLightningEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** 连锁闪电节点渲染状态：携带实体引用与 partialTick（闪电几何按实体实时计算）。 */
public class ChainLightningRenderState extends EntityRenderState {
    public ChainLightningEntity lightningEntity;
    public float partialTick;
}
