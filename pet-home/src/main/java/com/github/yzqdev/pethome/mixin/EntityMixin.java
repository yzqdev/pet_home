package com.github.yzqdev.pethome.mixin;


import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.entity.PsychicWallEntity;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code Entity} 通用注入（附魔行为开关的主要落点之一）：
 * {@code fireImmune}（烈焰保护）、{@code isPushedByFluid}/{@code dismountsUnderwater}
 * （两栖：不被水流推动、水下可骑乘）、{@code isAlliedTo}（同主人宠物互为友军，
 * 供友军伤害保护判定）、{@code canCollideWith}（念力墙不与同队实体碰撞）。
 */
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
        if (TameableUtils.isTamed(us) && TameableUtils.hasEnchant((LivingEntity) us, PHEnchantments.FIREPROOF)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = {"isPushedByFluid()Z"},

            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_pushedByWater(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LivingEntity && TameableUtils.isTamed((LivingEntity) (Object) this) && TameableUtils.hasEnchant((LivingEntity) (Object) this, PHEnchantments.AMPHIBIOUS)) {
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
        if ((Object) this instanceof LivingEntity && TameableUtils.isTamed((LivingEntity) (Object) this) && TameableUtils.hasEnchant((LivingEntity) (Object) this, PHEnchantments.AMPHIBIOUS)) {
            cir.setReturnValue(true);
        }
    }

    /** muffled（安静，自 1.20 移植）：移动不发出声响游戏事件（脚步声等，MovementEmission.NONE）。 */
    @Inject(
            method = {"getMovementEmission()Lnet/minecraft/world/entity/Entity$MovementEmission;"},
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void di_getMovementEmission(CallbackInfoReturnable<Entity.MovementEmission> cir) {
        if ((Object) this instanceof LivingEntity living && TameableUtils.isTamed(living) && TameableUtils.hasEnchant(living, PHEnchantments.MUFFLED)) {
            cir.setReturnValue(Entity.MovementEmission.NONE);
        }
    }

    /** muffled（安静）：彻底阻断该实体的 gameEvent 传播（脚步/摔落等，sculk 系收不到）。 */
    @Inject(
            method = {"gameEvent(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/Entity;)V"},
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void di_gameEvent(Holder<GameEvent> event, Entity sourceEntity, CallbackInfo ci) {
        if ((Object) this instanceof LivingEntity living && TameableUtils.isTamed(living) && TameableUtils.hasEnchant(living, PHEnchantments.MUFFLED)) {
            ci.cancel();
        }
    }


}
