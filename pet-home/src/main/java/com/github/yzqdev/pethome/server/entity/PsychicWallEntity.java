package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.util.TameableUtils;
import java.util.*;
import javax.annotation.Nullable;
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

/**
 * 念力墙：可阻挡的墙形特效实体（宽 {@code blockWidth} 格、朝向 {@code DIRECTION}、寿命 {@code LIFESPAN}）。tick 中把 0.25
 * 格内的非同队实体沿墙法线推开；敌方投掷物反向弹回 （同主人发射的投掷物不反弹），同一个实体 15 tick 内只反弹一次（防连发粒子/音效）； 创建者死亡时寿命压到 20
 * tick。朝向/宽度/寿命走 SynchedEntityData 供客户端渲染。
 */
public class PsychicWallEntity extends Entity {

  /** 同步数据：墙面的朝向法线（推离与反弹方向）。 */
  protected static final EntityDataAccessor<Direction> DIRECTION =
      SynchedEntityData.defineId(PsychicWallEntity.class, EntityDataSerializers.DIRECTION);

  /** 同步数据：创建者 UUID（服务端定位创建者用）。 */
  private static final EntityDataAccessor<Optional<UUID>> CREATOR_UUID =
      SynchedEntityData.defineId(PsychicWallEntity.class, EntityDataSerializers.OPTIONAL_UUID);

  /** 同步数据：创建者实体 ID（客户端定位创建者用）。 */
  private static final EntityDataAccessor<Integer> CREATOR_ID =
      SynchedEntityData.defineId(PsychicWallEntity.class, EntityDataSerializers.INT);

  /** 同步数据：剩余寿命 tick 数（默认 300）。 */
  private static final EntityDataAccessor<Integer> LIFESPAN =
      SynchedEntityData.defineId(PsychicWallEntity.class, EntityDataSerializers.INT);

  /** 同步数据：墙的宽度（格），客户端据此渲染与计算碰撞盒。 */
  private static final EntityDataAccessor<Integer> BLOCK_WIDTH =
      SynchedEntityData.defineId(PsychicWallEntity.class, EntityDataSerializers.INT);

  /** 被反弹实体的 UUID 记录（当前逻辑未实际使用，反弹冷却见 {@link #deflectedEntities}）。 */
  private final List<UUID> deflectedArrows = new ArrayList<>();

  /** 被反弹实体 UUID → 剩余冷却 tick：同一实体 15 tick 内只反弹一次，防止连发粒子与音效。 */
  private final Map<UUID, Integer> deflectedEntities = new HashMap<>();

  /** 环境音循环计数器（每 15 tick 播放一次念力墙音效）。 */
  private int soundLoop = 0;

  public PsychicWallEntity(EntityType<?> type, Level level) {
    super(type, level);
  }

