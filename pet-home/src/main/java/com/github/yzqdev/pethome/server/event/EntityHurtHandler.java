package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.entity.ChainLightningEntity;
import com.github.yzqdev.pethome.server.entity.GiantBubbleEntity;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.RecallBallEntity;
import com.github.yzqdev.pethome.server.misc.PHEffects;
import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import com.github.yzqdev.pethome.server.misc.PHDamageTypes;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.util.FriendlyFireCommon;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
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
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.minecraft.world.damagesource.DamageTypes;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
/**
 * 伤害管线（全部服务端）：免疫帧检查 → {@code LivingIncomingDamageEvent}（HIGH）上友好保护、
 * 宠物防御附魔（烈焰/虹吸/召回/…）与宠物攻击附魔（链电/吸血/泡泡/霜牙/…）→
 * {@code LivingDamageEvent.Pre}（violent 随机效果）与 {@code Post}（chaos/share/paralysis 收尾 + 二次友好保护）。
 */
@EventBusSubscriber(modid = PetHomeMod.MODID)
public class EntityHurtHandler {
    /** 原版免疫帧挂钩：当前逻辑已整体注释保留（原有"主人不可伤自家宠物"移入 {@code handleFriendlyFire}）。 */
    @SubscribeEvent
    public static void onTameHurt(EntityInvulnerabilityCheckEvent event) {
//        if (event.getEntity() instanceof LivingEntity livingEntity) {
//            if (TameableUtils.isTamed(livingEntity) && event.getSource().getDirectEntity() instanceof Player player && TameableUtils.isPetOf(player, event.getEntity()) && !player.isShiftKeyDown()) {
//                event.setInvulnerable(true);
//
//            }
//            if (livingEntity.isBaby()) {
//                if (event.getSource().getEntity() instanceof Player player) {
//                    if (!player.isShiftKeyDown()) {
//                        event.setInvulnerable(true);
//                    }
//                }
//            }
//        }

    }
    /** 伤害入口（HIGH，仅服务端）：友好保护命中即取消；随后按"宠物是受害者（防御附魔）→ 宠物是攻击者（攻击附魔）"两段处理，防御段是否触发会传给攻击段。 */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        var level = event.getEntity().level();
        if (level.isClientSide()) return;

        // 1. 处理友好伤害逻辑
        if (handleFriendlyFire(event)) {
            return;
        }

        // 2. 检查是否有攻击者参与后续逻辑
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        // 3. 宠物作为受害者（被动防御）
        boolean flag = handlePetDefensiveEnchants(event, attacker);

