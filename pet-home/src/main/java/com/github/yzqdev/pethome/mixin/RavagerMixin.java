package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeConfig;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.raid.Raider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

/** 兔子吓掠夺兽（自 1.20 移植；1.21.1 已移除 FinalizeSpawn 事件，改用 Mixin） */
@Mixin(Ravager.class)
public abstract class RavagerMixin extends Raider {

    protected RavagerMixin(net.minecraft.world.entity.EntityType<? extends Raider> type, net.minecraft.world.level.Level level) {
        super(type, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void di_registerGoals(CallbackInfo ci) {
        if (PetHomeConfig.rabbitsScareRavagers) {
            Predicate<LivingEntity> avoidFilter = (Predicate<LivingEntity>) (Predicate<?>) EntitySelector.NO_SPECTATORS;
            this.goalSelector.addGoal(4, new AvoidEntityGoal<>((PathfinderMob) (Object) this, Rabbit.class, 13.0F, 1.5D, 2.0D, avoidFilter));
        }
    }
}
