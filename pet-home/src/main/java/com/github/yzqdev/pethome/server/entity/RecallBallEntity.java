package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.util.PHAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class RecallBallEntity extends Entity {

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(RecallBallEntity.class, PHAttachments.OPTIONAL_UUID.get());
    private static final EntityDataAccessor<String> CONTAINED_ENTITY_TYPE = SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<CompoundTag> CONTAINED_ENTITY_DATA = SynchedEntityData.defineId(RecallBallEntity.class, PHAttachments.COMPOUND_TAG.get());
    private static final EntityDataAccessor<Boolean> OPENED = SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FINISHED = SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.BOOLEAN);
    private float prevOpenProgress;
    private float openProgress;

    public RecallBallEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(CONTAINED_ENTITY_TYPE, "minecraft:pig");
        builder.define(CONTAINED_ENTITY_DATA, new CompoundTag());
        builder.define(OPENED, false);
        builder.define(FINISHED, false);
    }


    // 26.1: 原 interact(Player, InteractionHand) 与 interactAt(Player, Vec3, InteractionHand) 已合并为本方法，
    // 旧签名不会被调用，必须用 @Override 确保签名正确
    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (!this.isFinished()) {
            if (this.getOwnerUUID() == null) {
                this.discard();
            } else if (player.getUUID().equals(this.getOwnerUUID()) && !this.entityData.get(OPENED)) {
                this.playSound(SoundEvents.ENDER_CHEST_OPEN, 1.0F, 1.5F);
                this.entityData.set(OPENED, true);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public boolean isPickable() {
        return !this.isFinished();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // 召回球不可被破坏（与 isInvulnerableTo 保持一致）
        return false;
    }

    public void tick() {
        super.tick();
        this.setYRot(this.getYRot() + 1);
        this.setXRot(0);
        prevOpenProgress = openProgress;
        if (this.isInWater() || this.isInLava()) {
            this.setPos(this.position().add(0, 0.08, 0));
        }
        if (this.entityData.get(OPENED) && openProgress < 1F) {
            openProgress += 0.1F;
        }
        if (!this.entityData.get(OPENED) && openProgress > 0F) {
            openProgress -= 0.1F;
        }
        if (random.nextFloat() < 0.4F) {
            this.level().addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5D), this.getRandomY() - 0.25D, this.getRandomZ(0.5D), (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D);
        }
        if (this.getY() < level().getMinY()) {
            this.setPos(this.getX(), level().getMinY() + 1.2F, this.getZ());
        }
        if (this.entityData.get(OPENED) && openProgress >= 1F && !this.isFinished()) {
            if (!level().isClientSide()) {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(this.getContainedEntityType()));
                if (type != null) {
                    Entity entity = type.create(level(), EntitySpawnReason.TRIGGERED);
                    if (entity instanceof LivingEntity alive) {
                        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), PHConstants.LOG)) {
                            ValueInput input = TagValueInput.create(reporter, level().registryAccess(), this.getContainedData());
                            alive.load(input);
                        }
                        alive.setHealth(Math.max(2, alive.getMaxHealth() * 0.25F));
                        alive.setYRot(random.nextFloat() * 360 - 180);
                        alive.copyPosition(this);
                        level().addFreshEntity(alive);
                    }
                    this.entityData.set(FINISHED, true);
                    this.entityData.set(OPENED, false);
                }
            }
        }
        if (this.isFinished() && openProgress <= 0.01F) {
            this.discard();
        }
    }

    public boolean isNoGravity() {
        return true;
    }

    public boolean shouldBeSaved() {
        return !this.isFinished() && super.shouldBeSaved();
    }

    public boolean isFinished() {
        return this.entityData.get(FINISHED);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput compoundNBT) {
        UUID uuid;
        Optional<UUID> owner = compoundNBT.read("Owner", UUIDUtil.CODEC);
        if (owner.isPresent()) {
            uuid = owner.get();
        } else {
            String s = compoundNBT.getStringOr("Owner", "");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.level().getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setOwnerUUID(uuid);
            } catch (Throwable throwable) {
            }
        }
        this.setContainedEntityType(compoundNBT.getStringOr("ContainedEntityType", ""));
        CompoundTag contained = compoundNBT.read("ContainedData", CompoundTag.CODEC).orElse(null);
        if (contained != null && !contained.isEmpty()) {
            this.setContainedData(contained);
        }
        this.entityData.set(FINISHED, compoundNBT.getBooleanOr("Finished", false));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput compoundNBT) {
        if (this.getOwnerUUID() != null) {
            compoundNBT.store("Owner", UUIDUtil.CODEC, this.getOwnerUUID());
        }
        compoundNBT.putString("ContainedEntityType", this.getContainedEntityType());
        compoundNBT.store("ContainedData", CompoundTag.CODEC, this.getContainedData());
        compoundNBT.putBoolean("Finished", this.isFinished());
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse((UUID) null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public String getContainedEntityType() {
        return this.entityData.get(CONTAINED_ENTITY_TYPE);
    }

    public void setContainedEntityType(String containedEntityType) {
        this.entityData.set(CONTAINED_ENTITY_TYPE, containedEntityType);
    }

    public CompoundTag getContainedData() {
        return this.entityData.get(CONTAINED_ENTITY_DATA);
    }

    public void setContainedData(CompoundTag containedData) {
        this.entityData.set(CONTAINED_ENTITY_DATA, containedData);
    }

    public boolean isInvulnerableTo(DamageSource damageSource) {
        return damageSource.isCreativePlayer();
    }

    public float getOpenProgress(float f) {
        return Mth.lerp(f, prevOpenProgress, openProgress);
    }
}
