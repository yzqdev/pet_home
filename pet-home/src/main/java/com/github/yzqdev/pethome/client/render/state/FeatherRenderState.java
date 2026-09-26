package com.github.yzqdev.pethome.client.render.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FeatherRenderState extends EntityRenderState {
    public Player player;
    public HumanoidArm mainArm = HumanoidArm.RIGHT;
    public ItemStack mainHandItem = ItemStack.EMPTY;
    public float attackAnim;
    public float yBodyRotO;
    public float yBodyRot;
    public double xo;
    public double yo;
    public double zo;
    public double playerX;
    public double playerY;
    public double playerZ;
    public float eyeHeight;
    public boolean isCrouching;
    public boolean isPlayer;
    public double entityXo;
    public double entityYo;
    public double entityZo;
    public double entityX;
    public double entityY;
    public double entityZ;
    public float partialTick;
}