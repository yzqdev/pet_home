package com.github.yzqdev.pethome.server.event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public final class EntityInteractContext {

    private final Player player;
    private final InteractionHand hand;
    private final Level level;
    private final Entity target;
    private final ItemStack itemStack;

    private boolean canceled;
    private InteractionResult cancellationResult = InteractionResult.SUCCESS;

    public EntityInteractContext(Player player, InteractionHand hand, Level level, Entity target, ItemStack itemStack) {
        this.player = player;
        this.hand = hand;
        this.level = level;
        this.target = target;
        this.itemStack = itemStack;
    }

    public Player getEntity() {
        return player;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public Level getLevel() {
        return level;
    }

    public Entity getTarget() {
        return target;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public InteractionResult getCancellationResult() {
        return cancellationResult;
    }

    public void setCancellationResult(InteractionResult result) {
        this.cancellationResult = result;
    }

    /** 未被处理时返回 {@link InteractionResult#PASS}，交给原版与其他模组继续处理 */
    public InteractionResult result() {
        return canceled ? cancellationResult : InteractionResult.PASS;
    }
}
