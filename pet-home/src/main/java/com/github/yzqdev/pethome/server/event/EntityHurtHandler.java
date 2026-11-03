package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.ChainLightningEntity;
import com.github.yzqdev.pethome.server.entity.GiantBubbleEntity;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.RecallBallEntity;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import com.github.yzqdev.pethome.server.misc.PHDamageTypes;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.server.misc.PHEffects;
import com.github.yzqdev.pethome.util.FriendlyFireCommon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * 伤害事件处理核心（替代原 Forge 的 LivingAttackEvent / LivingHurtEvent / LivingDamageEvent 订阅）。
 * 由 {@code mixin/LivingEntityDamageMixin} 在 {@code LivingEntity#hurt} / {@code actuallyHurt} 处调入，
 * Mixin 只做转发与取消，全部决策逻辑集中在本类普通静态方法；双端都会进入，各分支内部自判
 * {@code level().isClientSide()}。
 */
public class EntityHurtHandler {

    /**
     * 轻量替代 Forge LivingAttackEvent 的上下文对象。
     */
    public static class AttackEvent {
        private final LivingEntity entity;
        private final DamageSource source;
        private float amount;
        private boolean canceled = false;

        public AttackEvent(LivingEntity entity, DamageSource source, float amount) {
            this.entity = entity;
            this.source = source;
            this.amount = amount;
        }

        public LivingEntity getEntity() {
            return entity;
        }

        public DamageSource getSource() {
            return source;
        }

        public float getAmount() {
            return amount;
        }

        public void setAmount(float amount) {
            this.amount = amount;
        }

        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }

        public boolean isCanceled() {
            return canceled;
        }
    }

    /**
     * 受击总入口（{@code LivingEntity#hurt} HEAD，可取消）：依次执行友军伤害保护、
     * 宠物受击防御附魔（无敌帧 / 烈焰保护 / 两栖溺水与脱水豁免 / 虚空云坠落豁免 / 生命虹吸 /
     * 紧急收回）、攻击方宠物的攻击附魔（链锁闪电 / 霜冻之牙 / 泡泡束缚 / 吸血 / 传送撕咬），
     * 最后通知附近治疗光环宠物进入充能。
     */
    public static void onLivingAttack(AttackEvent event) {
        //friendly fire
        var world = event.getEntity().level();
        if (!world.isClientSide() && FriendlyFireCommon.preventAttack(event.getEntity(), event.getSource(), event.getAmount())) {
            event.setCanceled(true);
            event.getEntity().setLastHurtByMob(null);
            if (event.getSource().getEntity() instanceof LivingEntity trueSource) {
                trueSource.setLastHurtByMob(null);
            }
        }
        if (TameableUtils.isTamed(event.getEntity()) && !event.getSource().is(PHDamageTypes.SIPHON)) {
            boolean flag = false;
            flag = tryImmunityFrame(event, flag);
            flag = tryBlazingProtection(event, flag);
            if (!flag && (event.getSource().is(DamageTypes.DROWN) || event.getSource().is(DamageTypes.DRY_OUT)) && TameableUtils.hasEnchant(event.getEntity(), PHEnchantmentRegistry.AMPHIBIOUS)) {
                event.setCanceled(true);
                flag = true;
            }
            if (!flag && (event.getSource().is(DamageTypes.FALL) || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) && TameableUtils.hasEnchant(event.getEntity(), PHEnchantmentRegistry.VOID_CLOUD)) {
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

    /** 无敌帧：首次受击进入 20+20×等级 tick 免疫期，免疫期内再次受击直接取消。 */
    private static boolean tryImmunityFrame(AttackEvent event, boolean flag) {
        if (TameableUtils.hasEnchant(event.getEntity(), PHEnchantmentRegistry.IMMUNITY_FRAME)) {
            int level = TameableUtils.getEnchantLevel(event.getEntity(), PHEnchantmentRegistry.IMMUNITY_FRAME);
            if (TameableUtils.getImmuneTime(event.getEntity()) <= 0) {
                TameableUtils.setImmuneTime(event.getEntity(), 20 + level * 20);
            } else {
                flag = true;
                event.setCanceled(true);
            }
        }
        return flag;
    }

    /** 烈焰保护：消耗 1 条蓄条抵消本次伤害，点燃并击退非同主人的攻击者（服务端音效/粒子）。 */
    private static boolean tryBlazingProtection(AttackEvent event, boolean flag) {
        if (TameableUtils.hasEnchant(event.getEntity(), PHEnchantmentRegistry.BLAZING_PROTECTION)) {
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
                event.getEntity().playSound(PHSoundRegistry.BLAZING_PROTECTION, 1, event.getEntity().getVoicePitch());
                TameableUtils.setBlazingProtectionBars(event.getEntity(), bars - 1);
                TameableUtils.setBlazingProtectionCooldown(event.getEntity(), 600);
            }
        }
        return flag;
    }

    /** 生命虹吸：把本次伤害原样转移给 100 格内的主人（0 伤害 SIPHON 占位打在宠物身上取消原伤害）。 */
    private static boolean tryHealthSiphon(AttackEvent event, boolean flag) {
        if (flag) {
            return flag;
        }
        if (TameableUtils.hasEnchant(event.getEntity(), PHEnchantmentRegistry.HEALTH_SIPHON)) {
            Entity owner = TameableUtils.getOwnerOf(event.getEntity());
            if (owner != null && owner.isAlive() && owner.distanceTo(event.getEntity()) < 100 && owner != event.getEntity()) {
                owner.hurt(event.getSource(), event.getAmount());
                event.setCanceled(true);
                flag = true;
                event.getEntity().hurt(PHDamageTypes.causeSiphonDamage(owner.level().registryAccess()), 0.0F);
            }
        }
        return flag;
    }

    /** 紧急收回：濒死（本次伤害会打到 2 血以下）时把宠物完整 NBT 收入 {@link RecallBallEntity} 并 discard 原实体，等待落地重建。 */
    private static boolean tryTotalRecall(AttackEvent event, boolean flag) {
        if (flag) {
            return flag;
        }
        if (TameableUtils.hasEnchant(event.getEntity(), PHEnchantmentRegistry.TOTAL_RECALL) && event.getEntity().getHealth() - event.getAmount() <= 2.0D) {
            UUID owner = TameableUtils.getOwnerUUIDOf(event.getEntity());
            if (owner != null) {
                if (event.getEntity() instanceof Mob mob) {
                    mob.playAmbientSound();
                }
                event.getEntity().playSound(SoundEvents.ENDER_CHEST_CLOSE, 1.0F, 1.5F);
                RecallBallEntity recallBall = PHEntityRegistry.RECALL_BALL.create(event.getEntity().level());
                recallBall.setOwnerUUID(owner);
                CompoundTag tag = new CompoundTag();
                event.getEntity().addAdditionalSaveData(tag);
                recallBall.setContainedData(tag);
                recallBall.setContainedEntityType(BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString());
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

    /** 攻击方为宠物时的攻击附魔分支（链锁闪电 / 霜冻之牙 / 泡泡束缚 / 吸血 / 传送撕咬）。 */
    private static void applyAttackerCombatEnchants(LivingEntity attacker, AttackEvent event) {
        LivingEntity target = event.getEntity();
        int lightningLevel = TameableUtils.getEnchantLevel(attacker, PHEnchantmentRegistry.CHAIN_LIGHTNING);
        int bubblingLevel = TameableUtils.getEnchantLevel(attacker, PHEnchantmentRegistry.BUBBLING);
        int vampireLevel = TameableUtils.getEnchantLevel(attacker, PHEnchantmentRegistry.VAMPIRE);

        if (lightningLevel > 0) {
            spawnChainLightning(attacker, target, lightningLevel);
        }
        if (TameableUtils.hasEnchant(attacker, PHEnchantmentRegistry.FROST_FANG)) {
            applyFrostFang(attacker, target);
        }
        if (bubblingLevel > 0) {
            applyBubbling(attacker, target, bubblingLevel);
        }
        if (vampireLevel > 0) {
            applyVampire(attacker, target, vampireLevel, event);
        }
        if (!target.level().isClientSide() && TameableUtils.hasEnchant(attacker, PHEnchantmentRegistry.WARPING_BITE)) {
            applyWarpingBite(attacker, target);
        }
    }

    private static void spawnChainLightning(LivingEntity attacker, LivingEntity target, int lightningLevel) {
        ChainLightningEntity lightning = PHEntityRegistry.CHAIN_LIGHTNING.create(target.level());
        lightning.setCreatorEntityID(attacker.getId());
        lightning.setFromEntityID(attacker.getId());
        lightning.setToEntityID(target.getId());
        lightning.copyPosition(target);
        lightning.setChainsLeft(3 + lightningLevel * 3);
        target.level().addFreshEntity(lightning);
        target.playSound(PHSoundRegistry.CHAIN_LIGHTNING, 1F, 1F);
    }

    private static void applyFrostFang(LivingEntity attacker, LivingEntity target) {
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

    private static void applyBubbling(LivingEntity attacker, LivingEntity target, int bubblingLevel) {
        if (!(target.getRootVehicle() instanceof GiantBubbleEntity) && (target.onGround() || target.isInWaterOrBubble() || target.isInLava())) {
            GiantBubbleEntity bubble = PHEntityRegistry.GIANT_BUBBLE.create(target.level());
            bubble.copyPosition(target);
            target.startRiding(bubble, true);
            bubble.setpopsIn(bubblingLevel * 40 + 40);
            target.level().addFreshEntity(bubble);
            target.playSound(PHSoundRegistry.GIANT_BUBBLE_INFLATE, 1F, 1F);
        }
    }

    private static void applyVampire(LivingEntity attacker, LivingEntity target, int vampireLevel, AttackEvent event) {
        if (attacker.getHealth() < attacker.getMaxHealth()) {
            float f = Mth.clamp(event.getAmount() * vampireLevel * 0.5F, 1F, 10F);
            attacker.heal(f);
            if (target.level() instanceof ServerLevel) {
                for (int i = 0; i < 5 + target.getRandom().nextInt(3); i++) {
                    double f1 = target.getRandomX(0.7F);
                    double f2 = target.getY(0.4F + target.getRandom().nextFloat() * 0.2F);
                    double f3 = target.getRandomZ(0.7F);
                    Vec3 motion = attacker.getEyePosition().subtract(f1, f2, f3).normalize().scale(0.2F);
                    ((ServerLevel) target.level()).sendParticles(PHParticleRegistry.VAMPIRE, f1, f2, f3, 1, motion.x, motion.y, motion.z, 0.2F);
                }
            }
        }
    }

    private static void applyWarpingBite(LivingEntity attacker, LivingEntity target) {
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

    // ===== 以下 4 个自 1.21 移植（violent / chaos / share / paralysis 附魔） =====

    /**
     * Violent（暴力）：宠物攻击时按概率触发随机强化效果，并可能修改本次伤害数值。
     *
     * <p>原 Forge 版订阅 {@code LivingHurtEvent}（护甲减免前、可改数值）；Fabric 侧由
     * {@code LivingEntityDamageMixin} 在 {@code LivingEntity#hurt} 的伤害参数上用
     * {@code @ModifyVariable} 调用本方法，语义等价。</p>
     *
     * @return 修正后的伤害数值
     */
    public static float onLivingHurt(LivingEntity victim, DamageSource source, float amount) {
        if (victim == null || victim.level().isClientSide()) {
            return amount;
        }
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker == victim) {
            return amount;
        }
        if (!TameableUtils.hasEnchant(attacker, PHEnchantmentRegistry.VIOLENT)) {
            return amount;
        }
        // 同一主人的两只宠物互殴会被友伤保护取消（Forge 版此时不会触发 LivingHurtEvent），
        // 这里提前跳过，避免只改数值的事件顺序差异导致误触发副作用。
        if (TameableUtils.hasSameOwnerAs(attacker, victim)) {
            return amount;
        }
        LivingEntity monster = victim;
        float chance = monster.level().getRandom().nextFloat();
        if (chance < 0.01) {
            monster.die(monster.damageSources().mobAttack(attacker));
        } else if (chance < 0.11) {
            monster.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 5));
        } else if (chance < 0.21) {
            int paralyticLevel = 1;
            monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, paralyticLevel * 20, 100, false, false));
            monster.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, paralyticLevel * 20, 100, false, false));
            monster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, paralyticLevel * 20, 100, false, false));
        } else if (chance < 0.41) {
            amount += 3;
        } else if (chance < 0.60) {
            monster.setSecondsOnFire(5);
        } else if (chance < 0.7) {
            if (monster.getHealth() > 30) {
                amount = monster.getHealth() / 3;
            } else {
                amount += 5;
            }
        } else if (chance < 0.8) {
            monster.addEffect(new MobEffectInstance(PHEffects.DRUNK, 20 * 5));
        } else {
            monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 50, false, false));
            monster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 50, false, false));
        }
        return amount;
    }

    /**
     * Chaos / Share / Paralysis：宠物受击时的反应。
     *
     * <p>原 Forge 版订阅 {@code LivingDamageEvent}（护甲减免后）；Fabric 侧由
     * {@code LivingEntityDamageMixin} 在 {@code LivingEntity#actuallyHurt} 头部调用本方法，语义等价。</p>
     *
     * @param amount 护甲/吸收减免后的实际伤害
     */
    public static void onLivingDamage(LivingEntity hurtEntity, DamageSource source, float amount) {
        if (hurtEntity.level().isClientSide()) {
            return;
        }
        Entity attacker = source.getEntity();

        if (TameableUtils.hasEnchant(hurtEntity, PHEnchantmentRegistry.CHAOS) && attacker instanceof LivingEntity livingAttacker) {
            livingAttacker.addEffect(new MobEffectInstance(PHEffects.DRUNK, 120, 1));
        }

        int shareLevel = TameableUtils.getEnchantLevel(hurtEntity, PHEnchantmentRegistry.SHARE);
        if (shareLevel > 0 && attacker instanceof LivingEntity) {
            Set<LivingEntity> monsterEntities = TameableUtils.getNearbyMobs(hurtEntity, 20).stream()
                    .filter(i -> i instanceof Enemy).collect(Collectors.toSet());
            if (monsterEntities.size() > 1) {
                float originalDamage = amount;
                monsterEntities.forEach(i -> i.hurt(source, (float) (originalDamage * 0.3)));
            }
        }

        int paralysisLevel = TameableUtils.getEnchantLevel(hurtEntity, PHEnchantmentRegistry.PARALYSIS);
        if (paralysisLevel > 0 && attacker instanceof LivingEntity livingAttacker) {
            livingAttacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, paralysisLevel * 20, 100, false, false));
            livingAttacker.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, paralysisLevel * 20, 100, false, false));
            livingAttacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, paralysisLevel * 20, 100, false, false));
        }
    }
}