        // 4. 宠物作为攻击者（主动攻击特效）
        handlePetOffensiveEnchants(event, attacker, flag);
    }

    /**
     * 处理友好伤害：如果触发了友好伤害保护，取消事件并清除仇恨
     */
    private static boolean handleFriendlyFire(LivingIncomingDamageEvent event) {
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
     * 处理宠物作为防御方的附魔逻辑
     * @return 是否触发了拦截逻辑(flag)
     */
    private static boolean handlePetDefensiveEnchants(LivingIncomingDamageEvent event, LivingEntity attacker) {
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

        // 烈焰保护（BLAZING_PROTECTION）
        if (TameableUtils.hasEnchant(victim, PHEnchantments.BLAZING_PROTECTION)) {
            int bars = TameableUtils.getBlazingProtectionBars(victim);
            if (bars > 0) {
                if (!TameableUtils.hasSameOwnerAs(attacker, victim)) {
                    attacker.igniteForTicks(20 * (5 + victim.getRandom().nextInt(3)));
                    attacker.knockback(0.4, victim.getX() - attacker.getX(), victim.getZ() - attacker.getZ());
                }
                event.setCanceled(true);
                flag = true;
                for (int i = 0; i < 3 + victim.getRandom().nextInt(3); i++) {
                    attacker.level().addParticle(ParticleTypes.FLAME, victim.getRandomX(0.8F), victim.getRandomY(), victim.getRandomZ(0.8F), 0.0F, 0.0F, 0.0F);
                }
                victim.playSound(PHSoundRegistry.BLAZING_PROTECTION.get(), 1, victim.getVoicePitch());
                TameableUtils.setBlazingProtectionBars(victim, bars - 1);
                TameableUtils.setBlazingProtectionCooldown(victim, 600);
            }
        }

        // 生命虹吸（HEALTH_SIPHON）：把宠物受到的部分伤害转移给主人。
        // 跳过 /kill 等 generic_kill 清除型伤害：其伤害量为 Float.MAX_VALUE，原样转移会直接秒杀主人
        if (!flag && !event.getSource().is(DamageTypes.GENERIC_KILL)
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
                    victim.hurtServer(victimLevel, PHDamageTypes.causeSiphonDamage(owner.level().registryAccess()), 0.0F);
                }
            }
        }

        // 紧急收回（TOTAL_RECALL）
        if (!flag && TameableUtils.hasEnchant(victim, PHEnchantments.TOTAL_RECALL) && victim.getHealth() - event.getAmount() <= 2.0D) {
            UUID ownerUUID = TameableUtils.getOwnerUUIDOf(victim);
            if (ownerUUID != null) {
                if (victim instanceof Mob mob) mob.playAmbientSound();
                victim.playSound(SoundEvents.ENDER_CHEST_CLOSE, 1.0F, 1.5F);

                RecallBallEntity recallBall = PHEntityRegistry.RECALL_BALL.get().create(victim.level(), EntitySpawnReason.EVENT);
                recallBall.setOwnerUUID(ownerUUID);
                // 26.1: addAdditionalSaveData 改为写入 ValueOutput
                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, victim.registryAccess());
                victim.saveWithoutId(output);
                CompoundTag tag = output.buildResult();
                recallBall.setContainedData(tag);
                recallBall.setContainedEntityType(BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString());
                recallBall.setPos(victim.getX(), Math.max(victim.getY(), victim.level().getMinY() + 1), victim.getZ());
                recallBall.setYRot(victim.getYRot());
                recallBall.setInvulnerable(true);

                victim.stopRiding();
                
                if (victim.level() instanceof ServerLevel victimLevel && victimLevel.addFreshEntity(recallBall)) {
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
     * 处理宠物作为攻击方的附魔逻辑
     */
    private static void handlePetOffensiveEnchants(LivingIncomingDamageEvent event, LivingEntity attacker, boolean alreadyTriggeredDefensive) {
        var victim = event.getEntity();
        if (!TameableUtils.isTamed(attacker)) return;

        // 1. 链锁闪电
        int lightningLevel = TameableUtils.getEnchantLevel(attacker, PHEnchantments.CHAIN_LIGHTNING);
        if (lightningLevel > 0) {
            ChainLightningEntity lightning = PHEntityRegistry.CHAIN_LIGHTNING.get().create(victim.level(), EntitySpawnReason.EVENT);
            lightning.setCreatorEntityID(attacker.getId());
            lightning.setFromEntityID(attacker.getId());
            lightning.setToEntityID(victim.getId());
            lightning.copyPosition(victim);
            lightning.setChainsLeft(3 + lightningLevel * 3);
            
            if (victim.level() instanceof ServerLevel victimLevel) {
                victimLevel.addFreshEntity(lightning);
            }
            victim.playSound(PHSoundRegistry.CHAIN_LIGHTNING.get(), 1F, 1F);
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
                    serverLevel.sendParticles(PHParticleRegistry.VAMPIRE.get(), f1, f2, f3, 1, motion.x, motion.y, motion.z, 0.2F);
                }
            }
        }

        // 3. 泡泡控制
        int bubblingLevel = TameableUtils.getEnchantLevel(attacker, PHEnchantments.BUBBLING);
        if (bubblingLevel > 0) {
            if (!(victim.getRootVehicle() instanceof GiantBubbleEntity) && (victim.onGround() || victim.isInWater() || victim.isInLava())) {
                GiantBubbleEntity bubble = PHEntityRegistry.GIANT_BUBBLE.get().create(victim.level(), EntitySpawnReason.EVENT);
                bubble.copyPosition(victim);
                victim.startRiding(bubble, true, false);
                bubble.setpopsIn(bubblingLevel * 40 + 40);
                
                if (victim.level() instanceof ServerLevel victimLevel) {
                    victimLevel.addFreshEntity(bubble);
                }
                victim.playSound(PHSoundRegistry.GIANT_BUBBLE_INFLATE.get(), 1F, 1F);
            }
        }

        // 4. 霜冻
        if (TameableUtils.hasEnchant(attacker, PHEnchantments.FROST_FANG)) {
            victim.setTicksFrozen(victim.getTicksRequiredToFreeze() + 200);
            Vec3 vec3 = victim.getEyePosition().subtract(attacker.getEyePosition()).normalize().scale(attacker.getBbWidth() + 0.5F);
            Vec3 particlePos = attacker.getEyePosition().add(vec3);
            for (int i = 0; i < 3 + attacker.getRandom().nextInt(3); i++) {
                float f1 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
                float f2 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
                float f3 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
                attacker.level().addParticle(ParticleTypes.SNOWFLAKE, particlePos.x + f1, particlePos.y + f2, particlePos.z + f3, 0.0F, 0.0F, 0.0F);
            }
            TameableUtils.setFrozenTimeTag(victim, 60);
        }

        // 5. 传送攻击
        if (TameableUtils.hasEnchant(attacker, PHEnchantments.WARPING_BITE)) {
            for (int i = 0; i < 16; ++i) {
                double d3 = victim.getX() + (attacker.getRandom().nextDouble() - 0.5D) * 16.0D;
                double d4 = Mth.clamp(victim.getY() + (double) (attacker.getRandom().nextInt(16) - 8), victim.level().getMinY(), victim.level().getMinY() + ((ServerLevel) victim.level()).getLogicalHeight() - 1);
                double d5 = victim.getZ() + (attacker.getRandom().nextDouble() - 0.5D) * 16.0D;
                if (victim.randomTeleport(d3, d4, d5, true)) {
                    SoundEvent soundevent = victim instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                    victim.playSound(soundevent, 1.0F, 1.0F);
                    break;
                }
            }
        }

        // 6. 治疗光环冲能
        if (!event.isCanceled()) {
            List<LivingEntity> nearbyHealers = TameableUtils.getNearbyHealers(victim);
            if (!nearbyHealers.isEmpty()) {
                for (LivingEntity healer : nearbyHealers) {
                    TameableUtils.setHealingAuraImpulse(healer, true);
                }
            }
        }
    }

    /** violent 附魔的 Pre 阶段判定（攻击方持有）：按随机数 8 档——1% 即死、中毒/三重减益/加伤/点燃/按血量斩杀/醉酒/减速。 */
    @SubscribeEvent
    public static void onEntityHurtPre(LivingDamageEvent.Pre event) {
        var maid = event.getSource().getEntity();

        var monster = event.getEntity();
        var level = monster.level();
        var chance = level.getRandom().nextFloat();




        if (maid instanceof LivingEntity maidLiving && TameableUtils.hasEnchant(maidLiving, PHEnchantments.VIOLENT)) {
            // 26.x 伤害管线禁止在 Pre 里把受害者杀死：LivingEntity.actuallyHurt 在 CommonHooks.onLivingDamagePre
            // 返回后立即 Preconditions.checkArgument(!this.dead, ...)，此处 die() 会抛 IllegalArgumentException
            // 并冒泡成 "Ticking entity" 崩溃。改为把伤害抬到必死值，由 hurtServer 在管线末端按原版流程
            // die(source)（图腾保护/掉落/死亡音效/Death 事件均照常），效果等价于旧实现的即死。
            if (chance < 0.01) {
                event.setNewDamage(monster.getHealth() + 1.0F);
            } else if (chance < 0.11) {
                monster.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 5));
            } else if (chance < 0.21) {
                var paralysicLevel = 1;
                monster.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, paralysicLevel * 20, 100, false, false));
                monster.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, paralysicLevel * 20, 100, false, false));
                monster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, paralysicLevel * 20, 100, false, false));
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

    /** 伤害落定后（受害者视角）：chaos 给攻击者醉酒、share 把伤害平摊给 20 格内怪物、paralysis 给攻击者挂三重减益；最后再做一次友好保护兜底（清双方仇恨）。 */
    @SubscribeEvent
    public static void onEntityHurt(LivingDamageEvent.Post event) {
        var hurtEntity = event.getEntity();
        var attacker = event.getSource().getEntity();
        if (TameableUtils.hasEnchant(hurtEntity, PHEnchantments.CHAOS) && attacker instanceof LivingEntity) {

            ((LivingEntity) attacker).addEffect(new MobEffectInstance(PHEffects.DRUNK, 120, 1));
        }
        var shareEnchantLevel = TameableUtils.getEnchantLevel(hurtEntity, PHEnchantments.SHARE);
        if (shareEnchantLevel > 0 && attacker instanceof LivingEntity attackerLiving) {
            var monsterEntities = TameableUtils.getNearbyMobs(hurtEntity, 20).stream().filter(i -> i instanceof Enemy).collect(
                    Collectors.toSet());

            if (monsterEntities.size() > 1) {
                float originalDamage = event.getOriginalDamage();
                monsterEntities.forEach(i -> {
                    if (i.level() instanceof ServerLevel serverLevel) {
                        i.hurtServer(serverLevel, event.getSource(), (float) (originalDamage * 0.3));
                    }
                });

            }
        }
        var paralysicLevel = TameableUtils.getEnchantLevel(hurtEntity, PHEnchantments.PARALYSIS);
        if (paralysicLevel > 0 && attacker instanceof LivingEntity attackerLiving) {

            attackerLiving.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, paralysicLevel * 20, 100, false, false));
            attackerLiving.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, paralysicLevel * 20, 100, false, false));
            attackerLiving.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, paralysicLevel * 20, 100, false, false));

        }

        if (FriendlyFireCommon.preventAttack(event.getEntity(), event.getSource(), event.getHealthDamage())) {


            event.getEntity().setLastHurtByMob(null);

            if (event.getSource().getEntity() instanceof LivingEntity trueSource) {

                trueSource.setLastHurtByMob(null);
            }
        }

    }

}
