package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 巨型泡泡（泡泡控制附魔的载体）：载着目标生物每 tick 上浮，免伤（投掷物或掉出世界才会破）， {@code popsIn} 倒计时归零或被击破时 {@link #pop()}（音效 +
 * GIANT_POP 粒子 + 弹出乘客）。
 */
public class GiantBubbleEntity extends Entity {

  /** 距自动破裂的剩余 tick 数，默认 20；经 SynchedEntityData 同步给客户端做缩放表现。 */
  private static final EntityDataAccessor<Integer> POPS_IN =
      SynchedEntityData.defineId(GiantBubbleEntity.class, EntityDataSerializers.INT);

  public GiantBubbleEntity(EntityType<?> type, Level level) {
    super(type, level);
  }

  @Override
  public void tick() {
    super.tick();
    // 每 tick 自行上浮：水中浮力更大；仅服务端权威调用，客户端由同步驱动
    double d = this.isInWater() ? 0.2D : 0.08D;
    this.move(MoverType.SELF, new Vec3(0, d, 0));
    if (getPopsIn() <= 0) {
      pop();
    } else {
      this.setpopsIn(this.getPopsIn() - 1);
      this.level()
          .addParticle(
              PHParticleRegistry.SIMPLE_BUBBLE,
              this.getRandomX(1.4F),
              this.getRandomY(),
              this.getRandomZ(1.4F),
              (random.nextFloat() - 0.5F) * 0.3F,
              -0.1F,
              (random.nextFloat() - 0.5F) * 0.3F);
    }
  }

  @Override
  public void positionRider(Entity entity, MoveFunction moveFunction) {
    moveFunction.accept(entity, this.getX(), this.getBoundingBox().minY - 0.1, this.getZ());
  }

  @Override
  public boolean hurt(DamageSource source, float f) {
    if (source.is(DamageTypeTags.IS_PROJECTILE) && f > 0
        || source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
      this.pop();
      return true;
    }
    return false;
  }

  /** 破裂：播破裂音效与粒子（仅服务端），弹出乘客并移除自身。 */
  private void pop() {
    this.playSound(PHSoundRegistry.GIANT_BUBBLE_POP, 1.0F, 1.5F);
    if (!level().isClientSide()) {
      ((ServerLevel) this.level())
          .sendParticles(
              PHParticleRegistry.GIANT_POP,
              this.getX(),
              this.getY() + this.getBbHeight() * 0.5F,
              this.getZ(),
              1,
              0,
              0,
              0,
              0);
    }
    this.ejectPassengers();
    this.discard();
  }

  @Override
  public boolean isNoGravity() {
    return true;
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    builder.define(POPS_IN, 20);
  }

  @Override
  protected void readAdditionalSaveData(CompoundTag tag) {
    this.setpopsIn(tag.getInt("PopsIn"));
  }

  @Override
  protected void addAdditionalSaveData(CompoundTag tag) {
    tag.putInt("PopsIn", this.getPopsIn());
  }

  public int getPopsIn() {
    return this.entityData.get(POPS_IN);
  }

  public void setpopsIn(int i) {
    this.entityData.set(POPS_IN, i);
  }

  // NeoForge 专属钩子：是否让乘客呈骑乘坐姿；泡泡里应保持被包裹姿态，返回 false
  public boolean shouldRiderSit() {
    return false;
  }
}
