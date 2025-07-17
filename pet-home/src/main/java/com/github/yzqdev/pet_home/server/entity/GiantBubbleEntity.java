package com.github.yzqdev.pet_home.server.entity;


import com.github.yzqdev.pet_home.server.misc.PHParticleRegistry;
import com.github.yzqdev.pet_home.server.misc.PHSoundRegistry;
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


public class GiantBubbleEntity extends Entity {

    private static final EntityDataAccessor<Integer> POPS_IN = SynchedEntityData.defineId(GiantBubbleEntity.class, EntityDataSerializers.INT);

    public GiantBubbleEntity(EntityType<?> type, Level level) {
        super(type, level);
    }



    public void tick(){
        super.tick();
        double d = this.isInWater() ? 0.2D : 0.08D;
        this.move(MoverType.SELF, new Vec3(0, d, 0));
        if(getPopsIn() <= 0){
            pop();
        }else{
            this.setpopsIn(this.getPopsIn() - 1);
            this.level().addParticle(PHParticleRegistry.SIMPLE_BUBBLE.get(), this.getRandomX(1.4F), this.getRandomY(), this.getRandomZ(1.4F), (random.nextFloat() - 0.5F) * 0.3F, -0.1F, (random.nextFloat() - 0.5F) * 0.3F);
        }
    }

    public void positionRider(Entity entity, MoveFunction moveFunction) {
        moveFunction.accept(entity, this.getX(), this.getBoundingBox().minY - 0.1, this.getZ());
    }

    public boolean hurt(DamageSource source, float f) {
        if(source.is(DamageTypeTags.IS_PROJECTILE) && f > 0 || source.is(DamageTypes.FELL_OUT_OF_WORLD)){
            this.pop();
            return true;
        }
        return false;
    }

    private void pop() {
        this.playSound(PHSoundRegistry.GIANT_BUBBLE_POP.get(), 1.0F, 1.5F);
        if(!level().isClientSide){
            ((ServerLevel)this.level()).sendParticles(PHParticleRegistry.GIANT_POP.get(), this.getX(), this.getY() + this.getBbHeight() * 0.5F, this.getZ(), 1, 0, 0, 0, 0);
        }
        this.ejectPassengers();
        this.discard();
    }

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



    public boolean shouldRiderSit(){
        return false;
    }
}
