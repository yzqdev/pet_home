package com.github.yzqdev.pethome.server.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 矿脉嗅探附魔的矿石高亮框：以方块实体形态常驻（无重力、常亮发光），寿命（LIFESPAN， 默认 20 tick）倒数归零或所标记方块变为空气时消失；轮廓颜色由客户端 {@code
 * OreColorRegistry} 决定。
 */
public class HighlightedBlockEntity extends Entity {

  /** 同步数据：剩余寿命 tick 数（默认 20），归零后消失。 */
  private static final EntityDataAccessor<Integer> LIFESPAN =
      SynchedEntityData.defineId(HighlightedBlockEntity.class, EntityDataSerializers.INT);

  public HighlightedBlockEntity(EntityType<?> type, Level level) {
    super(type, level);
  }

  @Override
  public void tick() {
    super.tick();
    if (this.getBlockState().isAir()) {
      this.discard();
    }
    if (getLifespan() <= 0) {
      this.discard();
    } else {
      this.setLifespan(this.getLifespan() - 1);
    }
  }

  @Override
  public boolean isNoGravity() {
    return true;
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    builder.define(LIFESPAN, 20);
  }

  @Override
  protected void readAdditionalSaveData(CompoundTag tag) {}

  @Override
  protected void addAdditionalSaveData(CompoundTag tag) {}

  public int getLifespan() {
    return this.entityData.get(LIFESPAN);
  }

  public void setLifespan(int i) {
    this.entityData.set(LIFESPAN, i);
  }

  public boolean shouldRiderSit() {
    return false;
  }

  /** 读取所标记位置的当前方块状态（变为空气时在 tick 中消失）。 */
  public BlockState getBlockState() {
    return this.level().getBlockState(this.blockPosition());
  }

  /** 常亮发光以实现矿石轮廓高亮。 */
  public boolean isCurrentlyGlowing() {
    return true;
  }
}
