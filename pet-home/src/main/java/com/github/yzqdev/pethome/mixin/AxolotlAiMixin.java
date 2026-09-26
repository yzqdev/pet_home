package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.datagen.ModEnchantments;
import com.github.yzqdev.pethome.server.entity.PHActivityRegistry;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.util.TameableUtils;
import com.github.yzqdev.pethome.server.entity.ai.AmphibianFollowOwnerBehavior;
import com.github.yzqdev.pethome.server.entity.ai.AmphibianStayBehavior;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.axolotl.AxolotlAi;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxolotlAi.class)
public class AxolotlAiMixin {

    // 26.1：brain 构建改为 getActivities() 列表模式，TAIL 追加 follow/stay 活动
    @Inject(
            method = {"getActivities()Ljava/util/List;"},
            remap = true,
            at = @At("TAIL"),
            cancellable = true
    )
    private static void di_getActivities(CallbackInfoReturnable<List<net.minecraft.world.entity.ai.ActivityData<Axolotl>>> cir) {
        cir.setReturnValue(ImmutableList.<net.minecraft.world.entity.ai.ActivityData<Axolotl>>builder()
                .addAll(cir.getReturnValue())
                .add(net.minecraft.world.entity.ai.ActivityData.create(PHActivityRegistry.AXOLOTL_FOLLOW,
                        ImmutableList.of(Pair.of(0, new AmphibianFollowOwnerBehavior<>(0.3F, 0.6F))), Set.of(), Set.of()))
                .add(net.minecraft.world.entity.ai.ActivityData.create(PHActivityRegistry.AXOLOTL_STAY,
                        ImmutableList.of(Pair.of(0, new AmphibianStayBehavior<>())), Set.of(), Set.of()))
                .build());
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
        if (activity != Activity.PLAY_DEAD && !axolotl.isPlayingDead() && axolotl instanceof ModifiedToBeTameable modifiedToBeTameable) {
            if (modifiedToBeTameable.isStayingStill()) {
                brain.setActiveActivityIfPossible(PHActivityRegistry.AXOLOTL_STAY);
                ci.cancel();
            } else if (modifiedToBeTameable.isFollowingOwner()) {
                brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.PLAY_DEAD, Activity.FIGHT, PHActivityRegistry.AXOLOTL_FOLLOW));
                ci.cancel();
            }
        }
    }


    // 26.1：getTemptations 已移除（诱惑改为 SensorType.FOOD_TEMPTATIONS → Animal.isFood），
    // 追加诱惑物品的注入点不存在；驯服交互（右键喂食）不受影响，故不再注入。
    // 原注入（1.21）：getTemptations TAIL 追加热带鱼。

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
        cir.setReturnValue(axolotl.isInWaterOrRain() ? 0.6F + speedsterLevel * 0.05F : 0.15F + speedsterLevel * 0.1F);
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
        cir.setReturnValue(axolotl.isInWaterOrRain() ? 0.6F + speedsterLevel * 0.05F : 0.15F + speedsterLevel * 0.1F);
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
        cir.setReturnValue(axolotl.isInWaterOrRain() ? 0.5F + speedsterLevel * 0.05F : 0.15F + speedsterLevel * 0.15F);
    }


}
