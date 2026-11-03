package com.github.yzqdev.pethome.mixin;



import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.IComandableMob;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
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

/** 原版「跟随主人」Goal 接入三态指令：仅跟随态(2)可用，canUse/canContinueToUse/canTeleportTo/tick 按驯服扩展物种的语义改写。 */
@Mixin(FollowOwnerGoal.class)
public abstract class FollowOwnerGoalMixin extends Goal {

    @Shadow
    @Final
    private TamableAnimal tamable;
    @Shadow
    @Final
    private LevelReader level;
    @Shadow
    private LivingEntity owner;
    @Shadow
    @Final
    private float stopDistance;

    @Shadow @Final private double speedModifier;

    /** 注入 {@code FollowOwnerGoal#canUse} 头部（可取消）：三态指令开启且宠物非跟随态(2)时禁用跟随主人 Goal。 */
    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"canUse()Z"},
            cancellable = true
    )
    private void di_canUse(CallbackInfoReturnable<Boolean> cir){
        if(tamable instanceof IComandableMob commandableMob && commandableMob.getCommand() != 2 && PetHomeMod.CONFIG.trinaryCommandSystem.get()){
            cir.setReturnValue(false);
        }
    }

    /** 注入 {@code FollowOwnerGoal#canContinueToUse} 头部（可取消）：宠物切到非跟随态(2)时立即中断跟随。 */
    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"canContinueToUse()Z"},
            cancellable = true
    )
    private void di_canContinueToUse(CallbackInfoReturnable<Boolean> cir){
        if(tamable instanceof IComandableMob commandableMob && commandableMob.getCommand() != 2 && PetHomeMod.CONFIG.trinaryCommandSystem.get()){
            cir.setReturnValue(false);
        }
    }

    /** 注入 {@code FollowOwnerGoal#canTeleportTo} 头部（可取消）：带 AMPHIBIOUS（两栖）附魔的宠物允许传送到水中位置（原版禁止）。 */
    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"canTeleportTo(Lnet/minecraft/core/BlockPos;)Z"},
            cancellable = true
    )
    private void di_canTeleportTo(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if(TameableUtils.hasEnchant(tamable, PHEnchantmentRegistry.AMPHIBIOUS) && level.isWaterAt(pos)){
            cir.setReturnValue(true);
        }
    }

    /** 注入 {@code FollowOwnerGoal#tick} 头部（可取消）：带 AMPHIBIOUS 附魔且在水中的宠物近距离(12 格)直接寻路游向主人，跳过原版陆基跟随逻辑。 */
    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"tick()V"},
            cancellable = true
    )
    private void di_tick(CallbackInfo ci) {
        if(TameableUtils.hasEnchant(tamable, PHEnchantmentRegistry.AMPHIBIOUS) && tamable.isInWaterOrBubble() && this.tamable.distanceToSqr(this.owner) < 144.0D){
            tamable.getNavigation().moveTo(owner, speedModifier);
            ci.cancel();
        }
    }
}