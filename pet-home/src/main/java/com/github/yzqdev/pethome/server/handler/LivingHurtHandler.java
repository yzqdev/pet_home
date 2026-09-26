package com.github.yzqdev.pethome.server.handler;

import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.ChainLightningEntity;
import com.github.yzqdev.pethome.server.entity.GiantBubbleEntity;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.RecallBallEntity;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import com.github.yzqdev.pethome.server.misc.DIDamageTypes;
import com.github.yzqdev.pethome.server.misc.DIParticleRegistry;
import com.github.yzqdev.pethome.server.misc.DISoundRegistry;
import com.github.yzqdev.pethome.server.misc.ModEffects;
import com.github.yzqdev.pethome.util.FriendlyFireCommon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 从 CommonProxy.onLivingHurt 拆分而来：宠物受击时的保护附魔与攻击方战斗附魔。
 */
public class LivingHurtHandler {

    /**
     * event.getEntity()是被攻击的
     */
    @SubscribeEvent
    public void onLivingHurt(LivingAttackEvent event) {
        //friendly fire
        var world = event.getEntity().level();
        if (!world.isClientSide() && FriendlyFireCommon.preventAttack(event.getEntity(), event.getSource(), event.getAmount())) {
            event.setCanceled(true);
            event.getEntity().setLastHurtByMob(null);
            if (event.getSource().getEntity() instanceof LivingEntity trueSource) {
                trueSource.setLastHurtByMob(null);
            }
        }
        if (TameableUtils.isTamed(event.getEntity()) && !event.getSource().is(DIDamageTypes.SIPHON)) {
            boolean flag = false;
            flag = tryImmunityFrame(event, flag);
            flag = tryBlazingProtection(event, flag);
            if (!flag && (event.getSource().is(DamageTypes.DROWN) || event.getSource().is(DamageTypes.DRY_OUT)) && TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.AMPHIBIOUS)) {
                event.setCanceled(true);
                flag = true;
            }
            if (!flag && (event.getSource().is(DamageTypes.FALL) || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) && TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.VOID_CLOUD)) {
                event.setCanceled(true);
                flag = true;
            }
            flag = tryHealthSiphon(event, flag);
            flag = tryTotalRecall(event, flag);
        }
        if (event.getSource().getEntity() != null && TameableUtils.isTamed(event.getSource().getEntity())) {
            LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
            applyAttackerCombatEnchants(attacker, event);
        }
        if (!event.isCanceled()) {
            List<LivingEntity> nearbyHealers = TameableUtils.getNearbyHealers(event.getEntity());
            if (!nearbyHealers.isEmpty()) {
                for (LivingEntity healer : nearbyHealers) {
                    TameableUtils.setHealingAuraImpulse(healer, true);
                }
            }
        }
    }

    private boolean tryImmunityFrame(LivingAttackEvent event, boolean flag) {
        if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.IMMUNITY_FRAME)) {
            int level = TameableUtils.getEnchantLevel(event.getEntity(), DIEnchantmentRegistry.IMMUNITY_FRAME);
            if (TameableUtils.getImmuneTime(event.getEntity()) <= 0) {
                TameableUtils.setImmuneTime(event.getEntity(), 20 + level * 20);
            } else {
                flag = true;
                event.setCanceled(true);
            }
        }
        return flag;
    }

    private boolean tryBlazingProtection(LivingAttackEvent event, boolean flag) {
        if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.BLAZING_PROTECTION)) {
            int bars = TameableUtils.getBlazingProtectionBars(event.getEntity());
            if (bars > 0) {
                Entity attacker = event.getSource().getEntity();
                if (attacker instanceof LivingEntity livingAttacker && !TameableUtils.hasSameOwnerAs(livingAttacker, event.getEntity())) {
                    livingAttacker.setSecondsOnFire(5 + event.getEntity().getRandom().nextInt(3));
                    livingAttacker.knockback(0.4, event.getEntity().getX() - livingAttacker.getX(), event.getEntity().getZ() - livingAttacker.getZ());
                }
                event.setCanceled(true);
                flag = true;
                if (attacker != null) {
                    for (int i = 0; i < 3 + event.getEntity().getRandom().nextInt(3); i++) {
                        attacker.level().addParticle(ParticleTypes.FLAME, event.getEntity().getRandomX(0.8F), event.getEntity().getRandomY(), event.getEntity().getRandomZ(0.8F), 0.0F, 0.0F, 0.0F);
                    }
                }
                event.getEntity().playSound(DISoundRegistry.BLAZING_PROTECTION.get(), 1, event.getEntity().getVoicePitch());
                TameableUtils.setBlazingProtectionBars(event.getEntity(), bars - 1);
                TameableUtils.setBlazingProtectionCooldown(event.getEntity(), 600);
            }
        }
        return flag;
    }

    private boolean tryHealthSiphon(LivingAttackEvent event, boolean flag) {
        if (flag) {
            return flag;
        }
        if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.HEALTH_SIPHON)) {
            Entity owner = TameableUtils.getOwnerOf(event.getEntity());
            if (owner != null && owner.isAlive() && owner.distanceTo(event.getEntity()) < 100 && owner != event.getEntity()) {
                owner.hurt(event.getSource(), event.getAmount());
                event.setCanceled(true);
                flag = true;
                event.getEntity().hurt(DIDamageTypes.causeSiphonDamage(owner.level().registryAccess()), 0.0F);
            }
        }
        return flag;
    }

    private boolean tryTotalRecall(LivingAttackEvent event, boolean flag) {
        if (flag) {
            return flag;
        }
        if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.TOTAL_RECALL) && event.getEntity().getHealth() - event.getAmount() <= 2.0D && !TameableUtils.isZombiePet(event.getEntity())) {
            UUID owner = TameableUtils.getOwnerUUIDOf(event.getEntity());
            if (owner != null) {
                if (event.getEntity() instanceof Mob mob) {
                    mob.playAmbientSound();
                }
                event.getEntity().playSound(SoundEvents.ENDER_CHEST_CLOSE, 1.0F, 1.5F);
                RecallBallEntity recallBall = PHEntityRegistry.RECALL_BALL.get().create(event.getEntity().level());
                recallBall.setOwnerUUID(owner);
                CompoundTag tag = new CompoundTag();
                event.getEntity().addAdditionalSaveData(tag);
                recallBall.setContainedData(tag);
                recallBall.setContainedEntityType(ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType()).toString());
                recallBall.setPos(event.getEntity().getX(), Math.max(event.getEntity().getY(), event.getEntity().level().getMinBuildHeight() + 1), event.getEntity().getZ());
                recallBall.setYRot(event.getEntity().getYRot());
                recallBall.setInvulnerable(true);
                event.getEntity().stopRiding();
                if (event.getEntity().level().addFreshEntity(recallBall)) {
                    event.getEntity().discard();
                }
                flag = true;
                event.setCanceled(true);
            }
        }
        return flag;
    }

    private void applyAttackerCombatEnchants(LivingEntity attacker, LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        int lightningLevel = TameableUtils.getEnchantLevel(attacker, DIEnchantmentRegistry.CHAIN_LIGHTNING);
        int bubblingLevel = TameableUtils.getEnchantLevel(attacker, DIEnchantmentRegistry.BUBBLING);
        int vampireLevel = TameableUtils.getEnchantLevel(attacker, DIEnchantmentRegistry.VAMPIRE);

        if (lightningLevel > 0) {
            spawnChainLightning(attacker, target, lightningLevel);
        }
        if (TameableUtils.hasEnchant(attacker, DIEnchantmentRegistry.FROST_FANG)) {
            applyFrostFang(attacker, target);
        }
        if (bubblingLevel > 0) {
            applyBubbling(attacker, target, bubblingLevel);
        }
        if (vampireLevel > 0) {
            applyVampire(attacker, target, vampireLevel, event);
        }
        if (!target.level().isClientSide() && TameableUtils.hasEnchant(attacker, DIEnchantmentRegistry.WARPING_BITE)) {
            applyWarpingBite(attacker, target);
        }
    }

    private void spawnChainLightning(LivingEntity attacker, LivingEntity target, int lightningLevel) {
        ChainLightningEntity lightning = PHEntityRegistry.CHAIN_LIGHTNING.get().create(target.level());
        lightning.setCreatorEntityID(attacker.getId());
        lightning.setFromEntityID(attacker.getId());
        lightning.setToEntityID(target.getId());
        lightning.copyPosition(target);
        lightning.setChainsLeft(3 + lightningLevel * 3);
        target.level().addFreshEntity(lightning);
        target.playSound(DISoundRegistry.CHAIN_LIGHTNING.get(), 1F, 1F);
    }

    private void applyFrostFang(LivingEntity attacker, LivingEntity target) {
        target.setTicksFrozen(target.getTicksRequiredToFreeze() + 200);
        Vec3 vec3 = target.getEyePosition().subtract(attacker.getEyePosition()).normalize().scale(attacker.getBbWidth() + 0.5F);
        Vec3 vec32 = attacker.getEyePosition().add(vec3);
        for (int i = 0; i < 3 + attacker.getRandom().nextInt(3); i++) {
            float f1 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
            float f2 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
            float f3 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
            attacker.level().addParticle(ParticleTypes.SNOWFLAKE, vec32.x + f1, vec32.y + f2, vec32.z + f3, 0.0F, 0.0F, 0.0F);
        }
        TameableUtils.setFrozenTimeTag(target, 60);
    }

    private void applyBubbling(LivingEntity attacker, LivingEntity target, int bubblingLevel) {
        if (!(target.getRootVehicle() instanceof GiantBubbleEntity) && (target.onGround() || target.isInWaterOrBubble() || target.isInLava())) {
            GiantBubbleEntity bubble = PHEntityRegistry.GIANT_BUBBLE.get().create(target.level());
            bubble.copyPosition(target);
            target.startRiding(bubble, true);
            bubble.setpopsIn(bubblingLevel * 40 + 40);
            target.level().addFreshEntity(bubble);
            target.playSound(DISoundRegistry.GIANT_BUBBLE_INFLATE.get(), 1F, 1F);
        }
    }

    private void applyVampire(LivingEntity attacker, LivingEntity target, int vampireLevel, LivingAttackEvent event) {
        if (attacker.getHealth() < attacker.getMaxHealth()) {
            float f = Mth.clamp(event.getAmount() * vampireLevel * 0.5F, 1F, 10F);
            attacker.heal(f);
            if (target.level() instanceof ServerLevel) {
                for (int i = 0; i < 5 + target.getRandom().nextInt(3); i++) {
                    double f1 = target.getRandomX(0.7F);
                    double f2 = target.getY(0.4F + target.getRandom().nextFloat() * 0.2F);
                    double f3 = target.getRandomZ(0.7F);
                    Vec3 motion = attacker.getEyePosition().subtract(f1, f2, f3).normalize().scale(0.2F);
                    ((ServerLevel) target.level()).sendParticles(DIParticleRegistry.VAMPIRE.get(), f1, f2, f3, 1, motion.x, motion.y, motion.z, 0.2F);
                }
            }
        }
    }

    private void applyWarpingBite(LivingEntity attacker, LivingEntity target) {
        for (int i = 0; i < 16; ++i) {
            double d3 = target.getX() + (attacker.getRandom().nextDouble() - 0.5D) * 16.0D;
            double d4 = Mth.clamp(target.getY() + (double) (attacker.getRandom().nextInt(16) - 8), target.level().getMinBuildHeight(), target.level().getMinBuildHeight() + ((ServerLevel) target.level()).getLogicalHeight() - 1);
            double d5 = target.getZ() + (attacker.getRandom().nextDouble() - 0.5D) * 16.0D;
            if (target.randomTeleport(d3, d4, d5, true)) {
                SoundEvent soundevent = target instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                target.playSound(soundevent, 1.0F, 1.0F);
                break;
            }
        }
    }

    // ===== 以下 2 个自 1.21 移植（violent / chaos / share / paralysis 附魔） =====

    /**
     * Violent（暴力）：宠物攻击时概率触发随机强化效果（对齐 1.21 EntityHurtHandler 的
     * LivingHurtEvent 逻辑，1.20 用 event.setAmount 修改伤害）。
     */
    @SubscribeEvent
    public void onLivingHurtApplyViolent(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity maidLiving)) {
            return;
        }
        if (!TameableUtils.hasEnchant(maidLiving, DIEnchantmentRegistry.VIOLENT)) {
            return;
        }
        LivingEntity monster = event.getEntity();
        var chance = monster.level().getRandom().nextFloat();
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
            event.setAmount(event.getAmount() + 3);
        } else if (chance < 0.60) {
            monster.setSecondsOnFire(5);
        } else if (chance < 0.7) {
            if (monster.getHealth() > 30) {
                event.setAmount(monster.getHealth() / 3);
            } else {
                event.setAmount(event.getAmount() + 5);
            }
        } else if (chance < 0.8) {
            monster.addEffect(new MobEffectInstance(ModEffects.DRUNK.get(), 20 * 5));
        } else {
            monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1 * 20, 50, false, false));
            monster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1 * 20, 50, false, false));
        }
    }

    /**
     * Chaos / Share / Paralysis：宠物受击时的反应（对齐 1.21 EntityHurtHandler 的
     * LivingDamageEvent.Post 逻辑，1.20 的 LivingDamageEvent 同样在护甲减免后触发）。
     */
    @SubscribeEvent
    public void onLivingDamageApplyDefensiveEnchants(LivingDamageEvent event) {
        var hurtEntity = event.getEntity();
        var attacker = event.getSource().getEntity();

        if (TameableUtils.hasEnchant(hurtEntity, DIEnchantmentRegistry.CHAOS) && attacker instanceof LivingEntity) {
            ((LivingEntity) attacker).addEffect(new MobEffectInstance(ModEffects.DRUNK.get(), 120, 1));
        }

        var shareEnchantLevel = TameableUtils.getEnchantLevel(hurtEntity, DIEnchantmentRegistry.SHARE);
        if (shareEnchantLevel > 0 && attacker instanceof LivingEntity) {
            var monsterEntities = TameableUtils.getNearbyMobs(hurtEntity, 20).stream()
                    .filter(i -> i instanceof Enemy).collect(Collectors.toSet());
            if (monsterEntities.size() > 1) {
                float originalDamage = event.getAmount();
                monsterEntities.forEach(i -> i.hurt(event.getSource(), (float) (originalDamage * 0.3)));
            }
        }

        var paralysicLevel = TameableUtils.getEnchantLevel(hurtEntity, DIEnchantmentRegistry.PARALYSIS);
        if (paralysicLevel > 0 && attacker instanceof LivingEntity attackerLiving) {
            attackerLiving.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, paralysicLevel * 20, 100, false, false));
            attackerLiving.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, paralysicLevel * 20, 100, false, false));
            attackerLiving.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, paralysicLevel * 20, 100, false, false));
        }
    }
}
