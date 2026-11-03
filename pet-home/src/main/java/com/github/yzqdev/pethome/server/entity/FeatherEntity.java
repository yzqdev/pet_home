package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.util.TameableUtils;
import javax.annotation.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 羽毛钓竿的抛射物：落地后不消失，而是像钓竿一样把主人最近的宠物拉到落点 （近处直接吸引移动，远处导航走过来；停留/坐下的宠物不理会）。 配合 {@code FishingHookMixin}
 * 复用钓鱼钩的收回与浮漂逻辑。
 */
public class FeatherEntity extends FishingHook {

  /** 同步数据：投掷者（主人）实体 ID，客户端据此定位主人；-1 表示未设置。 */
  private static final EntityDataAccessor<Integer> OWNER_ID =
      SynchedEntityData.defineId(FeatherEntity.class, EntityDataSerializers.INT);

  public FeatherEntity(EntityType featherType, Level level) {
    super(featherType, level);
  }

  /** 上一 tick 是否落地（用于在落地瞬间只锁定一次目标宠物）。 */
  private boolean prevOnGround = false;

  /** 落地后锁定的最近宠物，逐 tick 吸引至羽毛落点。 */
  public LivingEntity closestPet = null;

  /** 由玩家视角计算初速并抛出（复刻原版钓鱼钩的三角函数弹道）。 */
  public FeatherEntity(Player player, Level level) {
    this(PHEntityRegistry.FEATHER, level);
    this.setOwner(player);
    float f = player.getXRot();
    float f1 = player.getYRot();
    float f2 = Mth.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
    float f3 = Mth.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
    float f4 = -Mth.cos(-f * ((float) Math.PI / 180F));
    float f5 = Mth.sin(-f * ((float) Math.PI / 180F));
    double d0 = player.getX() - (double) f3 * 0.3D;
    double d1 = player.getEyeY();
    double d2 = player.getZ() - (double) f2 * 0.3D;
    this.moveTo(d0, d1, d2, f1, f);
    Vec3 vec3 =
        new Vec3((double) (-f3), (double) Mth.clamp(-(f5 / f4), -5.0F, 5.0F), (double) (-f2));
    double d3 = vec3.length();
    vec3 =
        vec3.multiply(
            0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D,
            0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D,
            0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D);
    this.setDeltaMovement(vec3);
    this.setYRot((float) (Mth.atan2(vec3.x, vec3.z) * (double) (180F / (float) Math.PI)));
    this.setXRot(
        (float) (Mth.atan2(vec3.y, vec3.horizontalDistance()) * (double) (180F / (float) Math.PI)));
    this.yRotO = this.getYRot();
    this.xRotO = this.getXRot();
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    super.defineSynchedData(builder);
    builder.define(OWNER_ID, 0);
  }

  /** 落地后锁定最近宠物并逐 tick 吸引/导航到落点（服务端逻辑；空中时按钓竿轨迹飞行）。 */
  @Override
  public void tick() {
    super.tick();
    this.setDeltaMovement(this.getDeltaMovement().multiply(0.8D, 0.8D, 0.8D));
    if (this.onGround() && !level().isClientSide()) {
      if (!prevOnGround) {
        closestPet = findClosestPetOf(this.getPlayerOwner());
      }
      if (closestPet instanceof Mob mob && isPetAmbulatory(mob)) {
        // 近距（贴身约 1 格内）：朝羽毛方向微调速度直接吸引；远距：寻路走过来
        if (closestPet.distanceTo(this) <= closestPet.getBbWidth() + 1.0F) {
          if (closestPet.distanceTo(this) > 0.1D) {
            mob.lookAt(this, 40, 90);
            Vec3 move =
                new Vec3(
                    this.getX() - mob.getX(),
                    this.getY() - (double) mob.getY(),
                    this.getZ() - mob.getZ());
            mob.setDeltaMovement(mob.getDeltaMovement().add(move.normalize().scale(0.05D)));
          }
          mob.getNavigation().stop();
        } else {
          mob.getNavigation().moveTo(this, 1.0D);
        }
      }
    }
    prevOnGround = onGround();
  }

  /** 在落点 21 格内找该主人最近的可移动宠物：需已驯服、属于该主人且与羽毛互有视线。 */
  private LivingEntity findClosestPetOf(Player owner) {
    LivingEntity closestValid = null;
    for (Entity entity :
        this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(21.0D))) {
      if (TameableUtils.isTamed(entity)
          && TameableUtils.isPetOf(owner, entity)
          && isPetAmbulatory((LivingEntity) entity)
          && ((LivingEntity) entity).hasLineOfSight(this)) {
        if (closestValid == null || this.distanceTo(entity) < this.distanceTo(closestValid)) {
          closestValid = (LivingEntity) entity;
        }
      }
    }
    return closestValid;
  }

  /** 禁用投掷物对实体的直接命中（羽毛只负责落地聚宠）。 */
  protected boolean canHitEntity(Entity entity) {
    return false;
  }

  /** 空实现：命中实体不做任何处理。 */
  protected void onHitEntity(EntityHitResult result) {}

  @Override
  public Entity getOwner() {
    Entity prev = super.getOwner();
    if (this.entityData.get(OWNER_ID) != -1) {
      return level().getEntity(this.entityData.get(OWNER_ID));
    } else {
      return prev;
    }
  }

  @Override
  public void setOwner(@Nullable Entity owner) {
    super.setOwner(owner);
    this.entityData.set(OWNER_ID, owner == null ? -1 : owner.getId());
  }

  @Nullable
  @Override
  public Player getPlayerOwner() {
    if (level().isClientSide && this.entityData.get(OWNER_ID) != -1) {
      Entity entity = level().getEntity(this.entityData.get(OWNER_ID));
      return entity instanceof Player ? (Player) entity : null;
    } else {
      Entity entity = this.getOwner();
      return entity instanceof Player ? (Player) entity : null;
    }
  }

  /** 该宠物是否可被羽毛吸引（原版宠物未坐下、三态指令开启时非停留态）。 */
  public boolean isPetAmbulatory(LivingEntity entity) {
    if (entity instanceof TamableAnimal
        && (((TamableAnimal) entity).isOrderedToSit()
            || ((TamableAnimal) entity).isInSittingPose())) {
      return false;
    }
    if (entity instanceof IComandableMob
        && ((IComandableMob) entity).getCommand() == 1
        && PetHomeConfig.trinaryCommandSystem) {
      return false;
    }
    return true;
  }
}
