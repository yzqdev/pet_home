package com.github.yzqdev.pethome.server.handler;

import com.github.yzqdev.pethome.client.ClientEvents;
import com.github.yzqdev.pethome.server.ServerEvents;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.UUID;

/**
 * 从 CommonProxy.onLivingUpdate 拆分而来：宠物每 tick 的各种附魔效果。
 */
public class LivingTickHandler {

    private static final UUID FROST_FANG_SLOW = UUID.fromString("1eaf83ff-7207-4596-b37a-d7a07b3ec4cf");

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        // 宠物罗盘：20 tick 低频档案更新（首次驯服懒初始化 PetId + 位置/名字/维度刷新，无变化不写盘）
        if (event.getEntity() instanceof Mob compassMob && !event.getEntity().level().isClientSide() && compassMob.tickCount % 20 == 0) {
            PetCompassTracker.updateRecord(compassMob);
        }
        int frozenTime = TameableUtils.getFrozenTime(event.getEntity());
        // 混乱之脑：混乱(DRUNK)状态的怪物转火周围其他怪物（自 1.21 移植；对所有怪物生效，不限于宠物）
        if (event.getEntity() instanceof Mob chaosMob && ServerEvents.canTickCollar(event.getEntity())) {
            tickChaos(chaosMob);
        }
        if (TameableUtils.couldBeTamed(event.getEntity()) && ServerEvents.canTickCollar(event.getEntity())) {
            tickImmunityFrame(event.getEntity());
            tickPoisonResistance(event.getEntity());
            tickAmphibious(event.getEntity());
            if (event.getEntity() instanceof Mob mob && TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.MAGNETIC)) {
                tickMagnetic(mob);
            }
            tickShadowHands(event.getEntity());
            tickDiskJockey(event.getEntity());
            if (event.getEntity() instanceof Mob mob && TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.LINKED_INVENTORY)) {
                tickLinkedInventory(mob);
            }
            tickPassiveAuras(event.getEntity());
            tickVoidCloud(event.getEntity());
            tickOreScenting(event.getEntity());
            if (event.getEntity() instanceof Mob mob && TameableUtils.isZombiePet(event.getEntity()) && !event.getEntity().level().isClientSide()) {
                tickZombiePetAI(mob);
            }
            if (event.getEntity() instanceof Mob mob && TameableUtils.getEnchantLevel(event.getEntity(), DIEnchantmentRegistry.PSYCHIC_WALL) > 0 && !event.getEntity().level().isClientSide()) {
                tickPsychicWall(mob);
            }
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.BLAZING_PROTECTION) && !event.getEntity().level().isClientSide()) {
                tickBlazingProtection(event.getEntity());
            }
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.HEALING_AURA) && !event.getEntity().level().isClientSide()) {
                tickHealingAura(event.getEntity());
            }
            // 以下 3 个自 1.21 移植
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.NIGHT_VISION) && !event.getEntity().level().isClientSide()) {
                tickNightVision(event.getEntity());
            }
            if (event.getEntity() instanceof Mob mob && !event.getEntity().level().isClientSide()) {
                tickInsight(mob);
                tickSonicBoom(mob);
            }
        }

        if (frozenTime > 0) {
            tickFrozen(event.getEntity(), frozenTime);
        }
    }

    private void tickImmunityFrame(LivingEntity entity) {
        if (TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.IMMUNITY_FRAME) && !entity.level().isClientSide()) {
            int i = TameableUtils.getImmuneTime(entity);
            if (i > 0) {
                TameableUtils.setImmuneTime(entity, i - 1);
            }
        }
    }

    private void tickPoisonResistance(LivingEntity entity) {
        if (entity.hasEffect(MobEffects.POISON) && TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.POISON_RESISTANCE)) {
            entity.removeEffect(MobEffects.POISON);
        }
    }

    private void tickAmphibious(LivingEntity entity) {
        if (TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.AMPHIBIOUS)) {
            entity.setAirSupply(entity.getMaxAirSupply());
        }
    }

    private void tickMagnetic(Mob mob) {
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
                    mob.level().addParticle(DIParticleRegistry.MAGNET.get(), vec3.x + f1, vec3.y + f2, vec3.z + f3, 0.0F, 0.0F, 0.0F);
                }
            }
        }
        if (sucking != null) {
            if (mob.tickCount % 15 == 0) {
                mob.playSound(DISoundRegistry.MAGNET_LOOP.get(), 1F, 1F);
            }
            mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.88D, 1.0D, 0.88D));
            Vec3 move = new Vec3(mob.getX() - sucking.getX(), mob.getY() - (double) sucking.getEyeHeight() / 2.0D - sucking.getY(), mob.getZ() - sucking.getZ());
            sucking.setDeltaMovement(sucking.getDeltaMovement().add(move.normalize().scale(mob.onGround() ? 0.15D : 0.05D)));
        }
    }

    private void tickShadowHands(LivingEntity entity) {
        int shadowHandsLevel = TameableUtils.getEnchantLevel(entity, DIEnchantmentRegistry.SHADOW_HANDS);
        if (shadowHandsLevel <= 0 || !(entity instanceof Mob)) {
            return;
        }
        Mob mob = (Mob) entity;
        if (entity.level().isClientSide()) {
            // 渲染插值用的上一 tick 数据只在客户端本地维护；原先服务端也写这个客户端 Map，
            // 专用服务器上无人消费且实体卸载后不清理（内存泄漏）
            ClientEvents.updateVisualDataForMob(entity, TameableUtils.getShadowPunchTimes(mob));
        }
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

    private void tickShadowPunch(Mob mob, Entity punching, int[] punchProgress, int shadowHandsLevel) {
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

    private void updateShadowPunchTarget(Mob mob, Entity punching, int[] punchProgress, int shadowHandsLevel) {
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

    private void tickDiskJockey(LivingEntity entity) {
        if (TameableUtils.hasEnchant(entity, DIEnchantmentRegistry.DISK_JOCKEY) && !entity.level().isClientSide() && entity.tickCount % 10 == 0) {
            UUID uuid = TameableUtils.getPetJukeboxUUID(entity);
            if (uuid == null || !(((ServerLevel) entity.level()).getEntity(uuid) instanceof FollowingJukeboxEntity)) {
                FollowingJukeboxEntity follower = PHEntityRegistry.FOLLOWING_JUKEBOX.get().create(entity.level());
                follower.setFollowingUUID(entity.getUUID());
                follower.copyPosition(entity);
                entity.level().addFreshEntity(follower);
                TameableUtils.setPetJukeboxUUID(entity, follower.getUUID());
            }
        }
    }

    private void tickLinkedInventory(Mob mob) {
        if (!mob.canPickUpLoot()) {
            mob.setCanPickUpLoot(true);
        }
    }

    private void tickPassiveAuras(LivingEntity entity) {
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

    private void tickVoidCloud(LivingEntity entity) {
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

    private void tickOreScenting(LivingEntity entity) {
        int oreLvl = TameableUtils.getEnchantLevel(entity, DIEnchantmentRegistry.ORE_SCENTING);
        if (oreLvl > 0 && !entity.level().isClientSide()) {
            int interval = 100 + Math.max(150, 550 - oreLvl * 100);
            TameableUtils.detectRandomOres(entity, interval, 5 + oreLvl * 2, oreLvl * 50, oreLvl * 3);
        }
    }

    private void tickZombiePetAI(Mob mob) {
        if (mob.getTarget() instanceof Player && ((Player) mob.getTarget()).isCreative()) {
            mob.setTarget(null);
        }
        if (mob.getTarget() == null || !mob.getTarget().isAlive()) {
            mob.setTarget(mob.level().getNearestPlayer(ServerEvents.ZOMBIE_TARGET, mob));
        } else if (mob.distanceTo(mob.getTarget()) < mob.getBbWidth() + 0.5F) {
            mob.doHurtTarget(mob.getTarget());
        } else if (mob.getNavigation().isDone()) {
            mob.getNavigation().moveTo(mob.getTarget(), 1.0D);
        }
    }

    private void tickPsychicWall(Mob mob) {
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
                PsychicWallEntity wall = PHEntityRegistry.PSYCHIC_WALL.get().create(mob.level());
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

    private void tickBlazingProtection(LivingEntity entity) {
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

    private void tickHealingAura(LivingEntity entity) {
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

    private void tickFrozen(LivingEntity entity, int frozenTime) {
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
    private void tickChaos(Mob attacker) {
        if (attacker.level().isClientSide() || !attacker.hasEffect(ModEffects.DRUNK.get()) || !(attacker instanceof Monster)) {
            return;
        }
        double x = attacker.getX();
        double y = attacker.getY() + attacker.getBbHeight() + 0.4;
        double z = attacker.getZ();
        if (attacker.level().getGameTime() % 10 == 0) {
            ((ServerLevel) (attacker.level())).sendParticles(DIParticleRegistry.QUESTION_MARK.get(), x, y, z, 1, 0, 0, 0, 0.0D);
        }
        // 实体查询移到效果判断之后：绝大多数 tick 没有 DRUNK 效果，原先每 Mob 每 tick 都做 10 格 AABB 查询
        List<Monster> genericMobs = attacker.level().getEntitiesOfClass(Monster.class, LivingUtils.getBoundingBoxAroundEntity(attacker, 10.0F));
        if (genericMobs.isEmpty()) {
            return;
        }
        var random = attacker.getRandom();
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

    /** Night Vision（夜视）：主人靠近宠物时获得夜视效果 */
    private void tickNightVision(LivingEntity pet) {
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
    private void tickInsight(Mob pet) {
        var insightLevel = TameableUtils.getEnchantLevel(pet, DIEnchantmentRegistry.INSIGHT);
        // %20 节流：发光效果时长 20 tick，到点重刷即可；原先是每 tick 做一次 level*15 格的大范围实体查询
        if (insightLevel > 0 && pet.tickCount % 20 == 0 && pet.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getMaxLocalRawBrightness(pet.getOnPos().above()) < 9) {
                TameableUtils.applyGlowingEffect(pet, insightLevel);
            }
        }
    }

    /** Sonic Boom（音波轰击）：有目标且（距离 10~20 格或周围敌对密集）时，每 200 tick 释放一次 */
    private void tickSonicBoom(Mob pet) {
        if (TameableUtils.hasEnchant(pet, DIEnchantmentRegistry.SONIC_BOOM)) {
            var beingAttacked = pet.getTarget();
            // 200 tick 冷却判断前置，避免每 tick 白做 10 格实体查询
            if (beingAttacked != null && pet.tickCount % 200 == 0 && pet.level() instanceof ServerLevel serverLevel) {
                if (pet.closerThan(beingAttacked, 10.0, 20.0) || TameableUtils.getNearbyMobs(pet, 10).size() > 3) {
                    TameableUtils.performSonicBook(pet, beingAttacked, serverLevel);
                }
            }
        }
    }
}
