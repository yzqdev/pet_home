package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.NbtKeys;

import com.github.yzqdev.pethome.server.entity.IComandableMob;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import com.github.yzqdev.pethome.server.entity.ai.*;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@Mixin(Rabbit.class)
public abstract class RabbitMixin extends Animal implements ModifiedToBeTameable, IComandableMob {

    @Shadow @Final private static EntityDataAccessor<Integer> DATA_TYPE_ID;

    @Shadow public abstract Rabbit.Variant getVariant();

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> COMMAND = SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> TAMED = SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.BOOLEAN);

    protected RabbitMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"registerGoals()V"}
    )
    private void di_registerGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(1, new Sit2Goal(this));
        this.goalSelector.addGoal(2, new FollowOwner2Goal(this, 2.0D, 10.0F, 3.0F, false));
        this.targetSelector.addGoal(2, new OwnerHurtTarget2Goal(this));
        this.targetSelector.addGoal(3, new OwnerHurtByTarget2Goal(this));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0D, Ingredient.of(Items.HAY_BLOCK), false));
        if(isTame()){
            removeUntamedGoals();
        }
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"defineSynchedData()V"}
    )
    private void di_registerData(CallbackInfo ci) {
        this.entityData.define(OWNER_UUID, Optional.empty());
        this.entityData.define(COMMAND, 0);
        this.entityData.define(TAMED, false);
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"}
    )
    private void di_writeAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        compoundNBT.putInt(NbtKeys.DI_COMMAND, this.getCommand());
        compoundNBT.putBoolean(NbtKeys.TAMED, this.isTame());
        if (this.getTameOwnerUUID() != null) {
            compoundNBT.putUUID(NbtKeys.OWNER, this.getTameOwnerUUID());
        }
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"}
    )
    private void di_readAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        this.setCommand(compoundNBT.getInt(NbtKeys.DI_COMMAND));
        this.setTame(compoundNBT.getBoolean(NbtKeys.TAMED));
        UUID uuid;
        if (compoundNBT.hasUUID(NbtKeys.OWNER)) {
            uuid = compoundNBT.getUUID(NbtKeys.OWNER);
        } else {
            String s = compoundNBT.getString(NbtKeys.OWNER);
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
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

    public int getCommand(){
        return this.entityData.get(COMMAND);
    }

    public void setCommand(int i){
        this.entityData.set(COMMAND, i);
    }

    public boolean isTame(){
        return this.entityData.get(TAMED);
    }

    public void setTame(boolean b){
        this.entityData.set(TAMED, b);
        if(b){
            removeUntamedGoals();
        }
    }

    @Nullable
    public UUID getTameOwnerUUID() {
        return PetHomeMod.CONFIG.tameableRabbit.get() ? this.entityData.get(OWNER_UUID).orElse((UUID)null) : null;
    }

    public void setTameOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
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

    public boolean isFollowingOwner(){
        return this.getCommand() == 2 && PetHomeMod.CONFIG.trinaryCommandSystem.get();
    }

    public boolean isStayingStill(){
        return this.getCommand() == 1 && PetHomeMod.CONFIG.trinaryCommandSystem.get();
    }

    public boolean isValidAttackTarget(LivingEntity target) {
        return this.getVariant() == Rabbit.Variant.EVIL && (!this.isTame() || !TameableUtils.hasSameOwnerAs(this, target));
    }

    public void removeUntamedGoals(){
        try {
            this.goalSelector.getAvailableGoals().stream().filter((wrapped) -> {
                return wrapped.getGoal() instanceof AvoidEntityGoal;
            }).filter(WrappedGoal::isRunning).forEach(WrappedGoal::stop);
            this.goalSelector.getAvailableGoals().removeIf((wrapped) -> {
                return wrapped.getGoal() instanceof AvoidEntityGoal;
            });
            this.targetSelector.getAvailableGoals().removeIf((wrapped) -> {
                return wrapped.getGoal() instanceof NearestAttackableTargetGoal;
            });
        } catch (Exception e){
            PetHomeMod.LOGGER.warn("encountered error modifying rabbit AI");
        }
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"setVariant(Lnet/minecraft/world/entity/animal/Rabbit$Variant;)V"},
            cancellable = true
    )
    private void di_setRabbitType(Rabbit.Variant type, CallbackInfo ci) {
        ci.cancel();
        if(type == Rabbit.Variant.EVIL){
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(30.0D);
            this.getAttribute(Attributes.ARMOR).setBaseValue(8.0D);
            // 不在此处回血：原版 readAdditionalSaveData 会经 setVariant 走到这里，加载期
            // （worldgen 线程、实体未入 level）发 LivingHealEvent 会让第三方监听器在离线实体上运行；
            // 且每次 NBT 加载叠加回血等于无限回血。满血由 di_finalizeSpawn 在 spawn 场景补足。
            this.goalSelector.addGoal(4, new RabbitMeleeGoal(this));
            this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
            if(!this.isTame()){
                this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
                this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Wolf.class, true));
            }else{
                this.targetSelector.addGoal(2, new OwnerHurtTarget2Goal(this));
                this.targetSelector.addGoal(3, new OwnerHurtByTarget2Goal(this));
                removeUntamedGoals();
            }
            if (!this.hasCustomName()) {
                this.setCustomName(Component.translatable(Util.makeDescriptionId("entity", ResourceLocation.withDefaultNamespace("killer_bunny"))));
            }
        }
        this.entityData.set(DATA_TYPE_ID, type.id());
    }

    /**
     * 注入目标：{@code Rabbit#finalizeSpawn} RETURN。
     *
     * <p>spawn 场景（自然生成/刷怪蛋/summon 无 NBT/结构模板放置）补满血：实体构造时血量按普通兔 3 点，
     * {@code setVariant(EVIL)} 把 MAX_HEALTH 提到 30 后需要覆盖为满血。直接 {@code setHealth} 不发
     * LivingHealEvent；NBT 加载路径不走 finalizeSpawn，存档血量得以保留。
     */
    @Inject(
            at = {@At("RETURN")},
            remap = true,
            method = {"finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/entity/SpawnGroupData;"}
    )
    private void di_finalizeSpawn(CallbackInfoReturnable<SpawnGroupData> cir) {
        if (this.getVariant() == Rabbit.Variant.EVIL) {
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public void sendCommandMessage(Player owner, int command, Component name) {
        owner.displayClientMessage(Component.translatable("message.pet_home.command_" + command, name), true);
    }
}
