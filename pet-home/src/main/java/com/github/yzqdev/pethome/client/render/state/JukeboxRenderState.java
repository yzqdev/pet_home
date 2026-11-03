package com.github.yzqdev.pethome.client.render.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.item.ItemStack;

/** disc_jockey 唱片机渲染状态（自 1.20 移植）：当前唱片 + 旋转插值角度。 */
public class JukeboxRenderState extends EntityRenderState {
    public ItemStack record = ItemStack.EMPTY;
    public float yRotO;
    public float yRot;
    public float partialTick;
}
