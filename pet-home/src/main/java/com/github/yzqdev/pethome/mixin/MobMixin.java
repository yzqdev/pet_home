package com.github.yzqdev.pethome.mixin;


import com.github.yzqdev.pethome.datagen.ModEnchantments;
import com.github.yzqdev.pethome.util.TameableUtils;
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

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

    protected MobMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(
            method = {"pickUpItem(Lnet/minecraft/world/entity/item/ItemEntity;)V"},

            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void di_pickUpItem(ItemEntity item, CallbackInfo ci) {
        if (TameableUtils.isTamed(this) && TameableUtils.hasEnchant(this, ModEnchantments.LINKED_INVENTORY)) {
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


}
