package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.entity.ChainLightningEntity;
import com.github.yzqdev.pethome.server.entity.GiantBubbleEntity;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.RecallBallEntity;
import com.github.yzqdev.pethome.server.misc.PHEffects;
import com.github.yzqdev.pethome.server.misc.PHDamageTypes;
import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.util.FriendlyFireCommon;
import com.github.yzqdev.pethome.util.TameableUtils;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * 受击/攻击伤害处理核心。入口 {@link #onLivingDamage} 由 {@code mixin/LivingEntityHurtMixin} 在 {@code
 * LivingEntity#hurtServer} HEAD 调入（可取消/可改伤）；{@link #onEntityHurtPre}/ {@link #onEntityHurt} 由同一
 * Mixin 在伤害 Post 阶段调入。全部仅服务端执行。
 */
public class EntityHurtHandler {

  /** 递归应用「被改写的伤害」时置位，防止 hurtServer 的注入再次进入 */
  private static boolean applyingAdjustedDamage = false;

  /** 重入保护当前是否生效（Mixin 据此跳过注入）。 */
  public static boolean isApplyingAdjustedDamage() {
    return applyingAdjustedDamage;
  }

  /** 进入重入保护区；已在保护区内返回 false（嵌套调用直接放行原版逻辑）。 */
  public static boolean beginAdjustedDamage() {
    if (applyingAdjustedDamage) {
      return false;
    }
    applyingAdjustedDamage = true;
    return true;
  }

  /** 退出重入保护区（与 {@link #beginAdjustedDamage} 成对，finally 中调用）。 */
  public static void endAdjustedDamage() {
    applyingAdjustedDamage = false;
  }

  /**
   * {@code hurtServer} HEAD 的总入口，顺序：① 友军伤害保护（命中即取消并双向清仇恨， 直接返回）；② 无实体攻击者不处理；③ 宠物作为受害者走防御附魔；④
   * 宠物作为攻击者 走攻击附魔（防御已触发时跳过部分逻辑）。
   */
  public static void onLivingDamage(IncomingDamage event) {
    var level = event.getEntity().level();
    if (level.isClientSide()) return;

    // 1. 处理友好伤害逻辑
    if (handleFriendlyFire(event)) {
      return;
    }

    // 2. 宠物作为受害者（被动防御）：不要求有攻击者实体——摔落/溺水/掉出世界等环境伤害的 source 没有实体，
    // 1.20 侧防御链对所有伤害类型生效，此处对齐；攻击者实体仅烈焰保护的反击部分需要（内部判空）
    LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity la ? la : null;
    boolean flag = handlePetDefensiveEnchants(event, attacker);

    // 3. 治疗光环冲能：伤害未被防御链取消时，让附近有治疗光环的宠物进入治疗（ 不依赖攻击者是否驯服）
    if (!event.isCanceled()) {
      List<LivingEntity> nearbyHealers = TameableUtils.getNearbyHealers(event.getEntity());
      for (LivingEntity healer : nearbyHealers) {
        TameableUtils.setHealingAuraImpulse(healer, true);
      }
    }

    // 4. 宠物作为攻击者（主动攻击特效）：需要攻击者是实体
    if (attacker != null) {
      handlePetOffensiveEnchants(event, attacker, flag);
    }
  }

  /** 处理友好伤害：如果触发了友好伤害保护，取消事件并清除仇恨 */
  private static boolean handleFriendlyFire(IncomingDamage event) {
    if (FriendlyFireCommon.preventAttack(event.getEntity(), event.getSource(), event.getAmount())) {
      event.setCanceled(true);
      event.getEntity().setLastHurtByMob(null);
      if (event.getSource().getEntity() instanceof LivingEntity trueSource) {
        trueSource.setLastHurtByMob(null);
      }
      return true;
    }
    return false;
  }

  /**
   * 宠物受击的防御附魔（虹吸伤害占位源除外，防递归）：免疫屏障（首击开免疫窗口）、 烈焰保护（耗 1 条蓄条抵挡，点燃+击退攻击者，同主人不打）、虚空云（免疫摔落/掉出世界）、
   * 生命虹吸（伤害原样转给 100 格内主人，宠物以 0 点 SIPHON 伤害占位——用于抑制本分支递归）、 紧急收回（本次伤害致死时收入 {@code RecallBallEntity} 并
   * discard 原实体）。
   *
   * @param attacker 攻击者实体；环境伤害（摔落/溺水/掉出世界等）时为 null，仅反击/粒子相关分支需要判空
   * @return 是否已取消本次伤害（防御分支触发）
   */
  private static boolean handlePetDefensiveEnchants(IncomingDamage event, LivingEntity attacker) {
    var victim = event.getEntity();
    if (!TameableUtils.isTamed(victim) || event.getSource().is(PHDamageTypes.SIPHON)) {
      return false;
    }

    boolean flag = false;

    // 免疫屏障（IMMUNITY_FRAME）：首击启动 (1+等级) 秒免疫窗口，窗口内的后续伤害直接取消
    if (TameableUtils.hasEnchant(victim, PHEnchantments.IMMUNITY_FRAME)) {
      int level = TameableUtils.getEnchantLevel(victim, PHEnchantments.IMMUNITY_FRAME);
      if (TameableUtils.getImmuneTime(victim) <= 0) {
        TameableUtils.setImmuneTime(victim, 20 + level * 20);
      } else {
        event.setCanceled(true);
        flag = true;
      }
    }

    // BLAZING_PROTECTION: 烈焰保护
    if (TameableUtils.hasEnchant(victim, PHEnchantments.BLAZING_PROTECTION)) {
      int bars = TameableUtils.getBlazingProtectionBars(victim);
      if (bars > 0) {
        if (attacker != null) {
          if (!TameableUtils.hasSameOwnerAs(attacker, victim)) {
            attacker.igniteForTicks(20 * (5 + victim.getRandom().nextInt(3)));
            attacker.knockback(0.4, victim.getX() - attacker.getX(), victim.getZ() - attacker.getZ());
          }
          for (int i = 0; i < 3 + victim.getRandom().nextInt(3); i++) {
            attacker
                .level()
                .addParticle(
                    ParticleTypes.FLAME,
                    victim.getRandomX(0.8F),
                    victim.getRandomY(),
                    victim.getRandomZ(0.8F),
                    0.0F,
                    0.0F,
                    0.0F);
          }
        }
        event.setCanceled(true);
        flag = true;
        victim.playSound(PHSoundRegistry.BLAZING_PROTECTION, 1, victim.getVoicePitch());
        TameableUtils.setBlazingProtectionBars(victim, bars - 1);
        TameableUtils.setBlazingProtectionCooldown(victim, 600);
      }
    }

    // 虚空云（VOID_CLOUD）：免疫摔落/掉出世界伤害（ 环境伤害 source 无实体，本链已不依赖 attacker）
    if (!flag
        && (event.getSource().is(DamageTypes.FALL)
            || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD))
        && TameableUtils.hasEnchant(victim, PHEnchantments.VOID_CLOUD)) {
      event.setCanceled(true);
      flag = true;
    }

    // HEALTH_SIPHON: 生命虹吸 (将伤害转移给主人)
    // 跳过 /kill 等 generic_kill 清除型伤害：其伤害量为 Float.MAX_VALUE，原样转移会直接秒杀主人
    if (!flag
        && !event.getSource().is(DamageTypes.GENERIC_KILL)
        && TameableUtils.hasEnchant(victim, PHEnchantments.HEALTH_SIPHON)) {
      Entity owner = TameableUtils.getOwnerOf(victim);
      if (owner != null && owner.isAlive() && owner.distanceTo(victim) < 100 && owner != victim) {
        if (owner.level() instanceof ServerLevel ownerLevel) {
          owner.hurtServer(ownerLevel, event.getSource(), event.getAmount());
        }
        event.setCanceled(true);
        flag = true;
        // 取消原始伤害会让原版跳过 resolveMobResponsibleForDamage（仇恨记录），宠物失去反击目标；
        // 此处手动补记，与未附魔时行为一致（复刻原版的 NO_ANGER 过滤）
        if (attacker != null && !event.getSource().is(DamageTypeTags.NO_ANGER)) {
          victim.setLastHurtByMob(attacker);
        }
        if (victim.level() instanceof ServerLevel victimLevel) {
          victim.hurtServer(
              victimLevel, PHDamageTypes.causeSiphonDamage(owner.level().registryAccess()), 0.0F);
        }
      }
    }

    // TOTAL_RECALL: 紧急收回
    if (!flag
        && TameableUtils.hasEnchant(victim, PHEnchantments.TOTAL_RECALL)
        && victim.getHealth() - event.getAmount() <= 2.0D) {
      UUID ownerUUID = TameableUtils.getOwnerUUIDOf(victim);
      if (ownerUUID != null) {
        if (victim instanceof Mob mob) mob.playAmbientSound();
        victim.playSound(SoundEvents.ENDER_CHEST_CLOSE, 1.0F, 1.5F);

        RecallBallEntity recallBall =
            PHEntityRegistry.RECALL_BALL.create(victim.level(), EntitySpawnReason.EVENT);
        recallBall.setOwnerUUID(ownerUUID);
        // 26.1: addAdditionalSaveData 改为写入 ValueOutput
        TagValueOutput output =
            TagValueOutput.createWithContext(ProblemReporter.DISCARDING, victim.registryAccess());
        victim.saveWithoutId(output);
        CompoundTag tag = output.buildResult();
        recallBall.setContainedData(tag);
        recallBall.setContainedEntityType(
            BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString());
        recallBall.setPos(
            victim.getX(), Math.max(victim.getY(), victim.level().getMinY() + 1), victim.getZ());
        recallBall.setYRot(victim.getYRot());
        recallBall.setInvulnerable(true);

        victim.stopRiding();

        if (victim.level() instanceof ServerLevel victimLevel
            && victimLevel.addFreshEntity(recallBall)) {
          victim.discard();
          // 罗盘档案标记「宝盒中」，位置改为宝盒落点（否则罗盘找不到实体、召回必失败）
          PetCompassTracker.onPetCapturedByRecallBall(victim, recallBall);
        }
        flag = true;
        event.setCanceled(true);
      }
    }
    return flag;
  }

  /**
   * 宠物作为攻击者的攻击附魔：链锁闪电（生成 {@code ChainLightningEntity}，跳数 {@code 3+3×等级}）、吸血（回血 {@code
   * clamp(伤害×0.5×等级, 1, 10)} + VAMPIRE 粒子）、 泡泡控制（目标骑上 {@code GiantBubbleEntity} 上浮，寿命 {@code
   * 40+40×等级}）、 霜冻（冻结 +200 tick）、传送攻击（目标末影珍珠式随机传送 16 次）、治疗光环冲能 （通知附近空闲治疗宠物立即起效）。
   */
  private static void handlePetOffensiveEnchants(
      IncomingDamage event, LivingEntity attacker, boolean alreadyTriggeredDefensive) {
    var victim = event.getEntity();
    if (!TameableUtils.isTamed(attacker)) return;

    // 1. 链锁闪电
    int lightningLevel = TameableUtils.getEnchantLevel(attacker, PHEnchantments.CHAIN_LIGHTNING);
    if (lightningLevel > 0) {
      ChainLightningEntity lightning =
          PHEntityRegistry.CHAIN_LIGHTNING.create(victim.level(), EntitySpawnReason.EVENT);
      lightning.setCreatorEntityID(attacker.getId());
      lightning.setFromEntityID(attacker.getId());
      lightning.setToEntityID(victim.getId());
      lightning.copyPosition(victim);
      lightning.setChainsLeft(3 + lightningLevel * 3);

      if (victim.level() instanceof ServerLevel victimLevel) {
        victimLevel.addFreshEntity(lightning);
      }
      victim.playSound(PHSoundRegistry.CHAIN_LIGHTNING, 1F, 1F);
    }

    // 2. 吸血
    int vampireLevel = TameableUtils.getEnchantLevel(attacker, PHEnchantments.VAMPIRE);
    if (vampireLevel > 0 && attacker.getHealth() < attacker.getMaxHealth()) {
      float healAmount = Mth.clamp(event.getAmount() * vampireLevel * 0.5F, 1F, 10F);
      attacker.heal(healAmount);
      if (victim.level() instanceof ServerLevel serverLevel) {
        for (int i = 0; i < 5 + victim.getRandom().nextInt(3); i++) {
          double f1 = victim.getRandomX(0.7F);
          double f2 = victim.getY(0.4F + victim.getRandom().nextFloat() * 0.2F);
          double f3 = victim.getRandomZ(0.7F);
          Vec3 motion = attacker.getEyePosition().subtract(f1, f2, f3).normalize().scale(0.2F);
          serverLevel.sendParticles(
              PHParticleRegistry.VAMPIRE, f1, f2, f3, 1, motion.x, motion.y, motion.z, 0.2F);
        }
      }
    }

    // 3. 泡泡控制
    int bubblingLevel = TameableUtils.getEnchantLevel(attacker, PHEnchantments.BUBBLING);
    if (bubblingLevel > 0) {
      if (!(victim.getRootVehicle() instanceof GiantBubbleEntity)
          && (victim.onGround() || victim.isInWater() || victim.isInLava())) {
        GiantBubbleEntity bubble =
            PHEntityRegistry.GIANT_BUBBLE.create(victim.level(), EntitySpawnReason.EVENT);
        bubble.copyPosition(victim);
        victim.startRiding(bubble, true, false);
        bubble.setpopsIn(bubblingLevel * 40 + 40);

        if (victim.level() instanceof ServerLevel victimLevel) {
          victimLevel.addFreshEntity(bubble);
        }
        victim.playSound(PHSoundRegistry.GIANT_BUBBLE_INFLATE, 1F, 1F);
      }
    }

    // 4. 霜冻
    if (TameableUtils.hasEnchant(attacker, PHEnchantments.FROST_FANG)) {
      victim.setTicksFrozen(victim.getTicksRequiredToFreeze() + 200);
      Vec3 vec3 =
          victim
              .getEyePosition()
              .subtract(attacker.getEyePosition())
              .normalize()
              .scale(attacker.getBbWidth() + 0.5F);
      Vec3 particlePos = attacker.getEyePosition().add(vec3);
      for (int i = 0; i < 3 + attacker.getRandom().nextInt(3); i++) {
        float f1 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
        float f2 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
        float f3 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
        attacker
            .level()
            .addParticle(
                ParticleTypes.SNOWFLAKE,
                particlePos.x + f1,
                particlePos.y + f2,
                particlePos.z + f3,
                0.0F,
                0.0F,
                0.0F);
      }
      TameableUtils.setFrozenTimeTag(victim, 60);
    }

    // 5. 传送攻击
    if (TameableUtils.hasEnchant(attacker, PHEnchantments.WARPING_BITE)) {
      for (int i = 0; i < 16; ++i) {
        double d3 = victim.getX() + (attacker.getRandom().nextDouble() - 0.5D) * 16.0D;
        double d4 =
            Mth.clamp(
                victim.getY() + (double) (attacker.getRandom().nextInt(16) - 8),
                victim.level().getMinY(),
                victim.level().getMinY() + ((ServerLevel) victim.level()).getLogicalHeight() - 1);
        double d5 = victim.getZ() + (attacker.getRandom().nextDouble() - 0.5D) * 16.0D;
        if (victim.randomTeleport(d3, d4, d5, true)) {
          SoundEvent soundevent =
              victim instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
          victim.playSound(soundevent, 1.0F, 1.0F);
          break;
        }
      }
    }
  }

  /**
   * 伤害 Post 前（mixin 调入）：攻击方宠物的 VIOLENT 附魔随机效果表——1% 即死、 1~10% 中毒、10~20% 麻痹三减益、20~40% +3 伤害、40~60%
   * 点燃、60~70% 斩杀 （30 血以上按 1/3 血计伤）、70~80% 醉酒、其余减速+虚弱。仅服务端有意义。
   */
  public static void onEntityHurtPre(LivingDamageContext event) {
    var maid = event.getSource().getEntity();

    var monster = event.getEntity();
    var level = monster.level();
    var chance = level.getRandom().nextFloat();

    //        var logStr =
    // "ab.yzq.mod.pet_home.server.ServerProxy.onEntityHurtPre(ServerProxy.java:425)";
    //        PetHomeMod.LOGGER.info(logStr);
    //        PetHomeMod.LOGGER.info(String.valueOf(chance));

    if (maid instanceof LivingEntity maidLiving
        && TameableUtils.hasEnchant(maidLiving, PHEnchantments.VIOLENT)) {
      // 26.x 伤害管线禁止在 Pre 阶段把受害者杀死：LivingEntity.hurtServer 管线在伤害 Pre 处理返回后
      // 会校验 !this.isDeadOrDying()，此处提前 die() 会抛 IllegalArgumentException 并冒泡成
      // "Ticking entity" 崩溃。改为把伤害抬到必死值，由管线末端按原版流程 die(source)
      //（图腾保护/掉落/死亡音效/死亡事件均照常），效果等价于旧实现的即死。
      if (chance < 0.01) {
        event.setNewDamage(monster.getHealth() + 1.0F);
      } else if (chance < 0.11) {
        monster.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 5));
      } else if (chance < 0.21) {
        var paralysicLevel = 1;
        monster.addEffect(
            new MobEffectInstance(MobEffects.SLOWNESS, paralysicLevel * 20, 100, false, false));
        monster.addEffect(
            new MobEffectInstance(
                MobEffects.MINING_FATIGUE, paralysicLevel * 20, 100, false, false));
        monster.addEffect(
            new MobEffectInstance(MobEffects.WEAKNESS, paralysicLevel * 20, 100, false, false));
      } else if (chance < 0.41) {
        event.setNewDamage(event.getOriginalDamage() + 3);
      } else if (chance < 0.60) {
        monster.igniteForTicks(20 * 5);
      } else if (chance < 0.7) {
        if (monster.getHealth() > 30) {
          event.setNewDamage(monster.getHealth() / 3);
        } else {
          event.setNewDamage(event.getOriginalDamage() + 5);
        }

      } else if (chance < 0.8) {
        monster.addEffect(new MobEffectInstance(PHEffects.DRUNK, 20 * 5));
      } else {
        monster.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 1 * 20, 50, false, false));
        monster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1 * 20, 50, false, false));
      }
    }
  }

  /**
   * 伤害 Post（mixin 调入），按受击方附魔：CHAOS（攻击者醉酒 6 秒）、SHARE （20 格内有 ≥2 只敌对时把 30%
   * 原始伤害分摊给它们）、PARALYSIS（攻击者三减益， 时长 20×等级）、最后再跑一遍友军保护判定（清仇恨，不改结果）。
   */
  public static void onEntityHurt(LivingDamageContext event) {
    var hurtEntity = event.getEntity();
    var attacker = event.getSource().getEntity();
    if (TameableUtils.hasEnchant(hurtEntity, PHEnchantments.CHAOS)
        && attacker instanceof LivingEntity) {

      ((LivingEntity) attacker).addEffect(new MobEffectInstance(PHEffects.DRUNK, 120, 1));
    }
    var shareEnchantLevel = TameableUtils.getEnchantLevel(hurtEntity, PHEnchantments.SHARE);
    if (shareEnchantLevel > 0 && attacker instanceof LivingEntity attackerLiving) {
      var monsterEntities =
          TameableUtils.getNearbyMobs(hurtEntity, 20).stream()
              .filter(i -> i instanceof Enemy)
              .collect(Collectors.toSet());

      if (monsterEntities.size() > 1) {
        float originalDamage = event.getOriginalDamage();
        monsterEntities.forEach(
            i -> {
              if (i.level() instanceof ServerLevel serverLevel) {
                i.hurtServer(serverLevel, event.getSource(), (float) (originalDamage * 0.3));
              }
            });
      }
    }
    var paralysicLevel = TameableUtils.getEnchantLevel(hurtEntity, PHEnchantments.PARALYSIS);
    if (paralysicLevel > 0 && attacker instanceof LivingEntity attackerLiving) {

      attackerLiving.addEffect(
          new MobEffectInstance(MobEffects.SLOWNESS, paralysicLevel * 20, 100, false, false));
      attackerLiving.addEffect(
          new MobEffectInstance(MobEffects.MINING_FATIGUE, paralysicLevel * 20, 100, false, false));
      attackerLiving.addEffect(
          new MobEffectInstance(MobEffects.WEAKNESS, paralysicLevel * 20, 100, false, false));
    }

    if (FriendlyFireCommon.preventAttack(
        event.getEntity(), event.getSource(), event.getHealthDamage())) {

      event.getEntity().setLastHurtByMob(null);

      if (event.getSource().getEntity() instanceof LivingEntity trueSource) {

        trueSource.setLastHurtByMob(null);
      }
    }
  }
}
