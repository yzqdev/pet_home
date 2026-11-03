package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.util.PHEntityDataAccessors;
import com.github.yzqdev.pethome.server.NbtKeys;


import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 数据槽改造组：给狼挂 {@link IComandableMob} 三态指令（驯服状态走 TamableAnimal 原生存储），注入 defineSynchedData/序列化、mobInteract 指令切换与按血量算尾巴角度。 */
@Mixin(Wolf.class)
public abstract class WolfMixin extends TamableAnimal implements IComandableMob {

    protected WolfMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    // 26.1：defineId 必须在 <clinit> 完成（实体构造时 Builder 容量=已注册访问器数，晚注册会 AIOOBE）；
    // 且调用者类必须是实体类、实体类上不能有 @MixinMerged 的访问器字段——
    // 故注入 <clinit> 调用 defineId，访问器统一存到外部类 PHEntityDataAccessors
    @Inject(
            method = {"<clinit>()V"},
            at = {@At("TAIL")},
            remap = false
    )
    private static void di_defineAccessors(CallbackInfo ci) {
        PHEntityDataAccessors.wolfCommand = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V"}
    )
    private void di_registerData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(PHEntityDataAccessors.wolfCommand, 0);
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V"}
    )
    private void di_writeAdditional(net.minecraft.world.level.storage.ValueOutput compoundNBT, CallbackInfo ci) {
        compoundNBT.putInt(NbtKeys.DI_COMMAND, this.getCommand());
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V"}
    )
    private void di_readAdditional(net.minecraft.world.level.storage.ValueInput compoundNBT, CallbackInfo ci) {
        this.setCommand(compoundNBT.getIntOr(NbtKeys.DI_COMMAND, 0));
    }

    // 注入点：mobInteract 里唯一一次 setOrderedToSit 调用之前（已用 javap 确认 26.1/26.3 该调用存在且唯一）。
    // 原版所有会消耗物品 / 改变状态的分支（回血、繁殖、幼年加速成长、项圈染色、狼铠装备与修复）
    // 都在到达本点之前已 return，因此这里天然只接管「原版这次右键什么也不做」的情况（与 1.20 / 1.21 侧同一注入点）。
    @Inject(
            method = {"mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/wolf/Wolf;setOrderedToSit(Z)V"
            ),
            cancellable = true
    )
    private void di_onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        // 手持该宠物食物且原版会消费它时不拦——回血（不满血）/ 繁殖（成年可恋爱）/ 幼年加速成长；三态只接管「原版这次右键什么也不做」的情况
        boolean vanillaFoodAction = this.isFood(player.getItemInHand(hand)) && (this.getHealth() < this.getMaxHealth() || this.isBaby() || (this.getAge() == 0 && this.canFallInLove()));
        if(PetHomeConfig.trinaryCommandSystem && this.isTame() && TameableUtils.isPetOf(player, this) && !vanillaFoodAction){
            this.jumping = false;
            this.navigation.stop();
            this.setTarget((LivingEntity)null);
            player.swing(hand, true);
            cir.setReturnValue(this.playerSetCommand(player, this));
        }
    }

    public int getCommand(){
        return this.entityData.get(PHEntityDataAccessors.wolfCommand);
    }

    public void setCommand(int i){
        this.entityData.set(PHEntityDataAccessors.wolfCommand, i);
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"getTailAngle()F"},
            cancellable = true)
    private void di_getTailAngle(CallbackInfoReturnable<Float> cir) {
        if(!((NeutralMob)this).isAngry() && this.isTame()){
            float f = (this.getMaxHealth() - this.getHealth()) / this.getMaxHealth() * 20F;
            cir.setReturnValue((0.55F - Math.max(f * 0.02F, 0F)) * (float)Math.PI);
        }
    }

    @Override
    public void sendCommandMessage(Player owner, int command, Component name) {
        owner.sendOverlayMessage(Component.translatable("message.pet_home.command_" + command, name));
    }
}
