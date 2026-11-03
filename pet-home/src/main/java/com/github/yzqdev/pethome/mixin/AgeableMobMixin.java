package com.github.yzqdev.pethome.mixin;


import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 注入 {@code AgeableMob}：HEAD 可取消 {@code isBaby}——immaturity_curse（变小诅咒）宠物恒判幼年
 * （与 onUpdateEnchants 里 setBaby(true) 配合，诅咒期间保持幼年模型）。
 */
@Mixin(AgeableMob.class)
public class AgeableMobMixin extends PathfinderMob {

    protected AgeableMobMixin(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"isBaby()Z"},
            cancellable = true
    )
    private void di_isBaby(CallbackInfoReturnable<Boolean> cir) {
        if (TameableUtils.isTamed(this) && TameableUtils.hasEnchant(this, PHEnchantments.IMMATURITY_CURSE)) {
            cir.setReturnValue(true);
        }
    }
}
