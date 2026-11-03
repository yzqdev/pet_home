package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.PHActivityRegistry;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.entity.ai.AmphibianFollowOwnerBehavior;
import com.github.yzqdev.pethome.server.entity.ai.AmphibianStayBehavior;
import com.github.yzqdev.pethome.server.misc.PHTagRegistry;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.FrogAi;
import net.minecraft.world.entity.animal.frog.ShootTongue;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 青蛙脑部改造：驯服个体注入 follow/stay 自定义活动，过滤诱惑物与吐舌目标。 */
@Mixin(FrogAi.class)
public class FrogAiMixin {


    /** 注入 {@code FrogAi#makeBrain} 的跳跃活动初始化点：向脑部追加 FROG_FOLLOW（跟随主人 + 吐舌）与 FROG_STAY（停留）两个自定义活动。 */
    @Inject(
            method = {"makeBrain(Lnet/minecraft/world/entity/ai/Brain;)Lnet/minecraft/world/entity/ai/Brain;"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/frog/FrogAi;initJumpActivity(Lnet/minecraft/world/entity/ai/Brain;)V"
            )
    )
    private static void di_makeBrain(Brain<Frog> brain, CallbackInfoReturnable<Brain<?>> cir) {
        brain.addActivity(PHActivityRegistry.FROG_FOLLOW, ImmutableList.of(Pair.of(0, new AmphibianFollowOwnerBehavior(1.25F, 1.0F)), Pair.of(2, new ShootTongue(SoundEvents.FROG_TONGUE, SoundEvents.FROG_EAT))));
        brain.addActivity(PHActivityRegistry.FROG_STAY, ImmutableList.of(Pair.of(0, new AmphibianStayBehavior())));
    }

    private static boolean canAttack(Frog frog) {
        return !frog.isInLove();
    }

    /** 注入 {@code FrogAi#updateActivity} 头部（可取消）：驯服青蛙按指令切换活动——停留走 FROG_STAY；跟随且有目标走 TONGUE，否则按 FROG_FOLLOW 优先的候选链选活动。 */
    @Inject(
            method = {"updateActivity(Lnet/minecraft/world/entity/animal/frog/Frog;)V"},
            remap = true,
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private static void di_updateActivity(Frog frog, CallbackInfo ci) {
        Brain<Frog> brain = frog.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        if (frog instanceof ModifiedToBeTameable ModifiedToBeTameable) {
            if (ModifiedToBeTameable.isStayingStill()) {
                brain.setActiveActivityIfPossible(PHActivityRegistry.FROG_STAY);
                ci.cancel();
            } else if (ModifiedToBeTameable.isFollowingOwner()) {
                if(frog.getTarget() != null && frog.getTarget().isAlive()){
                    brain.setMemory(MemoryModuleType.ATTACK_TARGET, frog.getTarget());
                    brain.setMemory(MemoryModuleType.NEAREST_ATTACKABLE, frog.getTarget());
                    brain.setActiveActivityIfPossible(Activity.TONGUE);
                }else{
                    frog.getBrain().setActiveActivityToFirstValid(ImmutableList.of(PHActivityRegistry.FROG_FOLLOW, Activity.TONGUE, Activity.LAY_SPAWN, Activity.LONG_JUMP, Activity.SWIM,  Activity.IDLE));
                }
                ci.cancel();
            }
        }
    }

    /** 注入 {@code FrogAi#getTemptations} 尾部：诱惑物并入 {@code PHTagRegistry#TAME_FROGS_WITH} 标签物品（驯服 / 治疗渠道）。 */
    @Inject(
            method = {"getTemptations()Lnet/minecraft/world/item/crafting/Ingredient;"},
            remap = true,
            at = @At(
                    value = "TAIL"
            ),
            cancellable = true
    )
    private static void di_getTemptationItems(CallbackInfoReturnable<Ingredient> cir) {
        cir.setReturnValue(Ingredient.of(java.util.stream.Stream.concat(java.util.stream.Stream.of(cir.getReturnValue().getItems()), java.util.stream.Stream.of(Ingredient.of(PHTagRegistry.TAME_FROGS_WITH).getItems()))));
    }
}
