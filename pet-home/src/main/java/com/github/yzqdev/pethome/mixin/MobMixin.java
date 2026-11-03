package com.github.yzqdev.pethome.mixin;


import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code Mob.pickUpItem} 注入：携带"连锁背包"（{@code LINKED_INVENTORY}）附魔的宠物
 * 捡起掉落物时不再塞进自己的物品栏，改为直接转入主人背包（主人背包满则保持原样）。
 */
@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

    protected MobMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(
            // 26.1: pickUpItem 新增 ServerLevel 前参
            method = {"pickUpItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/item/ItemEntity;)V"},

            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void di_pickUpItem(ServerLevel level, ItemEntity item, CallbackInfo ci) {
        if (TameableUtils.isTamed(this) && TameableUtils.hasEnchant(this, PHEnchantments.LINKED_INVENTORY)) {
            Entity owner = TameableUtils.getOwnerOf(this);
            if (owner instanceof Player player) {
                ci.cancel();
                if (player.addItem(item.getItem())) {
                    item.discard();
                } else {
                    item.copyPosition(player);
                }
            }

        }
    }

    /** muffled（安静，自 1.20 移植）：取消驯服宠物的环境音（idle 叫声）。 */
    @Inject(method = "playAmbientSound()V", at = @At(value = "HEAD"), cancellable = true)
    private void di_playAmbientSound(CallbackInfo ci) {
        if (TameableUtils.isTamed(this) && TameableUtils.hasEnchant(this, PHEnchantments.MUFFLED)) {
            ci.cancel();
        }
    }


}
