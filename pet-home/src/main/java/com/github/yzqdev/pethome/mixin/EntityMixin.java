package com.github.yzqdev.pethome.mixin;


import com.github.yzqdev.pethome.datagen.ModEnchantments;
import com.github.yzqdev.pethome.server.entity.PsychicWallEntity;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(
            method = {"fireImmune()Z"},

            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void di_isFireImmune(CallbackInfoReturnable<Boolean> cir) {
        Entity us = (Entity) ((Object) this);
        if (TameableUtils.isTamed(us) && TameableUtils.hasEnchant((LivingEntity) us, ModEnchantments.FIREPROOF)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = {"isPushedByFluid()Z"},

            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_pushedByWater(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LivingEntity && TameableUtils.isTamed((LivingEntity) (Object) this) && TameableUtils.hasEnchant((LivingEntity) (Object) this, ModEnchantments.AMPHIBIOUS)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = {"isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z"},

            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_isAlliedTo(Entity other, CallbackInfoReturnable<Boolean> cir) {
        if (TameableUtils.isTamed(other) && TameableUtils.isTamed((Entity) (Object) this) && TameableUtils.hasSameOwnerAs((LivingEntity) other, (Entity) (Object) this)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = {"canCollideWith(Lnet/minecraft/world/entity/Entity;)Z"},

            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_canCollideWith(Entity other, CallbackInfoReturnable<Boolean> cir) {
        if (other instanceof PsychicWallEntity && ((PsychicWallEntity) other).isSameTeam((Entity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = {"dismountsUnderwater()Z"},

            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_rideableInWater(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LivingEntity && TameableUtils.isTamed((LivingEntity) (Object) this) && TameableUtils.hasEnchant((LivingEntity) (Object) this, ModEnchantments.AMPHIBIOUS)) {
            cir.setReturnValue(true);
        }
    }


}
