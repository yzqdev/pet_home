package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.client.ClientGameEvents;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.entity.GiantBubbleEntity;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.PsychicWallEntity;
import com.github.yzqdev.pethome.server.misc.PHEffects;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import com.github.yzqdev.pethome.util.LivingUtils;
import com.github.yzqdev.pethome.util.TameableUtils;
import java.util.List;
import java.util.Random;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 实体 tick 附魔逻辑中心：由 {@code EntityTickMixin} 在每个 {@code Entity#tick} HEAD 调入 {@link
 * #onLivingUpdate}（Fabric 无逐实体 tick 回调，因此直接挂实体 tick），对可指挥的 {@code Mob} 依次执行罗盘档案更新、{@code DRUNK}
 * 效果行为与四组附魔逻辑。双端都会进入（客户端部分分支用于 粒子/视觉数据），生成实体与改数据的分支均有 {@code isClientSide} 判断。
 */
public class EntityTickHandler {

  /** 空实现占位（对齐 NeoForge 结构；本类事件经 Mixin 接入而非 Fabric 回调），由主入口调用以保持初始化顺序一致。 */
  public static void init() {}

  /** 每实体 tick 入口（EntityTickMixin 调入）：20 tick 一次罗盘档案更新 → DRUNK 怪物行为 → 四组附魔逻辑（生存/实用/战斗/控制）。 */
  public static void onLivingUpdate(Entity entity) {
    if (entity instanceof Mob pet && ServerEvent.canTickCollar(entity)) {
      // 宠物罗盘：20 tick 低频档案更新（首次驯服懒初始化 PetId + 位置/名字/维度刷新，无变化不写盘）
      if (pet.tickCount % 20 == 0) {
        PetCompassTracker.updateRecord(pet);
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

  /** {@code drunk}（醉酒）效果的怪物行为：10 格内随机"攻击"另一只怪物（互殴），头顶冒问号粒子；仅服务端。 */
  public static void mobTick(LivingEntity attacker) {

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

  /**
   * 生存组附魔：烈焰保护蓄条（200 tick/条，上限 2×等级）、无敌帧倒计时、毒素免疫、两栖回气、治愈光环（周期给 4 格内同主人单位再生，脉冲触发由 {@code
   * PetHealingAuraImpulse} 驱动）。
   */
  private static void handleSurvivalEnchants(Mob pet) {
    // Blazing Protection
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

    // Immunity Frame
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
    if (TameableUtils.hasEnchant(pet, PHEnchantments.HEALING_AURA) && !pet.level().isClientSide()) {
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

  /** 实用组附魔：夜视（10 格内主人 5 分钟夜视）、虚空云（下落缓降跟随主人）、矿石嗅探（生成高亮框）、连锁背包（开启拾取）、洞察（黑暗中给敌人发光）。 */
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

    // Void Cloud (虚空云/缓降)
    if (TameableUtils.hasEnchant(pet, PHEnchantments.VOID_CLOUD)
        && !pet.isInWaterOrBubble()
        && pet.fallDistance > 3.0F
        && !pet.onGround()) {
      Entity owner = TameableUtils.getOwnerOf(pet);
      boolean shouldMoveToOwnerXZ = owner != null && Math.abs(owner.getY() - pet.getY()) < 1;
      double targetX = shouldMoveToOwnerXZ ? owner.getX() : pet.getX();
      double targetY =
          Math.max(
              pet.level().getMinBuildHeight() + 0.5F,
              owner == null
                  ? 64F
                  : owner.getY() < pet.getY()
                      ? owner.getY() + 0.6F
                      : owner.getY(1.0F) + pet.getBbHeight());
      if (owner != null && owner.getRootVehicle() == pet) {
        targetY = Math.min(pet.level().getMinBuildHeight() + 0.5F, pet.getY() - 0.5F);
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
        TameableUtils.setFallDistance(pet, pet.fallDistance);
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
    }

    // Ore Scenting (矿石嗅探)
    int oreLvl = TameableUtils.getEnchantLevel(pet, PHEnchantments.ORE_SCENTING);
    if (oreLvl > 0 && !pet.level().isClientSide && pet.isAlive()) {
      int interval = 100 + Math.max(150, 550 - oreLvl * 100);
      TameableUtils.detectRandomOres(pet, interval, 5 + oreLvl * 2, oreLvl * 50, oreLvl * 3);
    }

    // Linked Inventory
    if (TameableUtils.hasEnchant(pet, PHEnchantments.LINKED_INVENTORY)) {
      if (!pet.canPickUpLoot()) pet.setCanPickUpLoot(true);
    }

    // Insight (洞察 )
    var insightLevel = TameableUtils.getEnchantLevel(pet, PHEnchantments.INSIGHT);
    if (insightLevel > 0 && pet.level() instanceof ServerLevel serverLevel) {
      if (serverLevel.getMaxLocalRawBrightness(pet.getOnPos().above()) < 9) {
        TameableUtils.applyGlowingEffect(pet, insightLevel);
      }
    }
  }

  /** 战斗组附魔：音波轰击（200 tick 冷却的群体/单体音爆）、暗影之手（见 {@link #handleShadowHandsLogic}）、经验吸收（吸附经验球回血）。 */
  private static void handleCombatEnchants(Mob pet) {
    // Sonic Boom
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
   * 暗影之手：按等级维护 {@code level} 只拳的出拳进度/出击状态数组（存 citadel 数据并同步， 客户端经 {@code
   * ClientGameEvents.updateVisualDataForMob} 取数组渲染拳模型）；目标限 16 格内有视线， 拳满 10 tick 进度造成 {@code
   * clamp(level,2,4)} 点伤害；目标失效则回收进度并从主人最近受击/攻击对象找新目标。
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
              punching.hurt(punching.damageSources().mobAttack(mob), Mth.clamp(level, 2, 4));
              striking[i] = 0;
            }
          }
          if (striking[i] == 0 && punchProgress[i] > 0) punchProgress[i]--;
        }
        TameableUtils.setShadowPunchStriking(mob, striking);
        TameableUtils.setShadowPunchTimes(mob, punchProgress);
      }
    } else {
      // 目标失效处理逻辑
      if (punching != null) {
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
      }
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
   * 控制组附魔：枯萎诅咒、臭名昭著、威吓、牧羊人、念力墙（冷却好后在宠物/主人与威胁目标之间生成 PsychicWallEntity）、引力（见 {@link
   * #handleMagneticLogic}）。
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

    // Psychic Wall
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
          Direction dir = Direction.getNearest(rotationFrom.x, rotationFrom.y, rotationFrom.z);
          PsychicWallEntity wall = PHEntityRegistry.PSYCHIC_WALL.create(pet.level());
          wall.setPos(avg.x, avg.y, avg.z);
          wall.setBlockWidth(width);
          wall.setCreatorId(pet.getUUID());
          wall.setLifespan(psychicWallLevel * 100);
          wall.setWallDirection(dir);
          pet.level().addFreshEntity(wall);
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
   * 引力：把"被吸入目标"（记忆于 {@code PetAttackTarget}）拉向宠物（服务端改速度），同时宠物自身被轻微拖向目标；
   * 目标接近/死亡/被巨型泡泡抓住则解除；客户端按目标连线画 MAGNET 粒子并周期播循环音效。
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