  public void tick() {
    super.tick();
    // 前 10 tick 为墙体展开动画期（或寿命将至），每 tick 重建碰撞盒以体现缩放
    if (this.tickCount <= 10 || this.getLifespan() <= 10) {
      this.setBoundingBox(this.makeBoundingBox());
    } else {
      // 稳定期：每 15 tick 播放一次念力墙环境音
      if (soundLoop % 15 == 0) {
        this.playSound(PHSoundRegistry.PSYCHIC_WALL, 1, random.nextFloat() * 0.3F + 0.9F);
      }
      soundLoop++;
    }
    if (!this.level().isClientSide && this.getCreatorId() != null) {
      // 服务端：同步创建者实体 ID；创建者死亡后把剩余寿命压到 20 tick 加速消散
      Entity creator = this.getCreator();
      if (creator != null) {
        this.entityData.set(CREATOR_ID, creator.getId());
        if (!creator.isAlive() && this.getLifespan() > 20) {
          this.setLifespan(20);
        }
      }
    }
    // 墙体固定不动
    this.setDeltaMovement(Vec3.ZERO);
    // 沿墙法线外扩 0.25 格作为检测盒：把范围内的非友方实体推离
    float wX = Math.abs(this.getWallDirection().getStepX());
    float wY = Math.abs(this.getWallDirection().getStepY());
    float wZ = Math.abs(this.getWallDirection().getStepZ());
    AABB collisionAABB = this.getBoundingBox().inflate(wX * 0.25F, wY * 0.25F, wZ * 0.25F);
    List<Entity> colliders = level().getEntities(this, collisionAABB);
    // 敌方投掷物的反弹力度：法线沿 Y 轴（水平墙面）时 -0.6（反向六成速度），其余 -0.1
    float backISay = this.getWallDirection().getAxis() == Direction.Axis.Y ? -0.6F : -0.1F;
    for (Entity collider : colliders) {
      if (!isSameTeam(collider)) {
        collider.push(
            getWallDirection().getStepX() * 0.25F,
            getWallDirection().getStepY() * 0.25F,
            getWallDirection().getStepZ() * 0.25F);
        if (!deflectedEntities.containsKey(collider.getUUID())) {
          boolean flag = true;
          // 友方发射的投掷物穿墙不反弹；敌方投掷物速度反向并翻转朝向
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
            // 记录反弹冷却；客户端在接触点生成粒子并播放反弹音效
            deflectedEntities.put(collider.getUUID(), 15);
            if (level().isClientSide && collider.getBoundingBox().intersects(collisionAABB)) {
              Vec3 vec3 = new Vec3(collider.getX(), collider.getY(0.5F), collider.getZ());
              Vec3 vec31 = collisionAABB.getCenter();
              Vec3 vec32;
              EntityHitResult entityhitresult =
                  ProjectileUtil.getEntityHitResult(
                      collider,
                      vec3,
                      vec31,
                      collisionAABB,
                      (entity) -> {
                        return entity == this;
                      },
                      collider.distanceTo(this));
              if (entityhitresult == null || entityhitresult.getType() == HitResult.Type.MISS) {
                vec32 = vec3;
              } else {
                vec32 = entityhitresult.getLocation();
              }
              level()
                  .addParticle(
                      PHParticleRegistry.PSYCHIC_WALL,
                      vec32.x,
                      vec32.y,
                      vec32.z,
                      this.getWallDirection().get3DDataValue(),
                      0,
                      0);
              this.playSound(
                  PHSoundRegistry.PSYCHIC_WALL_DEFLECT, 1, random.nextFloat() * 0.3F + 0.9F);
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

  /** 投掷物是否由墙的创建者同主人单位发射（友方火力穿过，不反弹）。 */
  private boolean isFiredByAlly(Projectile projectile) {
    Entity owner = this.getCreator();
    if (owner instanceof LivingEntity && projectile.getOwner() != null) {
      return TameableUtils.hasSameOwnerAs((LivingEntity) owner, projectile.getOwner());
    } else {
      return false;
    }
  }

  /** 特效墙不参与实体碰撞推挤。 */
  public boolean canBeCollidedWith() {
    return false;
  }

  /** 免疫一切伤害。 */
  public boolean hurt(DamageSource source, float f) {
    return false;
  }

  /** 无重力，保持悬停。 */
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

  /** 定位创建者：服务端按 UUID 查（重载后稳定），客户端按同步的实体 ID 查。 */
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

  /** 仅非友方实体可与之碰撞（友方直接穿过）。 */
  public boolean canCollideWith(Entity entity) {
    return !this.isSameTeam(entity);
  }

  /** 按墙面朝向生成交叉于实体中心的板状碰撞盒：半宽取 {@code BlockWidth/2}， 再乘以展开进度（取剩余寿命与已存活 tick 中较小者 / 10，封顶 1）。 */
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
    return new AABB(
        this.getX() + minX,
        this.getY() + minY,
        this.getZ() + minZ,
        this.getX() + maxX,
        this.getY() + maxY,
        this.getZ() + maxZ);
  }

  /** 覆写移动逻辑：位置更新后立即按新位置重建板状碰撞盒。 */
  public void setPos(double x, double y, double z) {
    this.setPosRaw(x, y, z);
    this.setBoundingBox(this.makeBoundingBox());
  }

  /** 朝向或宽度同步更新后重建碰撞盒，保持客户端碰撞盒与渲染一致。 */
  public void onSyncedDataUpdated(EntityDataAccessor<?> ac) {
    if (DIRECTION.equals(ac) || BLOCK_WIDTH.equals(ac)) {
      this.setBoundingBox(this.makeBoundingBox());
    }
    super.onSyncedDataUpdated(ac);
  }

  /** 实体是否属于墙的友方：与创建者同主人（宠物与主人共队）或与创建者结盟。 */
  public boolean isSameTeam(Entity entity) {
    Entity owner = this.getCreator();
    return owner instanceof LivingEntity
        && (TameableUtils.hasSameOwnerAs((LivingEntity) owner, entity) || owner.isAlliedTo(entity));
  }
}
