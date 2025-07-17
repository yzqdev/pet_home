package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkSensorBlock.class)
public class SculkSensorBlockMixin {

    @Inject(
            method = {"Lnet/minecraft/world/level/block/SculkSensorBlock;stepOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/Entity;)V"},
            remap = true,
            at = @At("HEAD"),
            cancellable = true
    )
    private void di_onStepOn(Level level, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if(entity instanceof LivingEntity living && TameableUtils.isTamed(living) && TameableUtils.hasEnchant(living, DIEnchantmentRegistry.MUFFLED)){
            ci.cancel();
        }
    }
    
}
