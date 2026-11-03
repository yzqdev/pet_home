package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.ShootTongue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/** 青蛙吐舌目标过滤与吞食保护：驯服青蛙不吐舌攻击主人 / 同主人单位。 */
@Mixin(ShootTongue.class)
public class ShootTongueMixin {

    /** 注入 {@code ShootTongue#checkExtraStartConditions} 的 {@code canEat} 判定点（可取消）：驯服青蛙绕过"饥饿才吐舌"限制，随时可吐舌作战。 */
    @Inject(
            method = {"checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/frog/Frog;)Z"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/frog/Frog;canEat(Lnet/minecraft/world/entity/LivingEntity;)Z"
            ),
            cancellable = true
    )
    private void di_checkExtraStartConditions(ServerLevel level, Frog frog, CallbackInfoReturnable<Boolean> cir){
        if(frog instanceof ModifiedToBeTameable tameable && tameable.isTame()){
            cir.setReturnValue(true);
        }
    }

    /** 注入 {@code ShootTongue#eatEntity} 头部（可取消）：驯服青蛙不吞食目标——改为吐舌伤害（击杀也不移除实体），避免吃掉战利品载体。 */
    @Inject(
            method = {"eatEntity(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/frog/Frog;)V"},
            remap = true,
            at = @At("HEAD"),
            cancellable = true
    )
    private void di_eatEntity(ServerLevel level, Frog frog, CallbackInfo ci) {
        if(frog instanceof ModifiedToBeTameable tameable && tameable.isTame()){
            ci.cancel();
            frog.playSound(SoundEvents.FROG_TONGUE);
            Optional<Entity> optional = frog.getTongueTarget();
            if (optional.isPresent()) {
                Entity entity = optional.get();
                if (entity.isAlive()) {
                    frog.doHurtTarget(entity);
                    if (!entity.isAlive() && Frog.canEat(frog)) {
                        entity.remove(Entity.RemovalReason.KILLED);
                    }
                }
            }

        }

    }
}
