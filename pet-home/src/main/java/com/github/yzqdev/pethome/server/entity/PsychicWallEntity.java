package com.github.yzqdev.pethome.server.entity;


import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.util.PHAttachments;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
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
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import java.util.*;

/**
 * 念力墙实体（PSYCHIC_WALL 附魔生成）：一面 {@code blockWidth} 见方、朝向
 * {@code wallDirection} 的可推挡墙，寿命 {@code lifespan}（等级×100 tick，创造者死亡后
 * 压缩到 20 tick）。每 tick 把碰到的非友方实体沿法线推开；投射物被反转 180° 弹回
 * （友方发射的 {@code isFiredByAlly} 不弹），同一实体 15 tick 内只触发一次特效。
 * 包围盒由方向/宽度动态计算（出现/消失 10 tick 内按比例缩放）；免伤、无重力。
 * 自定义序列化器：CREATOR_UUID 用 {@code OPTIONAL_UUID}，序列化见 read/addAdditionalSaveData。
 */
public class PsychicWallEntity extends Entity {

    protected static final EntityDataAccessor<Direction> DIRECTION = SynchedEntityData.defineId(PsychicWallEntity.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Optional<UUID>> CREATOR_UUID = SynchedEntityData.defineId(PsychicWallEntity.class, PHAttachments.OPTIONAL_UUID.get());
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
     * 核心 tick：出现/消失缩放包围盒 → 环境音循环 → 创造者状态同步（死亡则缩短寿命）→
     * 推开/弹回碰撞实体（每实体 15 tick 冷却）→ 寿命倒数。
     */
    public void tick() {
        super.tick();
        // 出现/消失各 10 tick 内包围盒按比例缩放，做出"墙长出/消散"的视觉过渡
        if (this.tickCount <= 10 || this.getLifespan() <= 10) {
            this.setBoundingBox(this.makeBoundingBox(this.position()));
        } else {
            // 每 15 tick 播放一次环境嗡鸣（随机音调 0.9~1.2）
            if (soundLoop % 15 == 0) {
                this.playSound(PHSoundRegistry.PSYCHIC_WALL, 1, random.nextFloat() * 0.3F + 0.9F);
            }
            soundLoop++;
        }
        // 服务端：把创造者实体 id 同步给客户端；创造者死亡后寿命压到 20 tick（墙随主人消散）
        if (!this.level().isClientSide() && this.getCreatorId() != null) {
            Entity creator = this.getCreator();
            if (creator != null) {
                this.entityData.set(CREATOR_ID, creator.getId());
                if (!creator.isAlive() && this.getLifespan() > 20) {
                    this.setLifespan(20);
                }
            }
        }
        // 墙本体固定不动，只结算与它碰撞的实体
        this.setDeltaMovement(Vec3.ZERO);
        float wX = Math.abs(this.getWallDirection().getStepX());
        float wY = Math.abs(this.getWallDirection().getStepY());
        float wZ = Math.abs(this.getWallDirection().getStepZ());
        AABB collisionAABB = this.getBoundingBox().inflate(wX * 0.25F, wY * 0.25F, wZ * 0.25F);
        List<Entity> colliders = level().getEntities(this, collisionAABB);
        float backISay = this.getWallDirection().getAxis() == Direction.Axis.Y ? -0.6F : -0.1F;
        for (Entity collider : colliders) {
            // 友方（同主人/同盟）直接穿墙，其余实体沿墙法线推开
            if (!isSameTeam(collider)) {
                collider.push(getWallDirection().getStepX() * 0.25F, getWallDirection().getStepY() * 0.25F, getWallDirection().getStepZ() * 0.25F);
                // 每实体只在首次接触时结算特效/弹回（冷却 15 tick 内不重复触发）
                if (!deflectedEntities.containsKey(collider.getUUID())) {
                    boolean flag = true;
                    if (collider instanceof Projectile) {
                        if (isFiredByAlly((Projectile) collider)) {
                            flag = false;
                        } else {
                            // 敌方投射物速度反转并翻转朝向，实现"弹回发射者"
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
        // 弹回冷却逐 tick 衰减，过期后允许再次触发
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

    /** 投射物是否由墙的友方（同主人/同盟）发射——友方的投射物穿过，敌方的弹回。 */
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

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float f) {
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
    protected void readAdditionalSaveData(ValueInput tag) {
        tag.getInt("Lifespan").ifPresent(this::setLifespan);
        tag.read("CreatorUUID", UUIDUtil.CODEC).ifPresent(this::setCreatorId);
        this.setBlockWidth(tag.getIntOr("BlockWidth", 0));
        this.setWallDirection(Direction.from3DDataValue(tag.getIntOr("WallDirection", 0)));

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        if (this.getCreatorId() != null) {
            tag.store("CreatorUUID", UUIDUtil.CODEC, this.getCreatorId());
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


    /** 创造者解析：服务端按 UUID 从 {@code ServerLevel} 查；客户端用同步来的网络 id 查。 */
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

    /**
     * 按朝向与方块宽度动态计算包围盒：与墙面平行的两个轴各扩 half 宽度
     * （half = blockWidth/2 × 出现/消失缩放系数），法线轴保持 0.3 厚度。
     */
    protected AABB makeBoundingBox(Vec3 position) {
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
        return new AABB(position.x + minX, position.y + minY, position.z + minZ, position.x + maxX, position.y + maxY, position.z + maxZ);
    }

    public void setPos(double x, double y, double z) {
        this.setPosRaw(x, y, z);
        this.setBoundingBox(this.makeBoundingBox(this.position()));
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> ac) {
        if (DIRECTION.equals(ac) || BLOCK_WIDTH.equals(ac)) {
            this.setBoundingBox(this.makeBoundingBox(this.position()));
        }
        super.onSyncedDataUpdated(ac);
    }


    /** 实体与墙的创造者是否友方（同主人或原版同盟）；友方单位可穿过念力墙。 */
    public boolean isSameTeam(Entity entity) {
        Entity owner = this.getCreator();
        return owner instanceof LivingEntity && (TameableUtils.hasSameOwnerAs((LivingEntity) owner, entity) || owner.isAlliedTo(entity));
    }
}
