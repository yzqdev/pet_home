package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.PHActivityRegistry;
import com.github.yzqdev.pethome.server.entity.ModifedToBeTameable;
import com.github.yzqdev.pethome.server.entity.ai.AmphibianFollowOwnerBehavior;
import com.github.yzqdev.pethome.server.entity.ai.AmphibianStayBehavior;
import com.github.yzqdev.pethome.server.misc.PHTagRegistry;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.frog.Frog;
import java.util.function.Predicate;
import net.minecraft.world.entity.animal.frog.FrogAi;
import net.minecraft.world.entity.animal.frog.ShootTongue;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FrogAi.class)
public class FrogAiMixin {


    @Inject(
            method = {"makeBrain(Lnet/minecraft/world/entity/ai/Brain;)Lnet/minecraft/world/entity/ai/Brain;"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/frog/FrogAi;initJumpActivity(Lnet/minecraft/world/entity/ai/Brain;)V"
            )
    )
    private static void di_makeBrain(Brain<Frog> brain, CallbackInfoReturnable<Brain<?>> cir) {
        brain.addActivity(PHActivityRegistry.FROG_FOLLOW.get(), ImmutableList.of(Pair.of(0, new AmphibianFollowOwnerBehavior(1.25F, 1.0F)), Pair.of(2, new ShootTongue(SoundEvents.FROG_TONGUE, SoundEvents.FROG_EAT))));
        brain.addActivity(PHActivityRegistry.FROG_STAY.get(), ImmutableList.of(Pair.of(0, new AmphibianStayBehavior())));
    }

    private static boolean canAttack(Frog frog) {
        return !frog.isInLove();
    }

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
        if (frog instanceof ModifedToBeTameable modifedToBeTameable) {
            if (modifedToBeTameable.isStayingStill()) {
                brain.setActiveActivityIfPossible(PHActivityRegistry.FROG_STAY.get());
                ci.cancel();
            } else if (modifedToBeTameable.isFollowingOwner()) {
                if(frog.getTarget() != null && frog.getTarget().isAlive()){
                    brain.setMemory(MemoryModuleType.ATTACK_TARGET, frog.getTarget());
                    brain.setMemory(MemoryModuleType.NEAREST_ATTACKABLE, frog.getTarget());
                    brain.setActiveActivityIfPossible(Activity.TONGUE);
                }else{
                    frog.getBrain().setActiveActivityToFirstValid(ImmutableList.of(PHActivityRegistry.FROG_FOLLOW.get(), Activity.TONGUE, Activity.LAY_SPAWN, Activity.LONG_JUMP, Activity.SWIM,  Activity.IDLE));
                }
                ci.cancel();
            }
        }
    }

    @Inject(
            method = {"getTemptations()Ljava/util/function/Predicate;"},
            remap = true,
            at = @At(
                    value = "TAIL"
            ),
            cancellable = true
    )
    private static void di_getTemptationItems(CallbackInfoReturnable<Predicate<ItemStack>> cir) {
        // 先取出原返回值再构造 lambda——lambda 内调用 cir.getReturnValue() 会无限递归
        Predicate<ItemStack> original = cir.getReturnValue();
        cir.setReturnValue(stack -> original.test(stack) || stack.is(PHTagRegistry.TAME_FROGS_WITH));
    }
}
