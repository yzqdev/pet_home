package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 替代 Forge LivingChangeTargetEvent（宠物不被主人选中为目标）与
 * MobSpawnEvent.FinalizeSpawn（掠夺者惧怕兔子）。
 */
@Mixin(Mob.class)
public class MobHooksMixin {

    @Inject(method = "setTarget(Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private void ph_onSetAttackTarget(LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (ServerEvent.onSetAttackTarget(self, target)) {
            ci.cancel();
        }
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void ph_onFinalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType spawnType, net.minecraft.world.entity.SpawnGroupData spawnData, net.minecraft.nbt.CompoundTag dataTag, CallbackInfoReturnable<net.minecraft.world.entity.SpawnGroupData> cir) {
        Mob self = (Mob) (Object) this;
        ServerEvent.onFinalizeSpawn(self);
    }
}
