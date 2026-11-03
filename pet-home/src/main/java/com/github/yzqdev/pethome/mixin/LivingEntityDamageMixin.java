package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.event.EntityHurtHandler;
import com.github.yzqdev.pethome.server.event.EntityTickHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric 事件替代：LivingTickEvent / LivingAttackEvent / LivingHurtEvent / LivingDamageEvent / LivingDropsEvent /
 * EntityTeleportEvent（randomTeleport）。
 */
@Mixin(LivingEntity.class)
public class LivingEntityDamageMixin {

    /** 注入 {@code LivingEntity#tick} 头部：每 tick 生命值校验 + 宠物行为调度（附魔触发 / 指令状态，见 {@code EntityTickHandler}）。 */
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void ph_onLivingTick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        ServerEvent.validateHealth(self);
        EntityTickHandler.onLivingUpdate(self);
    }

    /** 注入 {@code LivingEntity#hurt} 头部（可取消）：拒绝 NaN 伤害；按友军伤害保护改写伤害并分发受击事件（取消 = 无敌）。 */
    @Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
    private void ph_onLivingAttack(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        ServerEvent.validateHealth(self);
        if (Float.isNaN(amount)) {
            cir.setReturnValue(false);
            return;
        }
        amount = ServerEvent.modifyDamage(self, source, amount);
        EntityHurtHandler.AttackEvent event = new EntityHurtHandler.AttackEvent(self, source, amount);
        EntityHurtHandler.onLivingAttack(event);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
        }
    }

    /**
     * 替代 Forge 的 LivingHurtEvent：在护甲减免前修正本次伤害数值（Violent 附魔）。
     */
    @ModifyVariable(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("HEAD"), argsOnly = true)
    private float ph_onLivingHurt(float amount, DamageSource source, float originalAmount) {
        LivingEntity self = (LivingEntity) (Object) this;
        return EntityHurtHandler.onLivingHurt(self, source, amount);
    }

    /**
     * 替代 Forge 的 LivingDamageEvent：护甲/吸收减免之后、真正扣血之前触发
     * （Chaos / Share / Paralysis 附魔）。
     */
    @Inject(method = "actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V", at = @At("HEAD"))
    private void ph_onLivingDamage(DamageSource source, float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        EntityHurtHandler.onLivingDamage(self, source, amount);
    }

    /** 注入 {@code LivingEntity#dropAllDeathLoot} 头部（可取消）：按死亡掉落保护规则取消掉落（如宠物床复活流程需要保留实体数据）。 */
    @Inject(method = "dropAllDeathLoot(Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At("HEAD"), cancellable = true)
    private void ph_onLivingDrops(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (ServerEvent.shouldCancelLivingDrops(self)) {
            ci.cancel();
        }
    }

    /** 注入 {@code LivingEntity#randomTeleport} 头部：玩家随机传送（紫颂果 / 末影珍珠）时同样触发"拴魂"宠物随行转移。 */
    @Inject(method = "randomTeleport(DDDZ)Z", at = @At("HEAD"))
    private void ph_onRandomTeleport(double x, double y, double z, boolean broadcast, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player) {
            ServerEvent.onEntityTeleport(player, new net.minecraft.world.phys.Vec3(x, y, z));
        }
    }
}
