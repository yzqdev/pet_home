package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.server.NbtKeys;

import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
 * 巨型泡泡（BUBBLING）载体实体：把目标骑在上面持续上浮（水中更快），{@code popsIn} 到期或被投掷物 /
 * 虚空伤害击中时破裂释放乘客；无重力、免大部分伤害（本类 hurt 只处理破裂情形）。
 * 下车被 {@code ServerEvent.onEntityMount} 拦截。
 */
public class GiantBubbleEntity extends Entity {

    private static final EntityDataAccessor<Integer> POPS_IN = SynchedEntityData.defineId(GiantBubbleEntity.class, EntityDataSerializers.INT);

    public GiantBubbleEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    /** 每 tick：上浮、倒数破裂计时、冒气泡粒子；计时归零时 {@link #pop}。 */
    public void tick(){
        super.tick();
        double d = this.isInWater() ? 0.2D : 0.08D;
        this.move(MoverType.SELF, new Vec3(0, d, 0));
        if(getPopsIn() <= 0){
            pop();
        }else{
            this.setpopsIn(this.getPopsIn() - 1);
            this.level().addParticle(PHParticleRegistry.SIMPLE_BUBBLE, this.getRandomX(1.4F), this.getRandomY(), this.getRandomZ(1.4F), (random.nextFloat() - 0.5F) * 0.3F, -0.1F, (random.nextFloat() - 0.5F) * 0.3F);
        }
    }

    public void positionRider(Entity entity, Entity.MoveFunction moveFunction) {
        moveFunction.accept(entity, this.getX(), this.getBoundingBox().minY - 0.1, this.getZ());
    }

    public boolean hurt(DamageSource source, float f) {
        if(source.is(DamageTypeTags.IS_PROJECTILE) && f > 0 || source.is(DamageTypes.FELL_OUT_OF_WORLD)){
            this.pop();
            return true;
        }
        return false;
    }

    /** 破裂：音效 + 服务端 GIANT_POP 粒子，弹出手上的目标后自毁。 */
    private void pop() {
        this.playSound(PHSoundRegistry.GIANT_BUBBLE_POP, 1.0F, 1.5F);
        if(!level().isClientSide()){
            ((ServerLevel)this.level()).sendParticles(PHParticleRegistry.GIANT_POP, this.getX(), this.getY() + this.getBbHeight() * 0.5F, this.getZ(), 1, 0, 0, 0, 0);
        }
        this.ejectPassengers();
        this.discard();
    }

    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(POPS_IN, 20);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setpopsIn(tag.getInt(NbtKeys.POPS_IN));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(NbtKeys.POPS_IN, this.getPopsIn());
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
