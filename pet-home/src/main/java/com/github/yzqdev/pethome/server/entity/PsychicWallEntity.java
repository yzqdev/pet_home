package com.github.yzqdev.pethome.server.entity;


import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

/**
 * psychic_wall 附魔的念力墙：按墙面朝向生成可伸缩碰撞盒（前 10 tick 生长动画），
 * 推开非友军实体、反弹敌方投射物（同主人友军的投射物放行），寿命到或创建者死后
 * 20 tick 内消散。碰撞/推挤/反弹行为配合 EntityMixin.canCollideWith 的同队豁免。
 */
public class PsychicWallEntity extends Entity {

    protected static final EntityDataAccessor<Direction> DIRECTION = SynchedEntityData.defineId(PsychicWallEntity.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Optional<UUID>> CREATOR_UUID = SynchedEntityData.defineId(PsychicWallEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> CREATOR_ID = SynchedEntityData.defineId(PsychicWallEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIFESPAN = SynchedEntityData.defineId(PsychicWallEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BLOCK_WIDTH = SynchedEntityData.defineId(PsychicWallEntity.class, EntityDataSerializers.INT);
    private final List<UUID> deflectedArrows = new ArrayList<>();
    private final Map<UUID, Integer> deflectedEntities = new HashMap<>();
    private int soundLoop = 0;

    public PsychicWallEntity(EntityType<?> type, Level level) {
        super(type, level);
    }


    /**
     * 生长/保持/衰亡状态机：按墙向推离碰撞盒内的非友军（投射物反向弹回），
     * 每实体 15 tick 反弹冷却（deflectedEntities 防重复）；创建者死后剩余寿命压到 20。
     */
    public void tick() {
        super.tick();
        if (this.tickCount <= 10 || this.getLifespan() <= 10) {
            this.setBoundingBox(this.makeBoundingBox());
        } else {
            if (soundLoop % 15 == 0) {
                this.playSound(PHSoundRegistry.PSYCHIC_WALL.get(), 1, random.nextFloat() * 0.3F + 0.9F);
            }
            soundLoop++;
        }
        if (!this.level().isClientSide && this.getCreatorId() != null) {
            Entity creator = this.getCreator();
            if (creator != null) {
                this.entityData.set(CREATOR_ID, creator.getId());
                if (!creator.isAlive() && this.getLifespan() > 20) {
                    this.setLifespan(20);
                }
            }
        }
        this.setDeltaMovement(Vec3.ZERO);
        float wX = Math.abs(this.getWallDirection().getStepX());
        float wY = Math.abs(this.getWallDirection().getStepY());
        float wZ = Math.abs(this.getWallDirection().getStepZ());
        AABB collisionAABB = this.getBoundingBox().inflate(wX * 0.25F, wY * 0.25F, wZ * 0.25F);
        List<Entity> colliders = level().getEntities(this, collisionAABB);
        float backISay = this.getWallDirection().getAxis() == Direction.Axis.Y ? -0.6F : -0.1F;
        for (Entity collider : colliders) {
            if (!isSameTeam(collider)) {
                collider.push(getWallDirection().getStepX() * 0.25F, getWallDirection().getStepY() * 0.25F, getWallDirection().getStepZ() * 0.25F);
                if (!deflectedEntities.containsKey(collider.getUUID())) {
                    boolean flag = true;
                    if (collider instanceof Projectile) {
                        if (isFiredByAlly((Projectile) collider)) {
                            flag = false;
                        } else {
                            collider.setDeltaMovement(collider.getDeltaMovement().scale(backISay));
                            collider.setYRot(collider.getYRot() + 180);
                            collider.setXRot(collider.getXRot() + 180);
                        }
                    }
                    if (flag) {
                        deflectedEntities.put(collider.getUUID(), 15);
                        if (level().isClientSide && collider.getBoundingBox().intersects(collisionAABB)) {
                            Vec3 vec3 = new Vec3(collider.getX(), collider.getY(0.5F), collider.getZ());
                            Vec3 vec31 = collisionAABB.getCenter();
                            Vec3 vec32;
                            EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(collider, vec3, vec31, collisionAABB, (entity) -> {
                                return entity == this;
                            }, collider.distanceTo(this));
                            if (entityhitresult == null || entityhitresult.getType() == HitResult.Type.MISS) {
                                vec32 = vec3;
                            } else {
                                vec32 = entityhitresult.getLocation();
                            }
                            level().addParticle(PHParticleRegistry.PSYCHIC_WALL.get(), vec32.x, vec32.y, vec32.z, this.getWallDirection().get3DDataValue(), 0, 0);
                            this.playSound(PHSoundRegistry.PSYCHIC_WALL_DEFLECT.get(), 1, random.nextFloat() * 0.3F + 0.9F);
                        }
                    }
                }
            }
        }
        if (!deflectedEntities.isEmpty()) {
            for (UUID uuid : deflectedEntities.keySet()) {
                deflectedEntities.put(uuid, deflectedEntities.get(uuid) - 1);
            }
            deflectedEntities.entrySet().removeIf(e -> e.getValue() <= 0);
        }
        if (getLifespan() <= 0) {
            this.discard();
        } else {
            this.setLifespan(this.getLifespan() - 1);
        }
    }

    private boolean isFiredByAlly(Projectile projectile) {
        Entity owner = this.getCreator();
        if (owner instanceof LivingEntity && projectile.getOwner() != null) {
            return TameableUtils.hasSameOwnerAs((LivingEntity) owner, projectile.getOwner());
        } else {
            return false;
        }
    }

    public boolean canBeCollidedWith() {
        return false;
    }

    public boolean hurt(DamageSource source, float f) {
        return false;
    }

    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LIFESPAN, 300);
        builder.define(BLOCK_WIDTH, 1);
        builder.define(CREATOR_UUID, Optional.empty());
        builder.define(CREATOR_ID, -1);
        builder.define(DIRECTION, Direction.UP);
    }


    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Lifespan")) {
            this.setLifespan(tag.getInt("Lifespan"));
        }
        if (tag.hasUUID("CreatorUUID")) {
            this.setCreatorId(tag.getUUID("CreatorUUID"));
        }
        this.setBlockWidth(tag.getInt("BlockWidth"));
        this.setWallDirection(Direction.from3DDataValue(tag.getInt("WallDirection")));

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.getCreatorId() != null) {
            tag.putUUID("CreatorUUID", this.getCreatorId());
        }
        tag.putInt("Lifespan", this.getLifespan());
        tag.putInt("BlockWidth", this.getBlockWidth());
        tag.putInt("WallDirection", this.getWallDirection().get3DDataValue());
    }

    public int getLifespan() {
        return this.entityData.get(LIFESPAN);
    }

    public void setLifespan(int i) {
        this.entityData.set(LIFESPAN, i);
    }

    public int getBlockWidth() {
        return this.entityData.get(BLOCK_WIDTH);
    }

    public void setBlockWidth(int i) {
        this.entityData.set(BLOCK_WIDTH, i);
    }

    public Direction getWallDirection() {
        return this.entityData.get(DIRECTION);
    }

    public void setWallDirection(Direction direction) {
        this.entityData.set(DIRECTION, direction);
    }

    @Nullable
    public UUID getCreatorId() {
        return this.entityData.get(CREATOR_UUID).orElse(null);
    }

    public void setCreatorId(@Nullable UUID uniqueId) {
        this.entityData.set(CREATOR_UUID, Optional.ofNullable(uniqueId));
    }


    /** 创建者解析：服务端按 UUID 查实体（跨维度稳定），客户端用同步的实体自增 id 槽 */
    public Entity getCreator() {
        if (!level().isClientSide()) {
            UUID id = getCreatorId();
            if (id != null) {
                return ((ServerLevel) level()).getEntity(id);
            }
            return null;
        } else {
            int id = this.entityData.get(CREATOR_ID);
            return id < 0 ? null : level().getEntity(id);
        }

    }

    public boolean canCollideWith(Entity entity) {
        return !this.isSameTeam(entity);
    }

    /** 墙面碰撞盒：随寿命/存活 tick 缩放（min(10, lifespan, tickCount)/10），按朝向确定展开轴 */
    protected AABB makeBoundingBox() {
        Direction direction2 = this.getWallDirection().getOpposite();
        float scale = Math.min(10, Math.min(this.getLifespan(), this.tickCount)) / 10F;
        float width = this.getBlockWidth() / 2F * scale;
        float minX = -0.15F;
        float minY = -0.15F;
        float minZ = -0.15F;
        float maxX = 0.15F;
        float maxY = 0.15F;
        float maxZ = 0.15F;
        switch (direction2) {
            case NORTH:
            case SOUTH:
                minX = -width;
                maxX = width;
                minY = -width;
                maxY = width;
                break;
            case EAST:
            case WEST:
                minZ = -width;
                maxZ = width;
                minY = -width;
                maxY = width;
                break;
            case UP:
            case DOWN:
                minX = -width;
                maxX = width;
                minZ = -width;
                maxZ = width;
                break;
        }
        return new AABB(this.getX() + minX, this.getY() + minY, this.getZ() + minZ, this.getX() + maxX, this.getY() + maxY, this.getZ() + maxZ);
    }

    public void setPos(double x, double y, double z) {
        this.setPosRaw(x, y, z);
        this.setBoundingBox(this.makeBoundingBox());
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> ac) {
        if (DIRECTION.equals(ac) || BLOCK_WIDTH.equals(ac)) {
            this.setBoundingBox(this.makeBoundingBox());
        }
        super.onSyncedDataUpdated(ac);
    }


    /** 与创建者同主人（或主人盟友）的实体视为同队：不推挤、可穿过 */
    public boolean isSameTeam(Entity entity) {
        Entity owner = this.getCreator();
        return owner instanceof LivingEntity && (TameableUtils.hasSameOwnerAs((LivingEntity) owner, entity) || owner.isAlliedTo(entity));
    }
}
