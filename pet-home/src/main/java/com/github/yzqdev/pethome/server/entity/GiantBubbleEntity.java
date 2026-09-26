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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;


public class GiantBubbleEntity extends Entity {

    private static final EntityDataAccessor<Integer> POPS_IN = SynchedEntityData.defineId(GiantBubbleEntity.class, EntityDataSerializers.INT);

    public GiantBubbleEntity(EntityType<?> type, Level level) {
        super(type, level);
    }


    @Override
    public void tick() {
        super.tick();
        double d = this.isInWater() ? 0.2D : 0.08D;
        this.move(MoverType.SELF, new Vec3(0, d, 0));
        if (getPopsIn() <= 0) {
            pop();
        } else {
            this.setpopsIn(this.getPopsIn() - 1);
            this.level().addParticle(PHParticleRegistry.SIMPLE_BUBBLE, this.getRandomX(1.4F), this.getRandomY(), this.getRandomZ(1.4F), (random.nextFloat() - 0.5F) * 0.3F, -0.1F, (random.nextFloat() - 0.5F) * 0.3F);
        }
    }

    @Override
    public void positionRider(Entity entity, MoveFunction moveFunction) {
        moveFunction.accept(entity, this.getX(), this.getBoundingBox().minY - 0.1, this.getZ());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float f) {
        if (source.is(DamageTypeTags.IS_PROJECTILE) && f > 0 || source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            this.pop();
            return true;
        }
        return false;
    }

    private void pop() {
        this.playSound(PHSoundRegistry.GIANT_BUBBLE_POP, 1.0F, 1.5F);
        if (!level().isClientSide()) {
            ((ServerLevel) this.level()).sendParticles(PHParticleRegistry.GIANT_POP, this.getX(), this.getY() + this.getBbHeight() * 0.5F, this.getZ(), 1, 0, 0, 0, 0);
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
    protected void readAdditionalSaveData(ValueInput tag) {
        this.setpopsIn(tag.getIntOr("PopsIn", 0));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        tag.putInt("PopsIn", this.getPopsIn());
    }

    public int getPopsIn() {
        return this.entityData.get(POPS_IN);
    }

    public void setpopsIn(int i) {
        this.entityData.set(POPS_IN, i);
    }


    // 迁移说明：shouldRiderSit() 是 NeoForge 为 Entity 增加的方法，Fabric（原版）没有对应钩子，
    // 原来的覆写在这里无法编译；「巨泡上不能下坐骑」的行为改由 ServerEvent 的骑乘事件处理（待接线）。
}
