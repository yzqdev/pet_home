package com.github.yzqdev.pet_home.mixin;


import com.github.yzqdev.pet_home.datagen.ModEnchantments;
import com.github.yzqdev.pet_home.util.TameableUtils;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AgeableMob.class)
public class AgeableMobMixin extends PathfinderMob {

    protected AgeableMobMixin(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/AgeableMob;isBaby()Z"},
            cancellable = true
    )
    private void di_isBaby(CallbackInfoReturnable<Boolean> cir){
        if(TameableUtils.isTamed(this) && TameableUtils.hasEnchant(this, ModEnchantments.IMMATURITY_CURSE)){
            cir.setReturnValue(true);
        }
    }
}
