package com.github.yzqdev.pethome.mixin;

 import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.yzqdev.pethome.util.PetBedDrop;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;

@Mixin(  EntityMaid.class)
public abstract class EntityMaidMixin {

    @Inject(
            method = "dropEquipment",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pethome$dropEquipment(
            CallbackInfo ci
    ) {
        if (PetBedDrop.hasPetBedPos((LivingEntity) (Object) this)) {
            ci.cancel();
        }
    }
}