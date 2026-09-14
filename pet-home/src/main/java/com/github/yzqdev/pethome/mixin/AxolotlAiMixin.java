package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.datagen.ModEnchantments;
import com.github.yzqdev.pethome.server.entity.PHActivityRegistry;
import com.github.yzqdev.pethome.server.entity.ModifedToBeTameable;
import com.github.yzqdev.pethome.util.TameableUtils;
import com.github.yzqdev.pethome.server.entity.ai.AmphibianFollowOwnerBehavior;
import com.github.yzqdev.pethome.server.entity.ai.AmphibianStayBehavior;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.axolotl.AxolotlAi;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.function.Predicate;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxolotlAi.class)
public class AxolotlAiMixin {

    @Inject(
            method = {"makeBrain(Lnet/minecraft/world/entity/ai/Brain;)Lnet/minecraft/world/entity/ai/Brain;"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/axolotl/AxolotlAi;initPlayDeadActivity(Lnet/minecraft/world/entity/ai/Brain;)V"
            )
    )
    private static void di_makeBrain(Brain<Axolotl> brain, CallbackInfoReturnable<Brain<?>> cir) {
        brain.addActivity(PHActivityRegistry.AXOLOTL_FOLLOW.get(), ImmutableList.of(Pair.of(0, new AmphibianFollowOwnerBehavior(0.3F, 0.6F))));
        brain.addActivity(PHActivityRegistry.AXOLOTL_STAY.get(), ImmutableList.of(Pair.of(0, new AmphibianStayBehavior())));
    }

    @Inject(
            method = {"updateActivity(Lnet/minecraft/world/entity/animal/axolotl/Axolotl;)V"},
            remap = true,
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private static void di_updateActivity(Axolotl axolotl, CallbackInfo ci) {
        Brain<Axolotl> brain = axolotl.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        if (activity != Activity.PLAY_DEAD && !axolotl.isPlayingDead() && axolotl instanceof ModifedToBeTameable modifedToBeTameable) {
            if (modifedToBeTameable.isStayingStill()) {
                brain.setActiveActivityIfPossible(PHActivityRegistry.AXOLOTL_STAY.get());
                ci.cancel();
            } else if (modifedToBeTameable.isFollowingOwner()) {
                brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.PLAY_DEAD, Activity.FIGHT, PHActivityRegistry.AXOLOTL_FOLLOW.get()));
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
        cir.setReturnValue(stack -> original.test(stack) || stack.is(Items.TROPICAL_FISH));
    }

    @Inject(
            method = {"getSpeedModifierChasing(Lnet/minecraft/world/entity/LivingEntity;)F"},
            remap = true,
            at = @At(
                    value = "TAIL"
            ),
            cancellable = true
    )
    private static void di_getSpeedModifierChasing(LivingEntity axolotl, CallbackInfoReturnable<Float> cir) {
        int speedsterLevel = TameableUtils.getEnchantLevel(axolotl, ModEnchantments.SPEEDSTER);
        cir.setReturnValue(axolotl.isInWaterOrBubble() ? 0.6F + speedsterLevel * 0.05F : 0.15F + speedsterLevel * 0.1F);
    }

    @Inject(
            method = {"getSpeedModifierFollowingAdult(Lnet/minecraft/world/entity/LivingEntity;)F"},
            remap = true,
            at = @At(
                    value = "TAIL"
            ),
            cancellable = true
    )
    private static void di_getSpeedModifierFollowingAdult(LivingEntity axolotl, CallbackInfoReturnable<Float> cir) {
        int speedsterLevel = TameableUtils.getEnchantLevel(axolotl, ModEnchantments.SPEEDSTER);
        cir.setReturnValue(axolotl.isInWaterOrBubble() ? 0.6F + speedsterLevel * 0.05F : 0.15F + speedsterLevel * 0.1F);
    }

    @Inject(
            method = {"getSpeedModifier(Lnet/minecraft/world/entity/LivingEntity;)F"},
            remap = true,
            at = @At(
                    value = "TAIL"
            ),
            cancellable = true
    )
    private static void di_getSpeedModifier(LivingEntity axolotl, CallbackInfoReturnable<Float> cir) {
        int speedsterLevel = TameableUtils.getEnchantLevel(axolotl, ModEnchantments.SPEEDSTER);
        cir.setReturnValue(axolotl.isInWaterOrBubble() ? 0.5F + speedsterLevel * 0.05F : 0.15F + speedsterLevel * 0.15F);
    }


}
