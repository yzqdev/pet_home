package com.github.yzqdev.pethome.server.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;


/**
 * 矿石嗅探的高亮框实体（由 {@code TameableUtils.detectRandomOres} 生成在矿石位置）：
 * 常亮发光、无重力、免伤，寿命（{@code LIFESPAN}，等级×50 tick）倒计时归零或所在方块
 * 变为空气时消失。客户端按 {@code RenderHighlightedBlock} 画线框。
 */
public class HighlightedBlockEntity extends Entity {

    private static final EntityDataAccessor<Integer> LIFESPAN = SynchedEntityData.defineId(HighlightedBlockEntity.class, EntityDataSerializers.INT);

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
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LIFESPAN, 20);
    }


    @Override
    protected void readAdditionalSaveData(ValueInput tag) {

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {

    }

    public int getLifespan() {
        return this.entityData.get(LIFESPAN);
    }

    public void setLifespan(int i) {
        this.entityData.set(LIFESPAN, i);
    }


    public boolean shouldRiderSit() {
        return false;
    }

    public BlockState getBlockState() {
        return this.level().getBlockState(this.blockPosition());
    }

    public boolean isCurrentlyGlowing() {
        return true;
    }
}
