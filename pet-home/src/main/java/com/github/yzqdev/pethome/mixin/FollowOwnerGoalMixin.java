package com.github.yzqdev.pethome.mixin;



import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.ModEnchantments;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 原版跟随主人 Goal 接入三态指令与两栖：三态开启时仅跟随态(2)可用/可延续（HEAD 取消）；
 * tick 注入让两栖宠物在水里直接直线导航到 12 格内的主人（绕开原版陆地寻路卡水）。
 */
@Mixin(FollowOwnerGoal.class)
public abstract class FollowOwnerGoalMixin extends Goal {

    @Shadow
    @Final
    private TamableAnimal tamable;
    @Shadow
    private LivingEntity owner;
    @Shadow
    @Final
    private float stopDistance;

    @Shadow @Final private double speedModifier;

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"canUse()Z"},
            cancellable = true
    )
    private void di_canUse(CallbackInfoReturnable<Boolean> cir){
        if(tamable instanceof IComandableMob commandableMob && commandableMob.getCommand() != 2 && PetHomeConfig.trinaryCommandSystem){
            cir.setReturnValue(false);
        }
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"canContinueToUse()Z"},
            cancellable = true
    )
    private void di_canContinueToUse(CallbackInfoReturnable<Boolean> cir){
        if(tamable instanceof IComandableMob commandableMob && commandableMob.getCommand() != 2 && PetHomeConfig.trinaryCommandSystem){
            cir.setReturnValue(false);
        }
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"tick()V"},
            cancellable = true
    )
    private void di_tick(CallbackInfo ci) {
        if(TameableUtils.hasEnchant(tamable, ModEnchantments.AMPHIBIOUS) && tamable.isInWaterOrBubble() && this.tamable.distanceToSqr(this.owner) < 144.0D){
            tamable.getNavigation().moveTo(owner, speedModifier);
            ci.cancel();
        }
    }
}