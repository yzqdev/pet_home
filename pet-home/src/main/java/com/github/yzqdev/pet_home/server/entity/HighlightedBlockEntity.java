package com.github.yzqdev.pet_home.server.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;



public class HighlightedBlockEntity extends Entity {

    private static final EntityDataAccessor<Integer> LIFESPAN = SynchedEntityData.defineId(HighlightedBlockEntity.class, EntityDataSerializers.INT);

    public HighlightedBlockEntity(EntityType<?> type, Level level) {
        super(type, level);
    }



    public void tick() {
        super.tick();
        if(this.getBlockState().isAir()){
            this.discard();
        }
        if(getLifespan() <= 0){
            this.discard();
        }else{
           this.setLifespan(this.getLifespan() - 1);
        }
    }

    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LIFESPAN,20);
    }



    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {

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
