package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.datagen.ModEnchantments;
import com.github.yzqdev.pethome.server.entity.ChainLightningEntity;
import com.github.yzqdev.pethome.server.entity.GiantBubbleEntity;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.RecallBallEntity;
import com.github.yzqdev.pethome.server.misc.ModEffects;
import com.github.yzqdev.pethome.server.misc.PHDamageTypes;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.util.FriendlyFireCommon;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public class EntityHurtHandler {

    public static void init() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            var level = entity.level();
            if (level.isClientSide()) return true;

            // 1. 处理友好伤害逻辑
            if (handleFriendlyFire(entity, source, amount)) {
                return false;
            }

            // 2. 检查是否有攻击者参与后续逻辑
            if (!(source.getEntity() instanceof LivingEntity attacker)) {
                return true;
            }

            // 3. 宠物作为受害者（被动防御）
            boolean flag = handlePetDefensiveEnchants(entity, source, amount, attacker);

            // 4. 宠物作为攻击者（主动攻击特效）
            handlePetOffensiveEnchants(entity, amount, attacker, flag);

            return !flag;
        });
        ServerLivingEntityEvents.AFTER_DAMAGE.register(EntityHurtHandler::onEntityHurt);
    }

    /**
     * 处理友好伤害：如果触发了友好伤害保护，取消事件并清除仇恨
     */
    private static boolean handleFriendlyFire(LivingEntity entity, DamageSource source, float amount) {
        if (FriendlyFireCommon.preventAttack(entity, source, amount)) {
            entity.setLastHurtByMob(null);
            if (source.getEntity() instanceof LivingEntity trueSource) {
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
    private static boolean handlePetDefensiveEnchants(LivingEntity victim, DamageSource source, float amount, LivingEntity attacker) {
        if (!TameableUtils.isTamed(victim) || source.is(PHDamageTypes.SIPHON)) {
            return false;
        }

        boolean flag = false;

        // BLAZING_PROTECTION: 烈焰保护
        if (TameableUtils.hasEnchant(victim, ModEnchantments.BLAZING_PROTECTION)) {
            int bars = TameableUtils.getBlazingProtectionBars(victim);
            if (bars > 0) {
                if (!TameableUtils.hasSameOwnerAs(attacker, victim)) {
                    attacker.igniteForTicks(20 * (5 + victim.getRandom().nextInt(3)));
                    attacker.knockback(0.4, victim.getX() - attacker.getX(), victim.getZ() - attacker.getZ());
                }
                flag = true;
                for (int i = 0; i < 3 + victim.getRandom().nextInt(3); i++) {
                    attacker.level().addParticle(ParticleTypes.FLAME, victim.getRandomX(0.8F), victim.getRandomY(), victim.getRandomZ(0.8F), 0.0F, 0.0F, 0.0F);
                }
                victim.playSound(PHSoundRegistry.BLAZING_PROTECTION, 1, victim.getVoicePitch());
                TameableUtils.setBlazingProtectionBars(victim, bars - 1);
                TameableUtils.setBlazingProtectionCooldown(victim, 600);
            }
        }

        // HEALTH_SIPHON: 生命虹吸 (将伤害转移给主人)
        if (!flag && TameableUtils.hasEnchant(victim, ModEnchantments.HEALTH_SIPHON)) {
            Entity owner = TameableUtils.getOwnerOf(victim);
            if (owner != null && owner.isAlive() && owner.distanceTo(victim) < 100 && owner != victim) {
                owner.hurt(source, amount);
                flag = true;
                victim.hurt(PHDamageTypes.causeSiphonDamage(owner.level().registryAccess()), 0.0F);
            }
        }

        // TOTAL_RECALL: 紧急收回
        if (!flag && TameableUtils.hasEnchant(victim, ModEnchantments.TOTAL_RECALL) && victim.getHealth() - amount <= 2.0D) {
            UUID ownerUUID = TameableUtils.getOwnerUUIDOf(victim);
            if (ownerUUID != null) {
                if (victim instanceof Mob mob) mob.playAmbientSound();
                victim.playSound(SoundEvents.ENDER_CHEST_CLOSE, 1.0F, 1.5F);

                RecallBallEntity recallBall = PHEntityRegistry.RECALL_BALL.create(victim.level());
                recallBall.setOwnerUUID(ownerUUID);
                CompoundTag tag = new CompoundTag();
                victim.addAdditionalSaveData(tag);
                recallBall.setContainedData(tag);
                recallBall.setContainedEntityType(BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString());
                recallBall.setPos(victim.getX(), Math.max(victim.getY(), victim.level().getMinBuildHeight() + 1), victim.getZ());
                recallBall.setYRot(victim.getYRot());
                recallBall.setInvulnerable(true);

                victim.stopRiding();
                if (victim.level().addFreshEntity(recallBall)) {
                    victim.discard();
                }
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 处理宠物作为攻击方的附魔逻辑
     */
    private static void handlePetOffensiveEnchants(LivingEntity victim, float amount, LivingEntity attacker, boolean alreadyTriggeredDefensive) {
        if (!TameableUtils.isTamed(attacker)) return;

        // 1. 链锁闪电
        int lightningLevel = TameableUtils.getEnchantLevel(attacker, ModEnchantments.CHAIN_LIGHTNING);
        if (lightningLevel > 0) {
            ChainLightningEntity lightning = PHEntityRegistry.CHAIN_LIGHTNING.create(victim.level());
            lightning.setCreatorEntityID(attacker.getId());
            lightning.setFromEntityID(attacker.getId());
            lightning.setToEntityID(victim.getId());
            lightning.copyPosition(victim);
            lightning.setChainsLeft(3 + lightningLevel * 3);
            victim.level().addFreshEntity(lightning);
            victim.playSound(PHSoundRegistry.CHAIN_LIGHTNING, 1F, 1F);
        }

        // 2. 吸血
        int vampireLevel = TameableUtils.getEnchantLevel(attacker, ModEnchantments.VAMPIRE);
        if (vampireLevel > 0 && attacker.getHealth() < attacker.getMaxHealth()) {
            float healAmount = Mth.clamp(amount * vampireLevel * 0.5F, 1F, 10F);
            attacker.heal(healAmount);
            if (victim.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 5 + victim.getRandom().nextInt(3); i++) {
                    double f1 = victim.getRandomX(0.7F);
                    double f2 = victim.getY(0.4F + victim.getRandom().nextFloat() * 0.2F);
                    double f3 = victim.getRandomZ(0.7F);
                    Vec3 motion = attacker.getEyePosition().subtract(f1, f2, f3).normalize().scale(0.2F);
                    serverLevel.sendParticles(PHParticleRegistry.VAMPIRE, f1, f2, f3, 1, motion.x, motion.y, motion.z, 0.2F);
                }
            }
        }

        // 3. 泡泡控制
        int bubblingLevel = TameableUtils.getEnchantLevel(attacker, ModEnchantments.BUBBLING);
        if (bubblingLevel > 0) {
            if (!(victim.getRootVehicle() instanceof GiantBubbleEntity) && (victim.onGround() || victim.isInWaterOrBubble() || victim.isInLava())) {
                GiantBubbleEntity bubble = PHEntityRegistry.GIANT_BUBBLE.create(victim.level());
                bubble.copyPosition(victim);
                victim.startRiding(bubble, true);
                bubble.setpopsIn(bubblingLevel * 40 + 40);
                victim.level().addFreshEntity(bubble);
                victim.playSound(PHSoundRegistry.GIANT_BUBBLE_INFLATE, 1F, 1F);
            }
        }

        // 4. 霜冻
        if (TameableUtils.hasEnchant(attacker, ModEnchantments.FROST_FANG)) {
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
        if (TameableUtils.hasEnchant(attacker, ModEnchantments.WARPING_BITE)) {
            for (int i = 0; i < 16; ++i) {
                double d3 = victim.getX() + (attacker.getRandom().nextDouble() - 0.5D) * 16.0D;
                double d4 = Mth.clamp(victim.getY() + (double) (attacker.getRandom().nextInt(16) - 8), victim.level().getMinBuildHeight(), victim.level().getMinBuildHeight() + ((ServerLevel) victim.level()).getLogicalHeight() - 1);
                double d5 = victim.getZ() + (attacker.getRandom().nextDouble() - 0.5D) * 16.0D;
                if (victim.randomTeleport(d3, d4, d5, true)) {
                    SoundEvent soundevent = victim instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                    victim.playSound(soundevent, 1.0F, 1.0F);
                    break;
                }
            }
        }

        // 6. 治疗光环冲能
        if (!alreadyTriggeredDefensive) {
            List<LivingEntity> nearbyHealers = TameableUtils.getNearbyHealers(victim);
            if (!nearbyHealers.isEmpty()) {
                for (LivingEntity healer : nearbyHealers) {
                    TameableUtils.setHealingAuraImpulse(healer, true);
                }
            }
        }
    }

    /**
     * LivingDamageEvent.Pre 的替代（VIOLENT 附魔），由 LivingEntityDamageMixin 调用。
     *
     * @return 修改后的伤害值
     */
    public static float onDamagePre(LivingEntity monster, DamageSource source, float damage) {
        var maid = source.getEntity();
        var level = monster.level();
        var chance = level.getRandom().nextFloat();

        if (maid instanceof LivingEntity maidLiving && TameableUtils.hasEnchant(maidLiving, ModEnchantments.VIOLENT)) {
            if (chance < 0.01) {
                monster.die(monster.damageSources().mobAttack(maidLiving));
            } else if (chance < 0.11) {
                monster.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 5));
            } else if (chance < 0.21) {
                var paralysicLevel = 1;
                monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, paralysicLevel * 20, 100, false, false));
                monster.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, paralysicLevel * 20, 100, false, false));
                monster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, paralysicLevel * 20, 100, false, false));
            } else if (chance < 0.41) {
                damage = damage + 3;
            } else if (chance < 0.60) {
                monster.igniteForTicks(20 * 5);
            } else if (chance < 0.7) {
                if (monster.getHealth() > 30) {
                    damage = monster.getHealth() / 3;
                } else {
                    damage = damage + 5;
                }

            } else if (chance < 0.8) {
                monster.addEffect(new MobEffectInstance(ModEffects.DRUNK, 20 * 5));
            } else {
                monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1 * 20, 50, false, false));
                monster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1 * 20, 50, false, false));
            }
        }
        return damage;
    }

    public static void onEntityHurt(LivingEntity hurtEntity, DamageSource source, float originalDamage, float newDamage, boolean blocked) {
        var attacker = source.getEntity();
        if (TameableUtils.hasEnchant(hurtEntity, ModEnchantments.CHAOS) && attacker instanceof LivingEntity) {

            ((LivingEntity) attacker).addEffect(new MobEffectInstance(ModEffects.DRUNK, 120, 1));
        }
        var shareEnchantLevel = TameableUtils.getEnchantLevel(hurtEntity, ModEnchantments.SHARE);
        if (shareEnchantLevel > 0 && attacker instanceof LivingEntity attackerLiving) {
            var monsterEntities = TameableUtils.getNearbyMobs(hurtEntity, 20).stream().filter(i -> i instanceof Enemy).collect(
                    Collectors.toSet());

            if (monsterEntities.size() > 1) {
                monsterEntities.forEach(i -> {
                    i.hurt(source, (float) (originalDamage * 0.3));
                });

            }
        }
        var paralysicLevel = TameableUtils.getEnchantLevel(hurtEntity, ModEnchantments.PARALYSIS);
        if (paralysicLevel > 0 && attacker instanceof LivingEntity attackerLiving) {

            attackerLiving.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, paralysicLevel * 20, 100, false, false));
            attackerLiving.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, paralysicLevel * 20, 100, false, false));
            attackerLiving.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, paralysicLevel * 20, 100, false, false));

        }

        if (FriendlyFireCommon.preventAttack(hurtEntity, source, newDamage)) {


            hurtEntity.setLastHurtByMob(null);

            if (source.getEntity() instanceof LivingEntity trueSource) {

                trueSource.setLastHurtByMob(null);
            }
        }

    }

}
