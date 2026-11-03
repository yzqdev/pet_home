package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.util.TameableUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 链锁闪电特效实体（无碰撞、免伤、无存档）：从"来源宠物"链向"当前目标"，命中目标造成 3 点闪电伤害， 并在 8 格内寻找下一个非同主人、有视线的敌对 Mob 生成子闪电（{@code
 * chainsLeft} 递减，初始 3+3×等级）， 链条关系与目标 ID 存 SynchedEntityData 供客户端画闪电。寿命到（默认 5 tick）后消失。
 */
public class ChainLightningEntity extends Entity {

  /** 同步数据：闪电链起点（发起攻击的宠物）实体 ID，-1 表示未设置。 */
  private static final EntityDataAccessor<Integer> CREATOR_ID =
      SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.INT);

  /** 同步数据：本段闪电的起点实体 ID（上一环），客户端据此绘制闪电连线。 */
  private static final EntityDataAccessor<Integer> FROM_ID =
      SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.INT);

  /** 同步数据：剩余可链式跳跃次数。 */
  private static final EntityDataAccessor<Integer> TARGET_COUNT =
      SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.INT);

  /** 同步数据：本段闪电当前指向的目标实体 ID，-1 表示未设置。 */
  private static final EntityDataAccessor<Integer> CURRENT_TARGET_ID =
      SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.INT);

  /** 同步数据：剩余存活 tick 数，归零后移除自身。 */
  private static final EntityDataAccessor<Integer> DIES_IN =
      SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.INT);

  /** 本链条已电击过的实体列表（子闪电继承追加），防止同一目标被反复电击。 */
  private List<Entity> previouslyShocked = new ArrayList<>();

  /** 是否已对当前目标造成伤害（每段闪电只电击一次）。 */
  private boolean hasShocked = false;

  /** 是否已链向下一个目标（每段闪电只链一次）。 */
  private boolean hasChained = false;

  public ChainLightningEntity(EntityType<?> type, Level level) {
    super(type, level);
  }

  //
  //    public ChainLightningEntity(PlayMessages.SpawnEntity spawnEntity, Level world) {
  //        this(DIEntityRegistry.CHAIN_LIGHTNING.get(), world);
  //    }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    builder.define(CREATOR_ID, -1);
    builder.define(FROM_ID, -1);
    builder.define(TARGET_COUNT, 1);
    builder.define(CURRENT_TARGET_ID, -1);
    builder.define(DIES_IN, 5);
  }

  /** 每 tick：跟随目标位置 → 首次接触造成闪电伤害 → 尝试链向下一个目标 → 寿命递减。 */
  @Override
  public void tick() {
    super.tick();
    Entity creator = getCreatorEntity();
    Entity current = getToEntity();
    if (creator instanceof LivingEntity) {
      if (current != null) {
        this.setPos(
            new Vec3(
                current.getX(), current.getY() + current.getBbHeight() * 0.5F, current.getZ()));
        if (!level().isClientSide()) {
          if (!hasShocked) {
            hasShocked = true;
            current.hurt(current.damageSources().lightningBolt(), 3);
          }
        }
      }
    }
    if (!level().isClientSide()) {
      if (!hasChained) {
        if (this.getChainsLeft() > 0 && creator instanceof LivingEntity) {
          Entity closestValid = null;
          for (Entity entity :
              this.level()
                  .getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
            if (!entity.equals(creator)
                && !TameableUtils.hasSameOwnerAs((LivingEntity) creator, entity)
                && !previouslyShocked.contains(entity)
                && !creator.isAlliedTo(entity)
                && entity instanceof Mob
                && this.hasLineOfSight(entity)) {
              if (closestValid == null || this.distanceTo(entity) < this.distanceTo(closestValid)) {
                closestValid = entity;
              }
            }
          }
          if (closestValid != null) {
            createLightningAt(closestValid);
            hasChained = true;
          }
        }
      }
      if (getDiesInTicks() > 0) {
        this.setDiesInTicks(getDiesInTicks() - 1);
      } else {
        this.remove(RemovalReason.DISCARDED);
      }
    }
  }

  /** 目标与本闪电同维度、距离 128 格内且视线上无方块遮挡时返回 true。 */
  private boolean hasLineOfSight(Entity entity) {
    if (entity.level() != this.level()) {
      return false;
    } else {
      Vec3 vec3 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
      Vec3 vec31 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
      if (vec31.distanceTo(vec3) > 128.0D) {
        return false;
      } else {
        return this.level()
                .clip(
                    new ClipContext(
                        vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this))
                .getType()
            == HitResult.Type.MISS;
      }
    }
  }

  /** 在当前命中目标处生成下一段子闪电：继承已电击列表并加入该目标，剩余链数减一。 */
  private void createLightningAt(Entity closestValid) {
    ChainLightningEntity child = PHEntityRegistry.CHAIN_LIGHTNING.create(this.level());
    child.previouslyShocked = new ArrayList<>(previouslyShocked);
    child.previouslyShocked.add(closestValid);
    child.setCreatorEntityID(this.getCreatorEntityID());
    child.setFromEntityID(this.getId());
    child.setToEntityID(closestValid.getId());
    child.copyPosition(closestValid);
    child.setChainsLeft(this.getChainsLeft() - 1);
    this.level().addFreshEntity(child);
  }

  public int getCreatorEntityID() {
    return this.entityData.get(CREATOR_ID);
  }

  public void setCreatorEntityID(int id) {
    this.entityData.set(CREATOR_ID, id);
  }

  public Entity getCreatorEntity() {
    return getCreatorEntityID() == -1 ? null : this.level().getEntity(getCreatorEntityID());
  }

  public int getFromEntityID() {
    return this.entityData.get(FROM_ID);
  }

  public void setFromEntityID(int id) {
    this.entityData.set(FROM_ID, id);
  }

  public Entity getFromEntity() {
    return getFromEntityID() == -1 ? null : this.level().getEntity(getFromEntityID());
  }

  public int getToEntityID() {
    return this.entityData.get(CURRENT_TARGET_ID);
  }

  public void setToEntityID(int id) {
    this.entityData.set(CURRENT_TARGET_ID, id);
  }

  public Entity getToEntity() {
    return getToEntityID() == -1 ? null : this.level().getEntity(getToEntityID());
  }

  public int getChainsLeft() {
    return this.entityData.get(TARGET_COUNT);
  }

  public void setChainsLeft(int i) {
    this.entityData.set(TARGET_COUNT, i);
  }

  public int getDiesInTicks() {
    return this.entityData.get(DIES_IN);
  }

  public void setDiesInTicks(int i) {
    this.entityData.set(DIES_IN, i);
  }

  // 特效实体不持久化：退出世界即消失，无需存档
  @Override
  protected void readAdditionalSaveData(CompoundTag compound) {}

  @Override
  protected void addAdditionalSaveData(CompoundTag compound) {}
}
