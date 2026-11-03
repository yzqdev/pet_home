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

    /** 注入 {@code Mob#setTarget} 头部（可取消）：玩家试图把自己的宠物设为目标时拦截，决策转发 {@link ServerEvent#onSetAttackTarget}。 */
    @Inject(method = "setTarget(Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private void ph_onSetAttackTarget(LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (ServerEvent.onSetAttackTarget(self, target)) {
            ci.cancel();
        }
    }

    /** 注入 {@code Mob#finalizeSpawn} 尾部：生成后调用 {@link ServerEvent#onFinalizeSpawn}（按配置给掠夺者追加惧怕兔子 AI）。 */
    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void ph_onFinalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType spawnType, net.minecraft.world.entity.SpawnGroupData spawnData, net.minecraft.nbt.CompoundTag dataTag, CallbackInfoReturnable<net.minecraft.world.entity.SpawnGroupData> cir) {
        Mob self = (Mob) (Object) this;
        ServerEvent.onFinalizeSpawn(self);
    }
}
