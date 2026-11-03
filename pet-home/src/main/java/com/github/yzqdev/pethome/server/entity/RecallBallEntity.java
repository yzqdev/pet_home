package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.server.NbtKeys;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
import net.minecraft.core.registries.BuiltInRegistries;

import org.jetbrains.annotations.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * 往生球：紧急收回（TOTAL_RECALL）的载体实体。内置被收回宠物的类型 id 与完整实体 NBT
 * （经 SynchedEntityData 同步给客户端做开合动画）；主人右键打开后球体展开，宠物以
 * 25% 最大生命（至少 2 点）原地重建；除创造玩家外免疫一切伤害，落地不死（水中上浮、
 * 掉出世界底部拉回）。已完成释放的球不再写入存档（{@link #shouldBeSaved}）。
 */
public class RecallBallEntity extends Entity {

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> CONTAINED_ENTITY_TYPE = SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<CompoundTag> CONTAINED_ENTITY_DATA = SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Boolean> OPENED = SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean>  FINISHED = SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.BOOLEAN);
    private float prevOpenProgress;
    private float openProgress;

    public RecallBallEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_UUID, Optional.empty());
        this.entityData.define(CONTAINED_ENTITY_TYPE, "minecraft:pig");
        this.entityData.define(CONTAINED_ENTITY_DATA, new CompoundTag());
        this.entityData.define(OPENED, false);
        this.entityData.define(FINISHED, false);
    }

    public InteractionResult interactAt(Player player, Vec3 vec3, InteractionHand hand) {
        return interact(player, hand);
    }

    public boolean isPickable() {
        return !this.isFinished();
    }

    /** 主人右键打开（服务端记 OPENED）；无主（旧档遗留）直接消散。 */
    public InteractionResult interact(Player player, InteractionHand hand) {
        if(!this.isFinished()){
            if(this.getOwnerUUID() == null){
                this.discard();
            }else if(player.getUUID().equals(this.getOwnerUUID()) && !this.entityData.get(OPENED)){
                this.playSound(SoundEvents.ENDER_CHEST_OPEN, 1.0F, 1.5F);
                this.entityData.set(OPENED, true);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    /** 每 tick：自旋与开合动画、水中上浮、传送门粒子；完全打开后服务端按存下的 NBT 重建宠物（血量钳到 25%），释放完成且动画收拢后自毁。 */
    public void tick(){
        super.tick();
        this.setYRot(this.getYRot() + 1);
        this.setXRot(0);
        prevOpenProgress = openProgress;
        if(this.isInWaterOrBubble() || this.isInLava()){
            this.setPos(this.position().add(0, 0.08, 0));
        }
        if(this.entityData.get(OPENED) && openProgress < 1F){
            openProgress += 0.1F;
        }
        if(!this.entityData.get(OPENED) && openProgress > 0F){
            openProgress -= 0.1F;
        }
        if(random.nextFloat() < 0.4F){
            this.level().addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5D), this.getRandomY() - 0.25D, this.getRandomZ(0.5D), (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D);
        }
        if(this.getY() < level().getMinBuildHeight()){
            this.setPos(this.getX(), level().getMinBuildHeight() + 1.2F, this.getZ());
        }
        if(this.entityData.get(OPENED) && openProgress >= 1F && !this.isFinished()){
            if(!level().isClientSide()){
                EntityType type = BuiltInRegistries.ENTITY_TYPE.get( new ResourceLocation(this.getContainedEntityType()));
                if(type != null){
                    Entity entity = type.create(level());
                    if(entity instanceof LivingEntity alive){
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
        if(this.isFinished() && openProgress <= 0.01F){
            this.discard();
        }
    }

    public boolean isNoGravity() {
        return true;
    }

    /** 已完成释放的球不参与存档（重建出的实体接管后续）。 */
    public boolean shouldBeSaved() {
        return !this.isFinished() && super.shouldBeSaved();
    }

    public boolean isFinished() {
        return this.entityData.get(FINISHED);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundNBT) {
        UUID uuid;
        if (compoundNBT.hasUUID(NbtKeys.OWNER)) {
            uuid = compoundNBT.getUUID(NbtKeys.OWNER);
        } else {
            String s = compoundNBT.getString(NbtKeys.OWNER);
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setOwnerUUID(uuid);
            } catch (Throwable throwable) {
            }
        }
        this.setContainedEntityType(compoundNBT.getString(NbtKeys.CONTAINED_ENTITY_TYPE));
        if (!compoundNBT.getCompound(NbtKeys.CONTAINED_DATA).isEmpty()) {
            this.setContainedData(compoundNBT.getCompound(NbtKeys.CONTAINED_DATA));
        }
        this.entityData.set(FINISHED, compoundNBT.getBoolean(NbtKeys.FINISHED));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundNBT) {
        if (this.getOwnerUUID() != null) {
            compoundNBT.putUUID(NbtKeys.OWNER, this.getOwnerUUID());
        }
        compoundNBT.putString(NbtKeys.CONTAINED_ENTITY_TYPE, this.getContainedEntityType());
        compoundNBT.put(NbtKeys.CONTAINED_DATA, this.getContainedData());
        compoundNBT.putBoolean( NbtKeys.FINISHED, this.isFinished());
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse((UUID)null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public String getContainedEntityType(){
        return this.entityData.get(CONTAINED_ENTITY_TYPE);
    }

    public void setContainedEntityType(String containedEntityType) {
        this.entityData.set(CONTAINED_ENTITY_TYPE, containedEntityType);
    }

    public CompoundTag getContainedData(){
        return this.entityData.get(CONTAINED_ENTITY_DATA);
    }

    public void setContainedData(CompoundTag containedData) {
        this.entityData.set(CONTAINED_ENTITY_DATA, containedData);
    }

    public boolean isInvulnerableTo(DamageSource damageSource) {
        return damageSource.isCreativePlayer();
    }

    public float getOpenProgress(float f){
        return Mth.lerp(f, prevOpenProgress, openProgress);
    }
}
