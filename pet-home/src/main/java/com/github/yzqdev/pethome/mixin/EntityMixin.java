package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
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

/**
 * 实体行为谓词覆盖组（按附魔/驯服状态改写原版判定）：FIREPROOF 免火、AMPHIBIOUS 不被流体推动且
 * 可水下骑乘脱离、同主人结盟、念力墙可碰撞、MUFFLED 免疫游戏事件（幽匿尖啸体）等。
 */
@Mixin(Entity.class)
public class EntityMixin {

    /** 注入 {@code Entity#fireImmune} 头部：FIREPROOF（耐火）宠物免火焰伤害。 */
    @Inject(
            method = {"fireImmune()Z"},
            remap = true,
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void di_isFireImmune(CallbackInfoReturnable<Boolean> cir) {
        Entity us = (Entity)((Object)this);
        if(TameableUtils.isTamed(us) && TameableUtils.hasEnchant((LivingEntity) us, PHEnchantmentRegistry.FIREPROOF)){
            cir.setReturnValue(true);
        }
    }

    /** 注入 {@code Entity#isPushedByFluid} 头部：AMPHIBIOUS 宠物不被水流推动。 */
    @Inject(
            method = {"isPushedByFluid()Z"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_pushedByWater(CallbackInfoReturnable<Boolean> cir) {
        if((Object)this instanceof LivingEntity && TameableUtils.isTamed((LivingEntity)(Object)this) && TameableUtils.hasEnchant((LivingEntity)(Object)this, PHEnchantmentRegistry.AMPHIBIOUS)){
            cir.setReturnValue(false);
        }
    }

    /** 注入 {@code Entity#isAlliedTo} 头部：驯服实体之间同主人互判盟友（通用基类级，覆盖非 TamableAnimal 的 Mixin 扩展物种）。 */
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

    /** 注入 {@code Entity#canCollideWith} 头部：同队的念力墙对本实体不产生碰撞，让友军可穿墙、敌人被挡。 */
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

    /** 注入 {@code Entity#dismountsUnderwater} 头部：AMPHIBIOUS 宠物在水下骑乘时不会强制脱载。 */
    @Inject(
            method = {"Lnet/minecraft/world/entity/Entity;dismountsUnderwater()Z"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_rideableInWater(CallbackInfoReturnable<Boolean> cir) {
        if((Object)this instanceof LivingEntity && TameableUtils.isTamed((LivingEntity)(Object)this) && TameableUtils.hasEnchant((LivingEntity)(Object)this, PHEnchantmentRegistry.AMPHIBIOUS)){
            cir.setReturnValue(true);
        }
    }

    /** 注入 {@code Entity#getMovementEmission} 头部：MUFFLED 宠物移动不发出游戏事件（幽匿尖啸体/潜声传感器无法感知）。 */
    @Inject(
            method = {"Lnet/minecraft/world/entity/Entity;getMovementEmission()Lnet/minecraft/world/entity/Entity$MovementEmission;"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_getMovementEmission(CallbackInfoReturnable<Entity.MovementEmission> cir) {
        if((Object)this instanceof LivingEntity && TameableUtils.isTamed((LivingEntity)(Object)this) && TameableUtils.hasEnchant((LivingEntity)(Object)this, PHEnchantmentRegistry.MUFFLED)){
            cir.setReturnValue(Entity.MovementEmission.NONE);
        }
    }

    /** 注入 {@code Entity#gameEvent} 头部：MUFFLED 宠物彻底不发出游戏事件。 */
    @Inject(
            method = {"Lnet/minecraft/world/entity/Entity;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/world/entity/Entity;)V"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_gameEvent(GameEvent event, Entity entity, CallbackInfo ci) {
        if((Object)this instanceof LivingEntity && TameableUtils.isTamed((LivingEntity)(Object)this) && TameableUtils.hasEnchant((LivingEntity)(Object)this, PHEnchantmentRegistry.MUFFLED)){
            ci.cancel();
        }

    }
}
