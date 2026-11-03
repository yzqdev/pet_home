package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.NbtKeys;


import com.github.yzqdev.pethome.server.entity.IComandableMob;
import com.github.yzqdev.pethome.PetHomeMod;
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
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 狼的三态指令扩展：实现 {@link IComandableMob}（command 经 {@code defineSynchedData} 注入并随 NBT 持久化），右键空手轮换指令、尾巴角度随指令变化。
 */
@Mixin(Wolf.class)
public abstract class WolfMixin extends TamableAnimal implements IComandableMob {
    private static final EntityDataAccessor<Integer> COMMAND = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);

    protected WolfMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    /** 注入 {@code Wolf#defineSynchedData} 尾部：注册三态指令同步字段（0=游走）。 */
    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"defineSynchedData()V"}
    )
    private void di_registerData(CallbackInfo ci) {
        this.entityData.define(COMMAND, 0);
    }

    /** 注入 {@code Wolf#addAdditionalSaveData} 尾部：指令档位写入 NBT 持久化。 */
    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Wolf;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"}
    )
    private void di_writeAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        compoundNBT.putInt(NbtKeys.DI_COMMAND, this.getCommand());
    }

    /** 注入 {@code Wolf#readAdditionalSaveData} 尾部：从 NBT 恢复指令档位。 */
    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Wolf;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"}
    )
    private void di_readAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        this.setCommand(compoundNBT.getInt(NbtKeys.DI_COMMAND));
    }

    /** 注入 {@code Wolf#mobInteract} 的原版坐下切换点（可取消）：开启三态指令配置时拦截"空手右键=坐下"，改为轮换指令（游走→停留→跟随）。 */
    @Inject(
            method = {"mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/Wolf;setOrderedToSit(Z)V"
            ),
            cancellable = true
    )
    private void di_onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if(PetHomeMod.CONFIG.trinaryCommandSystem.get()){
            this.jumping = false;
            this.navigation.stop();
            this.setTarget((LivingEntity)null);
            player.swing(hand, true);
            cir.setReturnValue(this.playerSetCommand(player, this));
        }
    }

    public int getCommand(){
        return this.entityData.get(COMMAND);
    }

    public void setCommand(int i){
        this.entityData.set(COMMAND, i);
    }

    /** 注入 {@code Wolf#getTailAngle} 头部（可取消）：非愤怒且已驯服时按血量比例显式计算尾巴角度（公式与原版等价）。 */
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
        owner.displayClientMessage(Component.translatable("message.pet_home.command_" + command, name), true);
    }
}
