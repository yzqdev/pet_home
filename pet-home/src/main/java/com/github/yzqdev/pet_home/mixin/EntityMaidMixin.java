package com.github.yzqdev.pet_home.mixin;

import com.github.yzqdev.pet_home.util.PetBedDrop;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityMaid.class)
public abstract class EntityMaidMixin {
    @Inject(at = {@At("HEAD")}, method = {"dropEquipment"}, cancellable = true)
    public void drop(CallbackInfo ci) {
        if (PetBedDrop.hasPetBedPos((EntityMaid) ((Object) (this)))) {
            ci.cancel();
        }
    }

}
