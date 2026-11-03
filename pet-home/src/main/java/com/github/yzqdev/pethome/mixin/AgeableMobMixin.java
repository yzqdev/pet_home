package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** {@code isBaby} 覆盖：IMMATURITY_CURSE 使成年宠物被强制判定为幼年。 */
@Mixin(AgeableMob.class)
public class AgeableMobMixin extends PathfinderMob {

    protected AgeableMobMixin(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    /** 注入 {@code AgeableMob#isBaby} 头部（可取消）：IMMATURITY_CURSE（早熟诅咒）的宠物恒判为幼年。 */
    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"isBaby()Z"},
            cancellable = true
    )
    private void di_isBaby(CallbackInfoReturnable<Boolean> cir){
        if(TameableUtils.isTamed(this) && TameableUtils.hasEnchant(this, PHEnchantmentRegistry.IMMATURITY_CURSE)){
            cir.setReturnValue(true);
        }
    }
}
