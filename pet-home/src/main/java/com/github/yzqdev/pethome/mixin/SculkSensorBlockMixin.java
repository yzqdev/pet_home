package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
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

/** MUFFLED（消音）宠物踩上幽匿感测体不产生振动交互。 */
@Mixin(SculkSensorBlock.class)
public class SculkSensorBlockMixin {

    /** 注入 {@code SculkSensorBlock#stepOn} 头部（可取消）：带 MUFFLED（消音）附魔的宠物踩上幽匿感测体不触发振动（避免暴露行踪 / 误触发红石）。 */
    @Inject(
            method = {"stepOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/Entity;)V"},
            remap = true,
            at = @At("HEAD"),
            cancellable = true
    )
    private void di_onStepOn(Level level, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if(entity instanceof LivingEntity living && TameableUtils.isTamed(living) && TameableUtils.hasEnchant(living, PHEnchantmentRegistry.MUFFLED)){
            ci.cancel();
        }
    }
    
}
