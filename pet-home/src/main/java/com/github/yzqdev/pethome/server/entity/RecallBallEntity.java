package com.github.yzqdev.pethome.server.entity;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 往生球（紧急收回附魔的载体）：内部保存被收回宠物的实体类型与完整 NBT。 无重力、免伤、可存档；只有主人能右键"打开"（开合进度由客户端渲染）， 打满后按 NBT 重建宠物（至少 25%
 * 血量）并移除自身；浮空/水上缓慢上浮，掉出世界自动回弹。
 */
public class RecallBallEntity extends Entity {

  /** 主人 UUID；经 SynchedEntityData 同步，客户端用于绘制归属与开合判定。 */
  private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
      SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.OPTIONAL_UUID);

  /** 球内宠物的实体类型 id（字符串形式，如 {@code pet_home:grizzly_bear}），同步给客户端做贴图选择。 */
  private static final EntityDataAccessor<String> CONTAINED_ENTITY_TYPE =
      SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.STRING);

  /** 球内宠物的完整实体 NBT，重建宠物时作为数据源。 */
  private static final EntityDataAccessor<CompoundTag> CONTAINED_ENTITY_DATA =
      SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.COMPOUND_TAG);

  /** 是否已被主人触发打开（驱动客户端开合动画）。 */
  private static final EntityDataAccessor<Boolean> OPENED =
      SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.BOOLEAN);

  /** 宠物已重建完成；FINISHED 且开合动画合拢后移除自身。 */
  private static final EntityDataAccessor<Boolean> FINISHED =
      SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.BOOLEAN);

  /** 上一 tick 的开合进度（客户端插值用）与当前开合进度 [0,1]。 */
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

  public InteractionResult interactAt(Player player, Vec3 vec3, InteractionHand hand) {
    return interact(player, hand);
  }

  public boolean isPickable() {
    return !this.isFinished();
  }

  /** 只有主人能打开（播末影箱音效并置 OPENED）；无主球任何人可触发丢弃。 */
  public InteractionResult interact(Player player, InteractionHand hand) {
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

  /** 每 tick：旋转与开合进度推进、水上上浮、PORTAL 粒子；OPENED 且开满时在服务端按 NBT 重建宠物并标记 FINISHED。 */
  public void tick() {
    super.tick();
    this.setYRot(this.getYRot() + 1);
    this.setXRot(0);
    prevOpenProgress = openProgress;
    if (this.isInWaterOrBubble() || this.isInLava()) {
      this.setPos(this.position().add(0, 0.08, 0));
    }
    if (this.entityData.get(OPENED) && openProgress < 1F) {
      openProgress += 0.1F;
    }
    if (!this.entityData.get(OPENED) && openProgress > 0F) {
      openProgress -= 0.1F;
    }
    if (random.nextFloat() < 0.4F) {
      this.level()
          .addParticle(
              ParticleTypes.PORTAL,
              this.getRandomX(0.5D),
              this.getRandomY() - 0.25D,
              this.getRandomZ(0.5D),
              (this.random.nextDouble() - 0.5D) * 2.0D,
              -this.random.nextDouble(),
              (this.random.nextDouble() - 0.5D) * 2.0D);
    }
    if (this.getY() < level().getMinBuildHeight()) {
      this.setPos(this.getX(), level().getMinBuildHeight() + 1.2F, this.getZ());
    }
    if (this.entityData.get(OPENED) && openProgress >= 1F && !this.isFinished()) {
      if (!level().isClientSide()) {
        EntityType<?> type =
            BuiltInRegistries.ENTITY_TYPE.get(
                ResourceLocation.parse(this.getContainedEntityType()));
        if (type != null) {
          Entity entity = type.create(level());
          if (entity instanceof LivingEntity alive) {
            alive.readAdditionalSaveData(this.getContainedData());
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
  protected void readAdditionalSaveData(CompoundTag compoundNBT) {
    UUID uuid;
    if (compoundNBT.hasUUID("Owner")) {
      uuid = compoundNBT.getUUID("Owner");
    } else {
      String s = compoundNBT.getString("Owner");
      uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
    }

    if (uuid != null) {
      try {
        this.setOwnerUUID(uuid);
      } catch (Throwable throwable) {
      }
    }
    this.setContainedEntityType(compoundNBT.getString("ContainedEntityType"));
    if (!compoundNBT.getCompound("ContainedData").isEmpty()) {
      this.setContainedData(compoundNBT.getCompound("ContainedData"));
    }
    this.entityData.set(FINISHED, compoundNBT.getBoolean("Finished"));
  }

  @Override
  protected void addAdditionalSaveData(CompoundTag compoundNBT) {
    if (this.getOwnerUUID() != null) {
      compoundNBT.putUUID("Owner", this.getOwnerUUID());
    }
    compoundNBT.putString("ContainedEntityType", this.getContainedEntityType());
    compoundNBT.put("ContainedData", this.getContainedData());
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
