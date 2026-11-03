package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.client.ClientGameEvents;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.entity.FollowingJukeboxEntity;
import com.github.yzqdev.pethome.server.entity.GiantBubbleEntity;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.PsychicWallEntity;
import com.github.yzqdev.pethome.server.misc.PHEffects;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.util.LivingUtils;
import com.github.yzqdev.pethome.util.TameableUtils;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 每 tick 驱动的宠物附魔逻辑（NeoForge 的实体 tick 事件在 Fabric 无对应回调， 改为 {@code END_LEVEL_TICK} 遍历 {@code
 * level.getAllEntities()}）。对每个可指挥 Mob 依次执行四组附魔逻辑 + {@link #mobTick}（DRUNK 效果让怪物乱换目标）+ 20 tick
 * 低频罗盘档案更新。 全部逻辑仅服务端执行（入口 {@code init()} 先判 {@code level.isClientSide()}）。
 */
public class EntityTickHandler {

  /**
   * Fabric 侧接线：NeoForge 的 {@code EntityTickEvent.Pre} 没有对应回调， 改为在每级维度 tick 结束时遍历实体（服务端逻辑，客户端一律跳过）。
   */
  public static void init() {
    ServerTickEvents.END_LEVEL_TICK.register(
        level -> {
          if (level.isClientSide()) {
            return;
          }
          com.github.yzqdev.pethome.server.misc.CollarTickManager.tick(level);
          for (Entity entity : level.getAllEntities()) {
            onLivingUpdate(entity);
          }
        });
  }

  /** 单实体 tick 入口（{@code END_LEVEL_TICK} 逐实体调用；项圈换装冷却期跳过）： 罗盘低频档案更新 → {@link #mobTick} → 四组附魔逻辑。 */
  public static void onLivingUpdate(Entity entity) {
    if (entity instanceof Mob pet
        && com.github.yzqdev.pethome.server.misc.CollarTickManager.canTickCollar(entity)) {
      // 宠物罗盘：20 tick 低频档案更新（首次驯服懒初始化 PetId + 位置/名字/维度刷新，无变化不写盘）
      if (pet.tickCount % 20 == 0) {
        com.github.yzqdev.pethome.server.misc.PetCompassTracker.updateRecord(pet);
      }
      mobTick(pet);

      // 1. 基础状态与生存类附魔
      handleSurvivalEnchants(pet);

      // 2. 实用与辅助类附魔 (夜视、传送、矿石等)
      handleUtilityEnchants(pet);

      // 3. 战斗与攻击类附魔 (冲击波、暗影手等)
      handleCombatEnchants(pet);

      // 4. 控制与防御类附魔 (引力、念力墙等)
      handleControlEnchants(pet);
    }
  }

  /**
   * DRUNK 药水效果的行为侧：10 格内随机换目标（喝醉的怪乱打人），周期冒问号粒子。 仅服务端执行（需 ServerLevel 发粒子；客户端 level 会转型失败）。 目标选择经
   * {@code LivingUtils.setAttackTarget}（兼容猪灵脑驱动与中立怪）。
   */
  public static void mobTick(LivingEntity attacker) {
    // 仅服务端执行：涉及 ServerLevel（粒子、目标选择）等逻辑，客户端 level 为 ClientLevel 会 ClassCastException
    if (attacker.level().isClientSide()) {
      return;
    }
    List<Monster> genericMobs =
        attacker
            .level()
            .getEntitiesOfClass(
                Monster.class, LivingUtils.getBoundingBoxAroundEntity(attacker, (double) 10.0F));
    Random random = new Random();
    if (attacker.hasEffect(PHEffects.DRUNK) && attacker instanceof Monster) {

      double x = attacker.getX();
      double y = attacker.getY() + attacker.getBbHeight() + 0.4;
      double z = attacker.getZ();
      if (attacker.level().getGameTime() % 10 == 0) {
        ((ServerLevel) (attacker.level()))
            .sendParticles(
                PHParticleRegistry.QUESTION_MARK_PARTICLE_TYPE, x, y, z, 1, 0, 0, 0, 0.0D);
      }

      if (genericMobs == null) {
        return;
      }

      Monster Monster = (Monster) attacker;
      if (genericMobs.size() <= 0) {
        return;
      }

      Monster others = (Monster) genericMobs.get(random.nextInt(genericMobs.size()));
      if (genericMobs.size() > 2) {
        while (others == Monster) {
          others = (Monster) genericMobs.get(random.nextInt(genericMobs.size()));
        }
      }

      if (others == null) {
        Monster.setTarget((LivingEntity) null);
      } else {
        LivingUtils.setAttackTarget(Monster, others);
      }
    }
  }

  /** 生存组附魔：烈焰保护蓄条（200 tick/条，上限 2×等级）、无敌帧倒计时、毒素免疫、两栖回气、治愈光环（周期给 4 格内同主人受伤单位再生，冷却 600+随机 tick）。 */
  private static void handleSurvivalEnchants(Mob pet) {
    // Blazing Protection (烈焰保护)
    if (TameableUtils.hasEnchant(pet, PHEnchantments.BLAZING_PROTECTION)
        && !pet.level().isClientSide()) {
      int bars = TameableUtils.getBlazingProtectionBars(pet);
      if (bars < 2 * TameableUtils.getEnchantLevel(pet, PHEnchantments.BLAZING_PROTECTION)) {
        int cooldown = TameableUtils.getBlazingProtectionCooldown(pet);
        if (cooldown > 0) {
          cooldown--;
        } else {
          TameableUtils.setBlazingProtectionBars(pet, bars + 1);
          cooldown = 200;
        }
        TameableUtils.setBlazingProtectionCooldown(pet, cooldown);
      }
    }

    // Immunity Frame (无敌帧)
    if (TameableUtils.hasEnchant(pet, PHEnchantments.IMMUNITY_FRAME)
        && !pet.level().isClientSide()) {
      int i = TameableUtils.getImmuneTime(pet);
      if (i > 0) {
        TameableUtils.setImmuneTime(pet, i - 1);
      }
    }

    // Poison Resistance (毒素免疫)
    if (pet.hasEffect(MobEffects.POISON)
        && TameableUtils.hasEnchant(pet, PHEnchantments.POISON_RESISTANCE)) {
      pet.removeEffect(MobEffects.POISON);
    }

    // Amphibious (两栖)
    if (TameableUtils.hasEnchant(pet, PHEnchantments.AMPHIBIOUS)) {
      pet.setAirSupply(pet.getMaxAirSupply());
    }

    // Healing Aura (治愈光环)
    if (TameableUtils.hasEnchant(pet, PHEnchantments.HEALING_AURA)
        && !pet.level().isClientSide()) {
      int time = TameableUtils.getHealingAuraTime(pet);
      if (time > 0) {
        List<LivingEntity> hurtNearby = TameableUtils.getAuraHealables(pet);
        for (LivingEntity needsHealing : hurtNearby) {
          if (!needsHealing.hasEffect(MobEffects.REGENERATION)) {
            needsHealing.addEffect(
                new MobEffectInstance(
                    MobEffects.REGENERATION,
                    100,
                    TameableUtils.getEnchantLevel(pet, PHEnchantments.HEALING_AURA) - 1));
          }
        }
        time--;
        if (time == 0) time = -600 - pet.getRandom().nextInt(600);
      } else if (time < 0) {
        time++;
      } else if ((pet.tickCount + pet.getId()) % 200 == 0
          || TameableUtils.getHealingAuraImpulse(pet)) {
        List<LivingEntity> hurtNearby = TameableUtils.getAuraHealables(pet);
        if (!hurtNearby.isEmpty()) time = 200;
        TameableUtils.setHealingAuraImpulse(pet, false);
      }
      TameableUtils.setHealingAuraTime(pet, time);
    }
  }

  /**
   * 实用组附魔：夜视（10 格内主人）、虚空云（下落缓降跟随主人高度并记录 fallDistance）、矿石嗅探（周期调 {@code detectRandomOres}
   * 生成高亮实体）、连锁背包（开启拾取）、洞察（黑暗中给敌人发光）。
   */
  private static void handleUtilityEnchants(Mob pet) {
    // Night Vision (夜视)
    if (TameableUtils.hasEnchant(pet, PHEnchantments.NIGHT_VISION) && TameableUtils.isTamed(pet)) {
      var owner = TameableUtils.getOwnerOf(pet);
      if (owner != null && owner.distanceToSqr(pet) < 10 && owner instanceof Player petOwner) {
        if (!petOwner.hasEffect(MobEffects.NIGHT_VISION) && pet.tickCount % 40 == 0) {
          petOwner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60 * 5));
        }
      }
    }

    // XP Transfer (经验转移)
    //        if (TameableUtils.hasEnchant(pet, PHEnchantments.XP_Transfer)) {
    //            TameableUtils.xpTransfer(pet);
    //        }

    // Void Cloud (虚空云/缓降)
    if (TameableUtils.hasEnchant(pet, PHEnchantments.VOID_CLOUD)) {
      if (!pet.isInWater() && pet.fallDistance > 3.0F && !pet.onGround()) {
        Entity owner = TameableUtils.getOwnerOf(pet);
        boolean shouldMoveToOwnerXZ = owner != null && Math.abs(owner.getY() - pet.getY()) < 1;
        double targetX = shouldMoveToOwnerXZ ? owner.getX() : pet.getX();
        double targetY =
            Math.max(
                pet.level().getMinY() + 0.5F,
                owner == null
                    ? 64F
                    : owner.getY() < pet.getY()
                        ? owner.getY() + 0.6F
                        : owner.getY(1.0F) + pet.getBbHeight());
        if (owner != null && owner.getRootVehicle() == pet) {
          targetY = Math.min(pet.level().getMinY() + 0.5F, pet.getY() - 0.5F);
        }
        double targetZ = shouldMoveToOwnerXZ ? owner.getZ() : pet.getZ();
        if (pet.verticalCollision) {
          pet.setOnGround(true);
          targetX += (pet.getRandom().nextFloat() - 0.5F) * 4;
          targetZ += (pet.getRandom().nextFloat() - 0.5F) * 4;
        }
        Vec3 move = new Vec3(targetX - pet.getX(), targetY - pet.getY(), targetZ - pet.getZ());
        pet.setDeltaMovement(
            pet.getDeltaMovement().add(move.normalize().scale(0.15D)).multiply(0.5F, 0.5F, 0.5F));
        if (pet.level() instanceof ServerLevel) {
          TameableUtils.setFallDistance(pet, (float) pet.fallDistance);
          ((ServerLevel) pet.level())
              .sendParticles(
                  ParticleTypes.REVERSE_PORTAL,
                  pet.getRandomX(1.5F),
                  pet.getY() - pet.getRandom().nextFloat(),
                  pet.getRandomZ(1.5F),
                  0,
                  0,
                  -0.2F,
                  0,
                  1.0D);
        }
      } else if (pet.onGround()
          && pet.level() instanceof ServerLevel
          && TameableUtils.getFallDistance(pet) > 0.0F) {
        // 落地清零同步值（仅服务端）：残留 >3 的旧值会让客户端每次起跳都渲染云朵
        TameableUtils.setFallDistance(pet, 0.0F);
      }
    }

    // Ore Scenting (矿石嗅探)
    int oreLvl = TameableUtils.getEnchantLevel(pet, PHEnchantments.ORE_SCENTING);
    if (oreLvl > 0 && !pet.level().isClientSide() && pet.isAlive()) {
      int interval = 100 + Math.max(150, 550 - oreLvl * 100);
      TameableUtils.detectRandomOres(pet, interval, 5 + oreLvl * 2, oreLvl * 50, oreLvl * 3);
    }

    // Linked Inventory (连锁背包)
    if (TameableUtils.hasEnchant(pet, PHEnchantments.LINKED_INVENTORY)) {
      if (!pet.canPickUpLoot()) pet.setCanPickUpLoot(true);
    }

    // Insight (洞察/发光)
    var insightLevel = TameableUtils.getEnchantLevel(pet, PHEnchantments.INSIGHT);
    if (insightLevel > 0 && pet.level() instanceof ServerLevel serverLevel) {
      if (serverLevel.getMaxLocalRawBrightness(pet.getOnPos().above()) < 9) {
        TameableUtils.applyGlowingEffect(pet, insightLevel);
      }
    }

    // disc_jockey ：每 10 tick 确保跟随唱片实体存在（丢失则补建并写回 UUID）
    if (TameableUtils.hasEnchant(pet, PHEnchantments.DISC_JOCKEY)
        && !pet.level().isClientSide()
        && pet.tickCount % 10 == 0) {
      tickDiscJockey(pet);
    }
  }

  private static void tickDiscJockey(Mob pet) {
    UUID jukeboxUUID = TameableUtils.getPetJukeboxUUID(pet);
    boolean alive =
        jukeboxUUID != null
            && pet.level() instanceof ServerLevel serverLevel
            && serverLevel.getEntity(jukeboxUUID) instanceof FollowingJukeboxEntity;
    if (!alive) {
      FollowingJukeboxEntity follower =
          PHEntityRegistry.FOLLOWING_JUKEBOX
              .create(pet.level(), EntitySpawnReason.MOB_SUMMONED);
      if (follower != null) {
        follower.setFollowingUUID(pet.getUUID());
        follower.copyPosition(pet);
        pet.level().addFreshEntity(follower);
        TameableUtils.setPetJukeboxUUID(pet, follower.getUUID());
      }
    }
  }

  /** 战斗组附魔：音波轰击（有目标且 200 tick 周期，多敌对时群体释放）、暗影之手（见 {@link #handleShadowHandsLogic}）、经验吸收（吃经验球回血）。 */
  private static void handleCombatEnchants(Mob pet) {
    // Sonic Boom (音波轰击)
    if (TameableUtils.hasEnchant(pet, PHEnchantments.SonicBoom)) {
      var beingAttacked = pet.getTarget();
      if (beingAttacked != null) {
        if (pet.closerThan(beingAttacked, 10.0, 20.0)
            || TameableUtils.getNearbyMobs(pet, 10).size() > 3) {
          if (pet.tickCount % 200 == 0) {
            TameableUtils.performSonicBook(pet, beingAttacked, (ServerLevel) pet.level());
          }
        }
      }
    }

    // Shadow Hands (暗影之手)
    int shadowHandsLevel = TameableUtils.getEnchantLevel(pet, PHEnchantments.SHADOW_HANDS);
    if (shadowHandsLevel > 0) {
      handleShadowHandsLogic(pet, shadowHandsLevel);
    }

    // Rejuvenation (经验吸收)
    if (TameableUtils.hasEnchant(pet, PHEnchantments.REJUVENATION)) {
      TameableUtils.absorbExpOrbs(pet);
    }
  }

  /**
   * 暗影之手核心：按等级维护 N 拳的进度/出手数组（{@code PetShadowPunchTimes/Striking}， 每 5 tick 出一拳、进度满 10 时结算 {@code
   * clamp(2..4)} 点伤害）。目标取 {@code PetAttackTarget} （玩家/驯服动物被过滤不允许挨打）；目标失效时逐拳回缩并从主人受击记录里找新目标。 客户端先经
   * {@code ClientGameEvents.updateVisualDataForMob} 同步视觉数组再走服务端分支退出。
   */
  private static void handleShadowHandsLogic(Mob mob, int level) {
    ClientGameEvents.updateVisualDataForMob(mob, TameableUtils.getShadowPunchTimes(mob));
    if (mob.level().isClientSide()) return;

    var targetEntity = TameableUtils.getPetAttackTarget(mob);
    Entity punching =
        ((targetEntity instanceof Player) || (targetEntity instanceof TamableAnimal))
            ? null
            : targetEntity;
    int[] punchProgress = TameableUtils.getShadowPunchTimes(mob);

    if (punching != null
        && punching.isAlive()
        && mob.hasLineOfSight(punching)
        && mob.distanceTo(punching) < 16) {
      int[] striking = TameableUtils.getShadowPunchStriking(mob);
      if (punchProgress == null || punchProgress.length < level) {
        int[] clean = new int[level];
        TameableUtils.setShadowPunchTimes(mob, clean);
        TameableUtils.setShadowPunchStriking(mob, clean);
      } else {
        int cooldown = TameableUtils.getShadowPunchCooldown(mob);
        if (cooldown <= 0) {
          boolean flag = false;
          int start = level == 1 ? 0 : mob.getRandom().nextInt(level - 1);
          for (int i = start; i < level; i++) {
            if (striking[i] == 0) {
              striking[i] = 1;
              flag = true;
              break;
            }
          }
          if (flag) TameableUtils.setShadowPunchCooldown(mob, 5);
        } else {
          TameableUtils.setShadowPunchCooldown(mob, cooldown - 1);
        }
        for (int i = 0; i < Math.min(level, Math.min(striking.length, punchProgress.length)); i++) {
          if (striking[i] != 0) {
            if (punchProgress[i] < 10) {
              punchProgress[i]++;
            } else {
              if (punching.level() instanceof ServerLevel punchLevel) {
                punching.hurtServer(
                    punchLevel, punching.damageSources().mobAttack(mob), Mth.clamp(level, 2, 4));
              }
              striking[i] = 0;
            }
          }
          if (striking[i] == 0 && punchProgress[i] > 0) punchProgress[i]--;
        }
        TameableUtils.setShadowPunchStriking(mob, striking);
        TameableUtils.setShadowPunchTimes(mob, punchProgress);
      }
    } else {
      // 目标失效处理逻辑（含被过滤的目标——玩家/驯服动物不允许作为暗影之手目标，也要回缩并清除）。
      // 收拳递减必须无条件执行：目标实体被移除后 getPetAttackTarget 返回 null，若把递减放在
      // targetEntity!=null 里，打死敌人（实体消失）后 punch 卡在当前值，对应手永久保持握拳姿态
      boolean flag = true;
      for (int i = 0; i < Math.min(level, punchProgress.length); i++) {
        if (punchProgress[i] > 0) {
          punchProgress[i]--;
          flag = false;
        }
      }
      TameableUtils.setShadowPunchStriking(mob, new int[level]);
      TameableUtils.setShadowPunchTimes(mob, punchProgress);
      if (flag) TameableUtils.setPetAttackTarget(mob, -1);
      // 寻找新目标
      Entity punchingTarget = mob.getTarget();
      if (punchingTarget == null && TameableUtils.getOwnerOf(mob) instanceof LivingEntity owner) {
        if (owner.getLastHurtByMob() != null
            && owner.getLastHurtByMob().isAlive()
            && !TameableUtils.hasSameOwnerAs(mob, owner.getLastHurtByMob())) {
          punchingTarget = owner.getLastHurtByMob();
        } else if (owner.getLastHurtMob() != null
            && owner.getLastHurtMob().isAlive()
            && !TameableUtils.hasSameOwnerAs(mob, owner.getLastHurtMob())) {
          punchingTarget = owner.getLastHurtMob();
        }
      }
      if (punchingTarget != null && punchingTarget.isAlive()) {
        TameableUtils.setPetAttackTarget(mob, punchingTarget.getId());
      }
    }
  }

  /**
   * 控制组附魔：枯萎诅咒（毁植物）、臭名昭著（引怪）、威吓（吓怪）、牧羊人（吸引 3×等级 只动物）、念力墙（冷却 {@code 200×等级+40}
   * tick，在宠物/主人与威胁之间生成墙实体）、磁力（见 {@link #handleMagneticLogic}）。
   */
  private static void handleControlEnchants(Mob pet) {
    // Blight Curse (枯萎诅咒)
    if (TameableUtils.hasEnchant(pet, PHEnchantments.BLIGHT_CURSE)) {
      TameableUtils.destroyRandomPlants(pet);
    }
    // Infamy Curse (臭名昭著)
    if (TameableUtils.hasEnchant(pet, PHEnchantments.INFAMY_CURSE)) {
      TameableUtils.aggroRandomMonsters(pet);
    }
    // Intimidation (威吓)
    if (TameableUtils.hasEnchant(pet, PHEnchantments.INTIMIDATION)) {
      TameableUtils.scareRandomMonsters(
          pet, TameableUtils.getEnchantLevel(pet, PHEnchantments.INTIMIDATION));
    }
    // Shepherd (牧羊人)
    int shepherdLvl = TameableUtils.getEnchantLevel(pet, PHEnchantments.SHEPHERD);
    if (shepherdLvl > 0) {
      TameableUtils.attractAnimals(pet, shepherdLvl * 3);
    }

    // Psychic Wall (念力墙)
    int psychicWallLevel = TameableUtils.getEnchantLevel(pet, PHEnchantments.PSYCHIC_WALL);
    if (psychicWallLevel > 0 && !pet.level().isClientSide()) {
      int cooldown = TameableUtils.getPsychicWallCooldown(pet);
      if (cooldown > 0) {
        TameableUtils.setPsychicWallCooldown(pet, cooldown - 1);
      } else {
        Entity blocking = null;
        Entity blockingFrom = null;
        if (pet.getTarget() != null) {
          blocking = pet.getTarget();
          blockingFrom = pet;
        } else if (TameableUtils.getOwnerOf(pet) instanceof LivingEntity owner) {
          if (owner.getLastHurtByMob() != null
              && owner.getLastHurtByMob().isAlive()
              && !TameableUtils.hasSameOwnerAs(pet, owner.getLastHurtByMob())) {
            blocking = owner.getLastHurtByMob();
            blockingFrom = owner;
          } else if (owner.getLastHurtMob() != null
              && owner.getLastHurtMob().isAlive()
              && !TameableUtils.hasSameOwnerAs(pet, owner.getLastHurtMob())) {
            blocking = owner.getLastHurtMob();
            blockingFrom = owner;
          }
        }
        if (blocking != null) {
          int width = psychicWallLevel + 1;
          float yAdd = blocking.getBbHeight() * 0.5F + width * 0.5F;
          Vec3 avg =
              blockingFrom
                  .position()
                  .add(0, yAdd, 0)
                  .add(blocking.position().add(0, yAdd, 0))
                  .scale(0.5D);
          avg = new Vec3(avg.x, Math.floor(avg.y), avg.z);
          Vec3 rotationFrom = avg.subtract(blockingFrom.position().add(0, yAdd, 0));
          // 26.1: Direction#getNearest(double,double,double) 更名为 getApproximateNearest
          Direction dir =
              Direction.getApproximateNearest(rotationFrom.x, rotationFrom.y, rotationFrom.z);
          PsychicWallEntity wall =
              PHEntityRegistry.PSYCHIC_WALL.create(pet.level(), EntitySpawnReason.EVENT);
          wall.setPos(avg.x, avg.y, avg.z);
          wall.setBlockWidth(width);
          wall.setCreatorId(pet.getUUID());
          wall.setLifespan(psychicWallLevel * 100);
          wall.setWallDirection(dir);

          if (pet.level() instanceof ServerLevel petLevel) {
            petLevel.addFreshEntity(wall);
          }
          TameableUtils.setPsychicWallCooldown(pet, psychicWallLevel * 200 + 40);
        }
      }
    }

    // Magnetic (引力)
    if (TameableUtils.hasEnchant(pet, PHEnchantments.MAGNETIC)) {
      handleMagneticLogic(pet);
    }
  }

  /**
   * 磁力（Magnetic）：把 {@code PetAttackTarget} 拉向自己（服务端写目标 id、给目标施加拉力， 客户端沿连线画 MAGNET 粒子并每 15 tick
   * 播循环音效）。目标过近/死亡/骑在泡泡上时清除。
   */
  private static void handleMagneticLogic(Mob mob) {
    Entity sucking = TameableUtils.getPetAttackTarget(mob);
    if (!mob.level().isClientSide()) {
      if (mob.getTarget() == null
          || !mob.getTarget().isAlive()
          || mob.distanceTo(mob.getTarget()) < 0.5F + mob.getBbWidth()
          || mob.getRootVehicle() instanceof GiantBubbleEntity) {
        if (TameableUtils.getPetAttackTargetID(mob) != -1)
          TameableUtils.setPetAttackTarget(mob, -1);
      } else {
        TameableUtils.setPetAttackTarget(mob, mob.getTarget().getId());
      }
    } else if (sucking != null) {
      double dist = mob.distanceTo(sucking);
      Vec3 start = mob.position().add(0, mob.getBbHeight() * 0.5F, 0);
      Vec3 end = sucking.position().add(0, sucking.getBbHeight() * 0.5F, 0).subtract(start);
      for (float step = mob.getBbWidth() + 0.8F; step < (int) Math.ceil(dist); step++) {
        Vec3 vec = start.add(end.scale(step / dist));
        mob.level()
            .addParticle(
                PHParticleRegistry.MAGNET,
                vec.x + 0.5 * (mob.getRandom().nextFloat() - 0.5),
                vec.y + 0.5 * (mob.getRandom().nextFloat() - 0.5),
                vec.z + 0.5 * (mob.getRandom().nextFloat() - 0.5),
                0,
                0,
                0);
      }
    }
    if (sucking != null) {
      if (mob.tickCount % 15 == 0) mob.playSound(PHSoundRegistry.MAGNET_LOOP, 1F, 1F);
      mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.88D, 1.0D, 0.88D));
      Vec3 move =
          new Vec3(
              mob.getX() - sucking.getX(),
              mob.getY() - (double) sucking.getEyeHeight() / 2.0D - sucking.getY(),
              mob.getZ() - sucking.getZ());
      sucking.setDeltaMovement(
          sucking.getDeltaMovement().add(move.normalize().scale(mob.onGround() ? 0.15D : 0.05D)));
    }
  }
}
