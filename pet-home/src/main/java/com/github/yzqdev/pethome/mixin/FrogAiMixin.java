package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.PHActivityRegistry;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.entity.ai.AmphibianFollowOwnerBehavior;
import com.github.yzqdev.pethome.server.entity.ai.AmphibianStayBehavior;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import com.mojang.datafixers.util.Pair;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.FrogAi;
import net.minecraft.world.entity.animal.frog.ShootTongue;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FrogAi.class)
public class FrogAiMixin {


    // 26.1：brain 构建改为 getActivities() 列表模式，TAIL 追加 follow/stay 活动
    @Inject(
            method = {"getActivities()Ljava/util/List;"},
            remap = true,
            at = @At("TAIL"),
            cancellable = true
    )
    private static void di_getActivities(CallbackInfoReturnable<List<net.minecraft.world.entity.ai.ActivityData<Frog>>> cir) {
        cir.setReturnValue(ImmutableList.<net.minecraft.world.entity.ai.ActivityData<Frog>>builder()
                .addAll(cir.getReturnValue())
                .add(net.minecraft.world.entity.ai.ActivityData.create(PHActivityRegistry.FROG_FOLLOW,
                        ImmutableList.of(Pair.of(0, new AmphibianFollowOwnerBehavior<>(1.25F, 1.0F)), Pair.of(2, new ShootTongue(SoundEvents.FROG_TONGUE, SoundEvents.FROG_EAT))), Set.of(), Set.of()))
                .add(net.minecraft.world.entity.ai.ActivityData.create(PHActivityRegistry.FROG_STAY,
                        ImmutableList.of(Pair.of(0, new AmphibianStayBehavior<>())), Set.of(), Set.of()))
                .build());
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
        if (frog instanceof ModifiedToBeTameable modifiedToBeTameable) {
            if (modifiedToBeTameable.isStayingStill()) {
                brain.setActiveActivityIfPossible(PHActivityRegistry.FROG_STAY);
                ci.cancel();
            } else if (modifiedToBeTameable.isFollowingOwner()) {
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

    // 26.1：getTemptations 已移除（诱惑改为 SensorType.FROG_TEMPTATIONS → Animal.isFood），
    // 追加诱惑物品的注入点不存在；驯服交互（右键喂食蜘蛛眼）不受影响，故不再注入。
}
