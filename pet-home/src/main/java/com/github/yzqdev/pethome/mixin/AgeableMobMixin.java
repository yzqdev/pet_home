package com.github.yzqdev.pethome.mixin;


import com.github.yzqdev.pethome.datagen.ModEnchantments;
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
 * 幼体诅咒（immaturity_curse）注入：isBaby 对已驯服带该附魔的个体恒真，
 * 使宠物永久保持幼年模型/体型（onUpdateEnchants 另会同步姿态）。
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
        if (TameableUtils.isTamed(this) && TameableUtils.hasEnchant(this, ModEnchantments.IMMATURITY_CURSE)) {
            cir.setReturnValue(true);
        }
    }
}
