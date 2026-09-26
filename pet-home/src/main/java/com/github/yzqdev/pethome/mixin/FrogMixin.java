package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.NbtKeys;



import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.util.PHEntityDataAccessors;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.server.entity.IFrog;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.util.TameableUtils;
import com.github.yzqdev.pethome.server.misc.PHTagRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(Frog.class)
public abstract class FrogMixin extends Animal implements ModifiedToBeTameable, IComandableMob, IFrog {

    // 26.1：defineId 必须在 <clinit> 完成（实体构造时 Builder 容量=已注册访问器数，晚注册会 AIOOBE）；
    // 且调用者类必须是实体类、实体类上不能有 @MixinMerged 的访问器字段——
    // 故注入 <clinit> 调用 defineId，访问器统一存到外部类 PHEntityDataAccessors。
    // owner 用原版 STRING 序列化器存 UUID 字符串，避免 <clinit> 期依赖 DeferredRegister 的自定义序列化器
    @Inject(
            method = {"<clinit>()V"},
            at = {@At("TAIL")},
            remap = false
    )
    private static void di_defineAccessors(CallbackInfo ci) {
        PHEntityDataAccessors.frogOwner = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.STRING);
        PHEntityDataAccessors.frogCommand = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.INT);
        PHEntityDataAccessors.frogTamed = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.BOOLEAN);
    }
    private boolean hasInitialDamage = false;
    protected FrogMixin(EntityType<? extends Animal> type, Level lvl) {
        super(type, lvl);
    }

    @Shadow
    public abstract Brain<Frog> getBrain();

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V"}
    )
    private void di_registerData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(PHEntityDataAccessors.frogOwner, "");
        builder.define(PHEntityDataAccessors.frogCommand, 0);
        builder.define(PHEntityDataAccessors.frogTamed, false);
    }


    @Inject(
            method = {"Lnet/minecraft/world/entity/animal/frog/Frog;tick()V"},
            at = {@At("TAIL")},
            remap = true
    )
    private void di_tick(CallbackInfo ci) {
        if(!hasInitialDamage && this.isTame()){
            hasInitialDamage = true;
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        }
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/frog/Frog;addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V"}
    )
    private void di_writeAdditional(net.minecraft.world.level.storage.ValueOutput compoundNBT, CallbackInfo ci) {
        compoundNBT.putInt(NbtKeys.DI_COMMAND, this.getCommand());
        compoundNBT.putBoolean(NbtKeys.TAMED, this.isTame());
        if (this.getTameOwnerUUID() != null) {
            compoundNBT.store(NbtKeys.OWNER, UUIDUtil.CODEC, this.getTameOwnerUUID());
        }
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/frog/Frog;readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V"}
    )
    private void di_readAdditional(net.minecraft.world.level.storage.ValueInput compoundNBT, CallbackInfo ci) {
        this.setCommand(compoundNBT.getIntOr(NbtKeys.DI_COMMAND, 0));
        this.setTame(compoundNBT.getBooleanOr(NbtKeys.TAMED, false));
        UUID uuid;
        if (compoundNBT.read(NbtKeys.OWNER, UUIDUtil.CODEC).isPresent()) {
            uuid = compoundNBT.read(NbtKeys.OWNER, UUIDUtil.CODEC).orElse(null);
        } else {
            String s = compoundNBT.getStringOr(NbtKeys.OWNER, "");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.level().getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setTameOwnerUUID(uuid);
                this.setTame(true);
            } catch (Throwable throwable) {
                this.setTame(false);
            }
        }
    }

    private void spawnTamingParticles(boolean smoke) {
        if (!level().isClientSide()) {
            ParticleOptions particleoptions = smoke ? ParticleTypes.SMOKE : ParticleTypes.HEART;
            for (int i = 0; i < 7; ++i) {
                double d0 = this.getRandom().nextGaussian() * 0.02D;
                double d1 = this.getRandom().nextGaussian() * 0.02D;
                double d2 = this.getRandom().nextGaussian() * 0.02D;
                ((ServerLevel) this.level()).sendParticles(particleoptions, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 3, d0, d1, d2, 0.03F);
            }
        }
    }

    public int getCommand() {
        return this.entityData.get(PHEntityDataAccessors.frogCommand);
    }

    public void setCommand(int i) {
        this.entityData.set(PHEntityDataAccessors.frogCommand, i);
    }

    public boolean isTame() {
        return this.entityData.get(PHEntityDataAccessors.frogTamed);
    }

    public void setTame(boolean b) {
        this.entityData.set(PHEntityDataAccessors.frogTamed, b);
    }

    @Nullable
    public UUID getTameOwnerUUID() {
        if (!PetHomeConfig.tameableFrog) {
            return null;
        }
        try {
            String s = this.entityData.get(PHEntityDataAccessors.frogOwner);
            return s.isEmpty() ? null : UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setTameOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(PHEntityDataAccessors.frogOwner, uuid == null ? "" : uuid.toString());
    }

    @Nullable
    public LivingEntity getTameOwner() {
        try {
            UUID uuid = this.getTameOwnerUUID();
            return uuid == null ? null : this.level().getPlayerByUUID(uuid);
        } catch (IllegalArgumentException illegalargumentexception) {
            return null;
        }
    }

    public boolean isFollowingOwner() {
        return this.getCommand() == 2 && PetHomeConfig.trinaryCommandSystem;
    }

    public boolean isStayingStill() {
        return this.getCommand() == 1 && PetHomeConfig.trinaryCommandSystem;
    }

    public boolean isValidAttackTarget(LivingEntity target) {
        if (this.isAlliedTo(target)) {
            return false;
        }
        if (this.getTameOwner() != null && this.getTameOwner().getLastHurtMob() != null && this.getTameOwner().getLastHurtMob().equals(target)) {
            return !TameableUtils.hasSameOwnerAs(this, target) && !this.isAlliedTo(target);
        }
        if (this.getTameOwner() != null && this.getTameOwner().getLastHurtByMob() != null && this.getTameOwner().getLastHurtByMob().equals(target)) {
            return !TameableUtils.hasSameOwnerAs(this, target) && !this.isAlliedTo(target);
        }
        return false;
    }

    @Override
    public void sendCommandMessage(Player owner, int command, Component name) {
        owner.sendOverlayMessage(Component.translatable("message.pet_home.command_" + command, name));
    }

    private boolean isTamingItem(ItemStack stack) {
        return stack.is(PHTagRegistry.TAME_FROGS_WITH);
    }

    @Override
    public boolean onFrogInteract(Player player, InteractionHand hand) {
        if (PetHomeConfig.tameableFrog) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (!this.isTame() && this.isTamingItem(itemStack)) {
                this.usePlayerItem(player, hand, itemStack);
                this.heal(2);
                this.playSound(SoundEvents.FROG_EAT, this.getSoundVolume(), this.getVoicePitch());
                if (!this.level().isClientSide()) {
                    if (this.getRandom().nextInt(4) == 0) {
                        this.spawnTamingParticles(true);
                    } else {
                        this.spawnTamingParticles(false);
                        this.setTame(true);
                        this.setTameOwnerUUID(player.getUUID());
                        if (player instanceof ServerPlayer) {
                            CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer) player, this);
                        }
                    }
                }
                return true;
            } else if (isTame() && itemStack.getItem() != Items.WATER_BUCKET) {
                if ((this.isTamingItem(itemStack) || itemStack.is(Items.SLIME_BALL)) && this.getHealth() < this.getMaxHealth()) {
                    this.heal(2);
                    this.playSound(SoundEvents.FROG_EAT, this.getSoundVolume(), this.getVoicePitch());
                    return true;
                } else if (PetHomeConfig.trinaryCommandSystem) {
                    player.swing(hand, true);
                    this.playerSetCommand(player, this);
                    return false;
                }
            }
        }
        return false;
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/frog/Frog;customServerAiStep(Lnet/minecraft/server/level/ServerLevel;)V"}
    )
    private void di_customServerAiStep(net.minecraft.server.level.ServerLevel level, CallbackInfo ci) {
        if (this.isTame() && this.getTameOwner() != null) {
            if (this.getTameOwner().getLastHurtMob() != null && this.getTameOwner().getLastHurtMob().isAlive() && !TameableUtils.hasSameOwnerAs(this, this.getTameOwner().getLastHurtMob())) {
                this.setTarget(this.getTameOwner().getLastHurtMob());
                this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, this.getTameOwner().getLastHurtMob());
            }
            if (this.getTameOwner().getLastHurtByMob() != null && this.getTameOwner().getLastHurtByMob().isAlive() && !TameableUtils.hasSameOwnerAs(this, this.getTameOwner().getLastHurtByMob())) {
                this.setTarget(this.getTameOwner().getLastHurtByMob());
                this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, this.getTameOwner().getLastHurtByMob());
            }
        }
    }

}
