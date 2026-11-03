package com.github.yzqdev.pethome.mixin;



import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
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
 * 原版 {@code FollowOwnerGoal} 适配三态指令与"两栖"附魔：
 * 指令非"跟随"（2）时不再启动/继续（启用三态指令配置时生效）；
 * 携带两栖附魔且主人在 12 格内的水中宠物改用直线路径跟随
 * （原版寻路在水下会卡住，这里绕过寻路直接朝主人游）。
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
        if(TameableUtils.hasEnchant(tamable, PHEnchantments.AMPHIBIOUS) && tamable.isInWaterOrRain() && this.tamable.distanceToSqr(this.owner) < 144.0D){
            tamable.getNavigation().moveTo(owner, speedModifier);
            ci.cancel();
        }
    }
}