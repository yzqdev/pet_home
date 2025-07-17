package com.github.yzqdev.pet_home.mixin;


import com.github.yzqdev.pet_home.datagen.ModEnchantments;
import com.github.yzqdev.pet_home.util.TameableUtils;
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
            method = {"Lnet/minecraft/world/entity/Mob;pickUpItem(Lnet/minecraft/world/entity/item/ItemEntity;)V"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void di_pickUpItem(ItemEntity item, CallbackInfo ci) {
        if(TameableUtils.isTamed(this) && TameableUtils.hasEnchant(this, ModEnchantments.LINKED_INVENTORY)){
            Entity owner = TameableUtils.getOwnerOf(this);
            if(owner instanceof Player){
                ci.cancel();
                if(((Player) owner).addItem(item.getItem())){
                    item.discard();
                }else{
                    item.copyPosition(owner);
                }
            }

        }
    }


}
