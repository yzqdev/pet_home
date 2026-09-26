package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.ClientGameEvents;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.FollowingJukeboxEntity;
import com.github.yzqdev.pethome.server.entity.GiantBubbleEntity;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.PsychicWallEntity;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import com.github.yzqdev.pethome.server.misc.DIParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import com.github.yzqdev.pethome.server.misc.DISoundRegistry;
import com.github.yzqdev.pethome.server.misc.ModEffects;
import com.github.yzqdev.pethome.util.LivingUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;
import java.util.UUID;


public class EntityTickHandler {

    private static final UUID FROST_FANG_SLOW = UUID.fromString("1eaf83ff-7207-4596-b37a-d7a07b3ec4cf");

    public static void onLivingUpdate(LivingEntity entity) {
        // 宠物罗盘：20 tick 低频档案更新（首次驯服懒初始化 PetId + 位置/名字/维度刷新，无变化不写盘）
        if (entity instanceof Mob compassMob && !entity.level().isClientSide() && compassMob.tickCount % 20 == 0) {
            PetCompassTracker.updateRecord(compassMob);
        }
        int frozenTime = TameableUtils.getFrozenTime(entity);
        // 混乱之脑：混乱的怪物转火周围其他怪物（自 1.21 移植；对所有怪物生效，不限于宠物）
        if (entity instanceof Mob chaosMob && ServerEvent.canTickCollar(entity)) {
            tickChaos(chaosMob);
        }
        if (TameableUtils.couldBeTamed(entity) && ServerEvent.canTickCollar(entity)) {
            tickImmunityFrame(entity);
            tickPoisonResistance(entity);
            tickAmphibious(entity);
            if (entity instanceof Mob mob && TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.MAGNETIC)) {
                tickMagnetic(mob);
            }
            tickShadowHands(entity);
            tickDiskJockey(entity);
            if (entity instanceof Mob mob && TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.LINKED_INVENTORY)) {
                tickLinkedInventory(mob);
            }
            tickPassiveAuras(entity);
            tickVoidCloud(entity);
            tickOreScenting(entity);
            if (entity instanceof Mob mob && TameableUtils.isZombiePet(entity) && !entity.level().isClientSide()) {
                tickZombiePetAI(mob);
            }
            if (entity instanceof Mob mob && TameableUtils.getEnchantLevel(entity, DIEnchantmentRegistry.PSYCHIC_WALL) > 0 && !entity.level().isClientSide()) {
                tickPsychicWall(mob);
            }
            if (TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.BLAZING_PROTECTION) && !entity.level().isClientSide()) {
                tickBlazingProtection(entity);
            }
            if (TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.HEALING_AURA) && !entity.level().isClientSide()) {
                tickHealingAura(entity);
            }

            if (TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.NIGHT_VISION) && !entity.level().isClientSide()) {
                tickNightVision(entity);
            }
            if (entity instanceof Mob mob && !entity.level().isClientSide()) {
                tickInsight(mob);
                tickSonicBoom(mob);
            }
        }

        if (frozenTime > 0) {
            tickFrozen(entity, frozenTime);
        }
    }

    private static void tickImmunityFrame(LivingEntity entity) {
        if (TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.IMMUNITY_FRAME) && !entity.level().isClientSide()) {
            int i = TameableUtils.getImmuneTime(entity);
            if (i > 0) {
                TameableUtils.setImmuneTime(entity, i - 1);
            }
        }
    }

    private static void tickPoisonResistance(LivingEntity entity) {
        if (entity.hasEffect(MobEffects.POISON) && TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.POISON_RESISTANCE)) {
            entity.removeEffect(MobEffects.POISON);
        }
    }

    private static void tickAmphibious(LivingEntity entity) {
        if (TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.AMPHIBIOUS)) {
            entity.setAirSupply(entity.getMaxAirSupply());
        }
    }

    private static void tickMagnetic(Mob mob) {
        Entity sucking = TameableUtils.getPetAttackTarget(mob);
        if (!mob.level().isClientSide()) {
            if (mob.getTarget() == null || !mob.getTarget().isAlive() || mob.distanceTo(mob.getTarget()) < 0.5F + mob.getBbWidth() || mob.getRootVehicle() instanceof GiantBubbleEntity) {
                if (TameableUtils.getPetAttackTargetID(mob) != -1) {
                    TameableUtils.setPetAttackTarget(mob, -1);
                }
            } else {
                TameableUtils.setPetAttackTarget(mob, mob.getTarget().getId());
            }
        } else {
            if (sucking != null) {
                double dist = mob.distanceTo(sucking);
                Vec3 start = mob.position().add(0, mob.getBbHeight() * 0.5F, 0);
                Vec3 end = sucking.position().add(0, sucking.getBbHeight() * 0.5F, 0).subtract(start);
                for (float distStep = mob.getBbWidth() + 0.8F; distStep < (int) Math.ceil(dist); distStep++) {
                    Vec3 vec3 = start.add(end.scale(distStep / dist));
                    float f1 = 0.5F * (mob.getRandom().nextFloat() - 0.5F);
                    float f2 = 0.5F * (mob.getRandom().nextFloat() - 0.5F);
                    float f3 = 0.5F * (mob.getRandom().nextFloat() - 0.5F);
                    mob.level().addParticle(DIParticleRegistry.MAGNET, vec3.x + f1, vec3.y + f2, vec3.z + f3, 0.0F, 0.0F, 0.0F);
                }
            }
        }
        if (sucking != null) {
            if (mob.tickCount % 15 == 0) {
                mob.playSound(DISoundRegistry.MAGNET_LOOP, 1F, 1F);
            }
            mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.88D, 1.0D, 0.88D));
            Vec3 move = new Vec3(mob.getX() - sucking.getX(), mob.getY() - (double) sucking.getEyeHeight() / 2.0D - sucking.getY(), mob.getZ() - sucking.getZ());
            sucking.setDeltaMovement(sucking.getDeltaMovement().add(move.normalize().scale(mob.onGround() ? 0.15D : 0.05D)));
        }
    }

    private static void tickShadowHands(LivingEntity entity) {
        int shadowHandsLevel = TameableUtils.getEnchantLevel(entity, DIEnchantmentRegistry.SHADOW_HANDS);
        if (shadowHandsLevel <= 0 || !(entity instanceof Mob)) {
            return;
        }
        Mob mob = (Mob) entity;
        ClientGameEvents.updateVisualDataForMob(entity, TameableUtils.getShadowPunchTimes(mob));
        if (!mob.level().isClientSide()) {
            var targetEntity = TameableUtils.getPetAttackTarget(mob);
            Entity punching = ((targetEntity instanceof Player) || (targetEntity instanceof TamableAnimal)) ? null : targetEntity;
            int[] punchProgress = TameableUtils.getShadowPunchTimes(mob);
            if (punching != null && punching.isAlive() && mob.hasLineOfSight(punching) && mob.distanceTo(punching) < 16) {
                tickShadowPunch(mob, punching, punchProgress, shadowHandsLevel);
            } else {
                updateShadowPunchTarget(mob, punching, punchProgress, shadowHandsLevel);
            }
        }
    }

    private static void tickShadowPunch(Mob mob, Entity punching, int[] punchProgress, int shadowHandsLevel) {
        int[] striking = TameableUtils.getShadowPunchStriking(mob);
        if (punchProgress == null || punchProgress.length < shadowHandsLevel) {
            int[] clean = new int[shadowHandsLevel];
            TameableUtils.setShadowPunchTimes(mob, clean);
            TameableUtils.setShadowPunchStriking(mob, clean);
        } else {
            int cooldown = TameableUtils.getShadowPunchCooldown(mob);
            if (cooldown <= 0) {
                boolean flag = false;
                int start = shadowHandsLevel == 1 ? 0 : mob.getRandom().nextInt(shadowHandsLevel - 1);
                for (int i = start; i < shadowHandsLevel; i++) {
                    if (striking[i] == 0) {
                        striking[i] = 1;
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    TameableUtils.setShadowPunchCooldown(mob, 5);
                }
            } else {
                TameableUtils.setShadowPunchCooldown(mob, cooldown - 1);
            }
            for (int i = 0; i < Math.min(shadowHandsLevel, Math.min(striking.length, punchProgress.length)); i++) {
                if (striking[i] != 0) {
                    if (punchProgress[i] < 10) {
                        punchProgress[i] = punchProgress[i] + 1;
                    } else {
                        punching.hurt(punching.damageSources().mobAttack(mob), Mth.clamp(shadowHandsLevel, 2, 4));
                        striking[i] = 0;
                    }
                }
                if (striking[i] == 0 && punchProgress[i] > 0) {
                    punchProgress[i] = punchProgress[i] - 1;
                }
            }
            TameableUtils.setShadowPunchStriking(mob, striking);
            TameableUtils.setShadowPunchTimes(mob, punchProgress);
        }
    }

    private static void updateShadowPunchTarget(Mob mob, Entity punching, int[] punchProgress, int shadowHandsLevel) {
        if (punching != null) {
            boolean flag = true;
            for (int i = 0; i < Math.min(shadowHandsLevel, punchProgress.length); i++) {
                if (punchProgress[i] > 0) {
                    punchProgress[i] = punchProgress[i] - 1;
                    flag = false;
                }
            }
            TameableUtils.setShadowPunchStriking(mob, new int[shadowHandsLevel]);
            TameableUtils.setShadowPunchTimes(mob, punchProgress);
            if (flag) {
                TameableUtils.setPetAttackTarget(mob, -1);
            }
        }
        Entity punchingTarget = null;
        if (mob.getTarget() != null) {
            punchingTarget = mob.getTarget();
        } else if (TameableUtils.getOwnerOf(mob) instanceof LivingEntity owner) {
            if (owner.getLastHurtByMob() != null && owner.getLastHurtByMob().isAlive() && !TameableUtils.hasSameOwnerAs(mob, owner.getLastHurtByMob())) {
                punchingTarget = owner.getLastHurtByMob();
            }
            if (owner.getLastHurtMob() != null && owner.getLastHurtMob().isAlive() && !TameableUtils.hasSameOwnerAs(mob, owner.getLastHurtMob())) {
                punchingTarget = owner.getLastHurtMob();
            }
        }
        if (punchingTarget != null && punchingTarget.isAlive()) {
            TameableUtils.setPetAttackTarget(mob, punchingTarget.getId());
        }
    }

    private static void tickDiskJockey(LivingEntity entity) {
        if (TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.DISK_JOCKEY) && !entity.level().isClientSide() && entity.tickCount % 10 == 0) {
            UUID uuid = TameableUtils.getPetJukeboxUUID(entity);
            if (uuid == null || !(((ServerLevel) entity.level()).getEntity(uuid) instanceof FollowingJukeboxEntity)) {
                FollowingJukeboxEntity follower = PHEntityRegistry.FOLLOWING_JUKEBOX.create(entity.level());
                follower.setFollowingUUID(entity.getUUID());
                follower.copyPosition(entity);
                entity.level().addFreshEntity(follower);
                TameableUtils.setPetJukeboxUUID(entity, follower.getUUID());
            }
        }
    }

    private static void tickLinkedInventory(Mob mob) {
        if (!mob.canPickUpLoot()) {
            mob.setCanPickUpLoot(true);
        }
    }

    private static void tickPassiveAuras(LivingEntity entity) {
        int shepherdLvl = TameableUtils.getEnchantLevel(entity, DIEnchantmentRegistry.SHEPHERD);
        if (shepherdLvl > 0) {
            TameableUtils.attractAnimals(entity, shepherdLvl * 3);
        }
        if (TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.INFAMY_CURSE)) {
            TameableUtils.aggroRandomMonsters(entity);
        }
        if (TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.INTIMIDATION)) {
            TameableUtils.scareRandomMonsters(entity, TameableUtils.getEnchantLevel(entity, DIEnchantmentRegistry.INTIMIDATION));
        }
        if (TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.BLIGHT_CURSE)) {
            TameableUtils.destroyRandomPlants(entity);
        }
        if (TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.REJUVENATION)) {
            TameableUtils.absorbExpOrbs(entity);
        }
        if (TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.XP_Transfer)) {
            TameableUtils.xpTransfer(entity);
        }
    }

    private static void tickVoidCloud(LivingEntity entity) {
        if (TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.VOID_CLOUD) && !entity.isInWaterOrBubble() && entity.fallDistance > 3.0F && !entity.onGround()) {
            Entity owner = TameableUtils.getOwnerOf(entity);
            boolean shouldMoveToOwnerXZ = owner != null && Math.abs(owner.getY() - entity.getY()) < 1;
            double targetX = shouldMoveToOwnerXZ ? owner.getX() : entity.getX();
            double targetY = Math.max(entity.level().getMinBuildHeight() + 0.5F, owner == null ? 64F : owner.getY() < entity.getY() ? owner.getY() + 0.6F : owner.getY(1.0F) + entity.getBbHeight());
            if (owner != null && owner.getRootVehicle() == entity) {
                targetY = Math.min(entity.level().getMinBuildHeight() + 0.5F, entity.getY() - 0.5F);
            }
            double targetZ = shouldMoveToOwnerXZ ? owner.getZ() : entity.getZ();
            if (entity.verticalCollision) {
                entity.setOnGround(true);
                targetX += (entity.getRandom().nextFloat() - 0.5F) * 4;
                targetZ += (entity.getRandom().nextFloat() - 0.5F) * 4;
            }
            Vec3 move = new Vec3(targetX - entity.getX(), targetY - entity.getY(), targetZ - entity.getZ());
            entity.setDeltaMovement(entity.getDeltaMovement().add(move.normalize().scale(0.15D)).multiply(0.5F, 0.5F, 0.5F));
            if (entity.level() instanceof ServerLevel) {
                TameableUtils.setFallDistance(entity, entity.fallDistance);
                ((ServerLevel) entity.level()).sendParticles(ParticleTypes.REVERSE_PORTAL, entity.getRandomX(1.5F), entity.getY() - entity.getRandom().nextFloat(), entity.getRandomZ(1.5F), 0, 0, -0.2F, 0, 1.0D);
            }
        }
    }

    private static void tickOreScenting(LivingEntity entity) {
        int oreLvl = TameableUtils.getEnchantLevel(entity, DIEnchantmentRegistry.ORE_SCENTING);
        if (oreLvl > 0 && !entity.level().isClientSide()) {
            int interval = 100 + Math.max(150, 550 - oreLvl * 100);
            TameableUtils.detectRandomOres(entity, interval, 5 + oreLvl * 2, oreLvl * 50, oreLvl * 3);
        }
    }

    private static void tickZombiePetAI(Mob mob) {
        if (mob.getTarget() instanceof Player && ((Player) mob.getTarget()).isCreative()) {
            mob.setTarget(null);
        }
        if (mob.getTarget() == null || !mob.getTarget().isAlive()) {
            mob.setTarget(mob.level().getNearestPlayer(ServerEvent.ZOMBIE_TARGET, mob));
        } else if (mob.distanceTo(mob.getTarget()) < mob.getBbWidth() + 0.5F) {
            mob.doHurtTarget(mob.getTarget());
        } else if (mob.getNavigation().isDone()) {
            mob.getNavigation().moveTo(mob.getTarget(), 1.0D);
        }
    }

    private static void tickPsychicWall(Mob mob) {
        int psychicWallLevel = TameableUtils.getEnchantLevel(mob, DIEnchantmentRegistry.PSYCHIC_WALL);
        int cooldown = TameableUtils.getPsychicWallCooldown(mob);
        if (cooldown > 0) {
            TameableUtils.setPsychicWallCooldown(mob, cooldown - 1);
        } else {
            Entity blocking = null;
            Entity blockingFrom = null;
            if (mob.getTarget() != null) {
                blocking = mob.getTarget();
                blockingFrom = mob;
            } else if (TameableUtils.getOwnerOf(mob) instanceof LivingEntity owner) {
                if (owner.getLastHurtByMob() != null && owner.getLastHurtByMob().isAlive() && !TameableUtils.hasSameOwnerAs(mob, owner.getLastHurtByMob())) {
                    blocking = owner.getLastHurtByMob();
                    blockingFrom = owner;
                }
                if (owner.getLastHurtMob() != null && owner.getLastHurtMob().isAlive() && !TameableUtils.hasSameOwnerAs(mob, owner.getLastHurtMob())) {
                    blocking = owner.getLastHurtMob();
                    blockingFrom = owner;
                }
            }
            if (blocking != null) {
                int width = psychicWallLevel + 1;
                float yAdditional = blocking.getBbHeight() * 0.5F + width * 0.5F;
                Vec3 vec3 = blockingFrom.position().add(0, yAdditional, 0);
                Vec3 vec32 = blocking.position().add(0, yAdditional, 0);
                Vec3 vec33 = vec3.add(vec32);
                Vec3 avg = new Vec3(vec33.x / 2F, Math.floor(vec33.y / 2F), vec33.z / 2F);
                Vec3 rotationFrom = avg.subtract(vec3);
                Direction dir = Direction.getNearest(rotationFrom.x, rotationFrom.y, rotationFrom.z);
                PsychicWallEntity wall = PHEntityRegistry.PSYCHIC_WALL.create(mob.level());
                wall.setPos(avg.x, avg.y, avg.z);
                wall.setBlockWidth(width);
                wall.setCreatorId(mob.getUUID());
                wall.setLifespan(psychicWallLevel * 100);
                wall.setWallDirection(dir);
                mob.level().addFreshEntity(wall);
                TameableUtils.setPsychicWallCooldown(mob, psychicWallLevel * 200 + 40);
            }
        }
    }

    private static void tickBlazingProtection(LivingEntity entity) {
        int bars = TameableUtils.getBlazingProtectionBars(entity);
        if (bars < 2 * TameableUtils.getEnchantLevel(entity, DIEnchantmentRegistry.BLAZING_PROTECTION)) {
            int cooldown = TameableUtils.getBlazingProtectionCooldown(entity);
            if (cooldown > 0) {
                cooldown--;
            } else {
                TameableUtils.setBlazingProtectionBars(entity, bars + 1);
                cooldown = 200;
            }
            TameableUtils.setBlazingProtectionCooldown(entity, cooldown);
        }
    }

    private static void tickHealingAura(LivingEntity entity) {
        int time = TameableUtils.getHealingAuraTime(entity);
        if (time > 0) {
            List<LivingEntity> hurtNearby = TameableUtils.getAuraHealables(entity);
            for (LivingEntity needsHealing : hurtNearby) {
                if (!needsHealing.hasEffect(MobEffects.REGENERATION)) {
                    needsHealing.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, TameableUtils.getEnchantLevel(entity, DIEnchantmentRegistry.HEALING_AURA) - 1));
                }
            }
            time--;
            if (time == 0) {
                time = -600 - entity.getRandom().nextInt(600);
            }
        } else if (time < 0) {
            time++;
        } else if ((entity.tickCount + entity.getId()) % 200 == 0 || TameableUtils.getHealingAuraImpulse(entity)) {
            List<LivingEntity> hurtNearby = TameableUtils.getAuraHealables(entity);
            if (!hurtNearby.isEmpty()) {
                time = 200;
            }
            TameableUtils.setHealingAuraImpulse(entity, false);
        }
        TameableUtils.setHealingAuraTime(entity, time);
    }

    private static void tickFrozen(LivingEntity entity, int frozenTime) {
        TameableUtils.setFrozenTimeTag(entity, frozenTime - 1);
        AttributeInstance instance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance != null) {
            float f = -0.1F * entity.getPercentFrozen();
            if (frozenTime > 1) {
                AttributeModifier fangModifier = new AttributeModifier(FROST_FANG_SLOW, "Frost fang slow", f, AttributeModifier.Operation.ADDITION);
                if (!instance.hasModifier(fangModifier)) {
                    instance.addTransientModifier(fangModifier);
                }
            } else {
                instance.removeModifier(FROST_FANG_SLOW);
            }
        }
        for (int i = 0; i < 1 + entity.getRandom().nextInt(2); i++) {
            entity.level().addParticle(ParticleTypes.SNOWFLAKE, entity.getRandomX(0.7F), entity.getRandomY(), entity.getRandomZ(0.7F), 0.0F, 0.0F, 0.0F);
        }
    }

    // ===== 以下 4 个自 1.21 移植（chaos / night_vision / insight / sonic_boom 附魔） =====

    /** 混乱之脑：混乱(DRUNK)状态的怪物每几秒转向攻击 10 格内随机其他怪物，头顶冒问号粒子 */
    private static void tickChaos(Mob attacker) {
        if (attacker.level().isClientSide()) {
            return;
        }
        List<Monster> genericMobs = attacker.level().getEntitiesOfClass(Monster.class, LivingUtils.getBoundingBoxAroundEntity(attacker, 10.0F));
        Random random = new Random();
        if (attacker.hasEffect(ModEffects.DRUNK) && attacker instanceof Monster) {
            double x = attacker.getX();
            double y = attacker.getY() + attacker.getBbHeight() + 0.4;
            double z = attacker.getZ();
            if (attacker.level().getGameTime() % 10 == 0 && attacker.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(DIParticleRegistry.QUESTION_MARK, x, y, z, 1, 0, 0, 0, 0.0D);
            }

            if (genericMobs == null || genericMobs.isEmpty()) {
                return;
            }

            Monster monster = (Monster) attacker;
            Monster others = genericMobs.get(random.nextInt(genericMobs.size()));
            if (genericMobs.size() > 2) {
                while (others == monster) {
                    others = genericMobs.get(random.nextInt(genericMobs.size()));
                }
            }

            if (others == null) {
                monster.setTarget((LivingEntity) null);
            } else {
                LivingUtils.setAttackTarget(monster, others);
            }
        }
    }

    /** Night Vision（夜视）：主人靠近宠物时获得夜视效果 */
    private static void tickNightVision(LivingEntity pet) {
        if (TameableUtils.isTamed(pet)) {
            var owner = TameableUtils.getOwnerOf(pet);
            if (owner != null && owner.distanceToSqr(pet) < 10 && owner instanceof Player petOwner) {
                if (!petOwner.hasEffect(MobEffects.NIGHT_VISION) && pet.tickCount % 40 == 0) {
                    petOwner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60 * 5));
                }
            }
        }
    }

    /** Insight（洞察）：黑暗中的宠物让周围敌对生物发光 */
    private static void tickInsight(Mob pet) {
        int insightLevel = TameableUtils.getEnchantLevel(pet, DIEnchantmentRegistry.INSIGHT);
        if (insightLevel > 0 && pet.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getMaxLocalRawBrightness(pet.getOnPos().above()) < 9) {
                TameableUtils.applyGlowingEffect(pet, insightLevel);
            }
        }
    }

    /** Sonic Boom（音波轰击）：有目标且（距离 10~20 格或周围敌对密集）时，每 200 tick 释放一次 */
    private static void tickSonicBoom(Mob pet) {
        if (TameableUtils.hasEnchant(pet, DIEnchantmentRegistry.SONIC_BOOM)) {
            var beingAttacked = pet.getTarget();
            if (beingAttacked != null) {
                if (pet.closerThan(beingAttacked, 10.0, 20.0) || TameableUtils.getNearbyMobs(pet, 10).size() > 3) {
                    if (pet.tickCount % 200 == 0 && pet.level() instanceof ServerLevel serverLevel) {
                        TameableUtils.performSonicBook(pet, beingAttacked, serverLevel);
                    }
                }
            }
        }
    }
}
