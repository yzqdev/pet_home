package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.PsychicWallEntity;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(
            method = {"Lnet/minecraft/world/entity/Entity;fireImmune()Z"},
            remap = true,
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void di_isFireImmune(CallbackInfoReturnable<Boolean> cir) {
        Entity us = (Entity)((Object)this);
        if(TameableUtils.isTamed(us) && TameableUtils.hasEnchant((LivingEntity) us, DIEnchantmentRegistry.FIREPROOF)){
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/Entity;isPushedByFluid()Z"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_pushedByWater(CallbackInfoReturnable<Boolean> cir) {
        if((Object)this instanceof LivingEntity && TameableUtils.isTamed((LivingEntity)(Object)this) && TameableUtils.hasEnchant((LivingEntity)(Object)this, DIEnchantmentRegistry.AMPHIBIOUS)){
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/Entity;isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_isAlliedTo(Entity other, CallbackInfoReturnable<Boolean> cir) {
        if(TameableUtils.isTamed(other) && TameableUtils.isTamed((Entity)(Object)this) && TameableUtils.hasSameOwnerAs((LivingEntity) other, (Entity)(Object)this)){
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/Entity;canCollideWith(Lnet/minecraft/world/entity/Entity;)Z"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_canCollideWith(Entity other, CallbackInfoReturnable<Boolean> cir) {
        if(other instanceof PsychicWallEntity && ((PsychicWallEntity)other).isSameTeam((Entity)(Object)this)){
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/Entity;dismountsUnderwater()Z"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_rideableInWater(CallbackInfoReturnable<Boolean> cir) {
        if((Object)this instanceof LivingEntity && TameableUtils.isTamed((LivingEntity)(Object)this) && TameableUtils.hasEnchant((LivingEntity)(Object)this, DIEnchantmentRegistry.AMPHIBIOUS)){
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/Entity;getMovementEmission()Lnet/minecraft/world/entity/Entity$MovementEmission;"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_getMovementEmission(CallbackInfoReturnable<Entity.MovementEmission> cir) {
        if((Object)this instanceof LivingEntity && TameableUtils.isTamed((LivingEntity)(Object)this) && TameableUtils.hasEnchant((LivingEntity)(Object)this, DIEnchantmentRegistry.MUFFLED)){
            cir.setReturnValue(Entity.MovementEmission.NONE);
        }
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/Entity;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/world/entity/Entity;)V"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_gameEvent(GameEvent event, Entity entity, CallbackInfo ci) {
        if((Object)this instanceof LivingEntity && TameableUtils.isTamed((LivingEntity)(Object)this) && TameableUtils.hasEnchant((LivingEntity)(Object)this, DIEnchantmentRegistry.MUFFLED)){
            ci.cancel();
        }

    }
}
