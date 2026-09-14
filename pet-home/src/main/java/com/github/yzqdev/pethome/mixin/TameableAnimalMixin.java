package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.datagen.ModEnchantments;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TamableAnimal.class)
public abstract class TameableAnimalMixin extends Animal {

    protected TameableAnimalMixin(EntityType<? extends Animal> an, Level lvl) {
        super(an, lvl);
    }

    @Inject(
            method = {"isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z"},
            remap = true,
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void di_isAlliedTo(Entity other, CallbackInfoReturnable<Boolean> cir) {
        if(TameableUtils.hasSameOwnerAs(this, other)){
            cir.setReturnValue(true);
        }
    }

    // 1.21/26.1：canTeleportTo 移到 TamableAnimal（自 FollowOwnerGoalMixin 搬家）——两栖附魔允许传送到水中
    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"canTeleportTo(Lnet/minecraft/core/BlockPos;)Z"},
            cancellable = true
    )
    private void di_canTeleportTo(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (TameableUtils.hasEnchant((LivingEntity) (Object) this, ModEnchantments.AMPHIBIOUS) && this.level().isWaterAt(pos)) {
            cir.setReturnValue(true);
        }
    }
}
