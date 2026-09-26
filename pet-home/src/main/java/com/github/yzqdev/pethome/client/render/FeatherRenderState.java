package com.github.yzqdev.pethome.client.render;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.world.entity.player.Player;

public class FeatherRenderState extends FishingHookRenderState {
    public Player player = null;
    public float partialTicks;

}
