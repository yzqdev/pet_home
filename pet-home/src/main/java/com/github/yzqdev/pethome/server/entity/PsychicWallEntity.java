package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.server.NbtKeys;

import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
import net.minecraft.world.phys.*;

import org.jetbrains.annotations.Nullable;
import java.util.*;

/**
 * 念力墙（PSYCHIC_WALL）：立在宠物/主人与威胁之间的力场墙——宽度 {@code blockWidth}（等级+1 格）、
 * 法线朝向 {@code wallDirection}、寿命 {@code lifespan}（等级×100 tick）；出现/消散各 10 tick 的
 * 包围盒生长动画。阻挡并击退 / 反弹非同阵营实体，投射物调头弹回（友方投射物直接放行）；免疫伤害、
 * 无重力；施法者死亡时寿命被压到 20 tick 快速消散。包围盒 / 朝向 / 宽度 / 寿命随 NBT 持久化。
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

    /** 每 tick：动画期重建包围盒 / 循环音效；服务端同步施法者实体 ID 并处理其死亡；对接触的非同阵营实体施加推力（投射物调头），逐实体 15 tick 反弹去重窗口；寿命倒计时后自毁。 */
    public void tick() {
        super.tick();
        if (this.tickCount <= 10 || this.getLifespan() <= 10) {
            this.setBoundingBox(this.makeBoundingBox());
        } else {
            if (soundLoop % 15 == 0) {
                this.playSound(PHSoundRegistry.PSYCHIC_WALL, 1, random.nextFloat() * 0.3F + 0.9F);
            }
            soundLoop++;
        }
        if (!this.level().isClientSide() && this.getCreatorId() != null) {
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
                        if (level().isClientSide() && collider.getBoundingBox().intersects(collisionAABB)) {
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
                            level().addParticle(PHParticleRegistry.PSYCHIC_WALL, vec32.x, vec32.y, vec32.z, this.getWallDirection().get3DDataValue(), 0, 0);
                            this.playSound(PHSoundRegistry.PSYCHIC_WALL_DEFLECT, 1, random.nextFloat() * 0.3F + 0.9F);
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

    /** 判定投射物是否由墙主人或其同主人单位发射（友方投射物不反弹，直接穿过）。 */
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
    protected void defineSynchedData() {
        this.entityData.define(LIFESPAN, 300);
        this.entityData.define(BLOCK_WIDTH, 1);
        this.entityData.define(CREATOR_UUID, Optional.empty());
        this.entityData.define(CREATOR_ID, -1);
        this.entityData.define(DIRECTION, Direction.UP);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(NbtKeys.LIFESPAN)) {
            this.setLifespan(tag.getInt(NbtKeys.LIFESPAN));
        }
        if (tag.hasUUID(NbtKeys.CREATOR_UUID)) {
            this.setCreatorId(tag.getUUID(NbtKeys.CREATOR_UUID));
        }
        this.setBlockWidth(tag.getInt(NbtKeys.BLOCK_WIDTH));
        this.setWallDirection(Direction.from3DDataValue(tag.getInt(NbtKeys.WALL_DIRECTION)));

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.getCreatorId() != null) {
            tag.putUUID(NbtKeys.CREATOR_UUID, this.getCreatorId());
        }
        tag.putInt(NbtKeys.LIFESPAN, this.getLifespan());
        tag.putInt(NbtKeys.BLOCK_WIDTH, this.getBlockWidth());
        tag.putInt(NbtKeys.WALL_DIRECTION, this.getWallDirection().get3DDataValue());
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


    /** 解析墙主人：服务端按 UUID 查实体，客户端按同步来的实体 ID 查（两者都经 SynchedEntityData 携带）。 */
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

    /** 按朝向与宽度构造墙体 AABB；生长动画期间按 {@code min(lifespan, tickCount)/10} 缩放。 */
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

    /** 朝向 / 宽度数据更新时重算包围盒（客户端渲染与碰撞保持一致）。 */
    public void onSyncedDataUpdated(EntityDataAccessor<?> ac) {
        if (DIRECTION.equals(ac) || BLOCK_WIDTH.equals(ac)) {
            this.setBoundingBox(this.makeBoundingBox());
        }
        super.onSyncedDataUpdated(ac);
    }

    /** 同阵营（同主人或与主人同盟）实体不被墙阻挡。 */
    public boolean isSameTeam(Entity entity) {
        Entity owner = this.getCreator();
        return owner instanceof LivingEntity && (TameableUtils.hasSameOwnerAs((LivingEntity)owner, entity) || owner.isAlliedTo(entity));
    }
}
