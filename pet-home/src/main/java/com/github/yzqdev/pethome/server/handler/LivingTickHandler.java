package com.github.yzqdev.pethome.server.handler;

import com.github.yzqdev.pethome.client.ClientEvents;
import com.github.yzqdev.pethome.server.ServerEvents;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.FollowingJukeboxEntity;
import com.github.yzqdev.pethome.server.entity.GiantBubbleEntity;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.PsychicWallEntity;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.server.misc.PHEffects;
import com.github.yzqdev.pethome.util.LivingUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.UUID;


public class LivingTickHandler {

    /**
     * 宠物附魔 tick 引擎（LivingTickEvent，双端调用、内部各分支自判 isClientSide）：
     * 先做罗盘 20 tick 低频建档，再对可驯服生物依次驱动免疫帧/抗毒/两栖供氧/magnetic/
     * shadow_hands 状态机/disk_jockey/linked_inventory/被动光环（shepherd 等六种）/
     * void_cloud 缓降/ore_scenting/psychic_wall/blazing_protection/healing_aura/
     * night_vision/insight/sonic_boom；项圈防抖期内全部跳过。
     */
    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        // 宠物罗盘：20 tick 低频档案更新（首次驯服懒初始化 PetId + 位置/名字/维度刷新，无变化不写盘）
        if (event.getEntity() instanceof Mob compassMob && !event.getEntity().level().isClientSide() && compassMob.tickCount % 20 == 0) {
            PetCompassTracker.updateRecord(compassMob);
        }
        // 混乱之脑：混乱(DRUNK)状态的怪物转火周围其他怪物（自 1.21 移植；对所有怪物生效，不限于宠物）
        if (event.getEntity() instanceof Mob chaosMob && ServerEvents.canTickCollar(event.getEntity())) {
            tickChaos(chaosMob);
        }
        if (TameableUtils.couldBeTamed(event.getEntity()) && ServerEvents.canTickCollar(event.getEntity())) {
            tickImmunityFrame(event.getEntity());
            tickPoisonResistance(event.getEntity());
            tickAmphibious(event.getEntity());
            if (event.getEntity() instanceof Mob mob && TameableUtils.hasEnchant(event.getEntity(), PHEnchantmentRegistry.MAGNETIC)) {
                tickMagnetic(mob);
            }
            tickShadowHands(event.getEntity());
            tickDiskJockey(event.getEntity());
            if (event.getEntity() instanceof Mob mob && TameableUtils.hasEnchant(event.getEntity(), PHEnchantmentRegistry.LINKED_INVENTORY)) {
                tickLinkedInventory(mob);
            }
            tickPassiveAuras(event.getEntity());
            tickVoidCloud(event.getEntity());
            tickOreScenting(event.getEntity());
            if (event.getEntity() instanceof Mob mob && TameableUtils.getEnchantLevel(event.getEntity(), PHEnchantmentRegistry.PSYCHIC_WALL) > 0 && !event.getEntity().level().isClientSide()) {
                tickPsychicWall(mob);
            }
            if (TameableUtils.hasEnchant(event.getEntity(), PHEnchantmentRegistry.BLAZING_PROTECTION) && !event.getEntity().level().isClientSide()) {
                tickBlazingProtection(event.getEntity());
            }
            if (TameableUtils.hasEnchant(event.getEntity(), PHEnchantmentRegistry.HEALING_AURA) && !event.getEntity().level().isClientSide()) {
                tickHealingAura(event.getEntity());
            }
            // 以下 3 个自 1.21 移植
            if (TameableUtils.hasEnchant(event.getEntity(), PHEnchantmentRegistry.NIGHT_VISION) && !event.getEntity().level().isClientSide()) {
                tickNightVision(event.getEntity());
            }
            if (event.getEntity() instanceof Mob mob && !event.getEntity().level().isClientSide()) {
                tickInsight(mob);
                tickSonicBoom(mob);
            }
        }
    }

    private void tickImmunityFrame(LivingEntity entity) {
        if (TameableUtils.hasEnchant(entity, PHEnchantmentRegistry.IMMUNITY_FRAME) && !entity.level().isClientSide()) {
            int i = TameableUtils.getImmuneTime(entity);
            if (i > 0) {
                TameableUtils.setImmuneTime(entity, i - 1);
            }
        }
    }

    private void tickPoisonResistance(LivingEntity entity) {
        if (entity.hasEffect(MobEffects.POISON) && TameableUtils.hasEnchant(entity, PHEnchantmentRegistry.POISON_RESISTANCE)) {
            entity.removeEffect(MobEffects.POISON);
        }
    }

    private void tickAmphibious(LivingEntity entity) {
        if (TameableUtils.hasEnchant(entity, PHEnchantmentRegistry.AMPHIBIOUS)) {
            entity.setAirSupply(entity.getMaxAirSupply());
        }
    }

    /** magnetic（磁力）附魔：服务端记录当前吸附目标实体 ID；客户端画磁力线粒子；双端把目标往宠物方向拉动（宠物自身减速）。 */
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
                    mob.level().addParticle(PHParticleRegistry.MAGNET.get(), vec3.x + f1, vec3.y + f2, vec3.z + f3, 0.0F, 0.0F, 0.0F);
                }
            }
        }
        if (sucking != null) {
            if (mob.tickCount % 15 == 0) {
                mob.playSound(PHSoundRegistry.MAGNET_LOOP.get(), 1F, 1F);
            }
            mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.88D, 1.0D, 0.88D));
            Vec3 move = new Vec3(mob.getX() - sucking.getX(), mob.getY() - (double) sucking.getEyeHeight() / 2.0D - sucking.getY(), mob.getZ() - sucking.getZ());
            sucking.setDeltaMovement(sucking.getDeltaMovement().add(move.normalize().scale(mob.onGround() ? 0.15D : 0.05D)));
        }
    }

    /**
     * shadow_hands（暗影手）状态机入口：客户端只把出拳进度同步给渲染层（ClientEvents 本地 Map）；
     * 服务端目标不可为玩家/驯服动物，16 格内有视线则 {@link #tickShadowPunch}，否则 {@link #updateShadowPunchTarget}
     * （目标继承主人仇恨）。等级即并发拳数。
     */
    private void tickShadowHands(LivingEntity entity) {
        int shadowHandsLevel = TameableUtils.getEnchantLevel(entity, PHEnchantmentRegistry.SHADOW_HANDS);
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

    /** 单拳推进：5 tick 冷却占拳 → 进度 0..10 递增 → 满拳造成 clamp(等级,2,4) 点伤害并收拳。 */
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

    /**
     * 无有效出拳目标时回收进行中的拳并从宠物自身目标/主人仇恨里挑新目标（对齐 26.3：
     * 主人「被打目标」优先于「攻击过的目标」，else-if 连接）。收拳递减必须无条件执行：
     * 目标实体被移除后 getPetAttackTarget 返回 null，若把递减放在 punching!=null 里，打死敌人
     * （实体消失）后 punch 卡在当前值，对应手永久保持握拳姿态。
     */
    private void updateShadowPunchTarget(Mob mob, Entity punching, int[] punchProgress, int shadowHandsLevel) {
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
        Entity punchingTarget = null;
        if (mob.getTarget() != null) {
            punchingTarget = mob.getTarget();
        } else if (TameableUtils.getOwnerOf(mob) instanceof LivingEntity owner) {
            if (owner.getLastHurtByMob() != null && owner.getLastHurtByMob().isAlive() && !TameableUtils.hasSameOwnerAs(mob, owner.getLastHurtByMob())) {
                punchingTarget = owner.getLastHurtByMob();
            } else if (owner.getLastHurtMob() != null && owner.getLastHurtMob().isAlive() && !TameableUtils.hasSameOwnerAs(mob, owner.getLastHurtMob())) {
                punchingTarget = owner.getLastHurtMob();
            }
        }
        if (punchingTarget != null && punchingTarget.isAlive()) {
            TameableUtils.setPetAttackTarget(mob, punchingTarget.getId());
        }
    }

    /** disk_jockey（唱片骑师）：每 10 tick 确保跟随唱片实体存在（丢失则补建并写回 UUID）。 */
    private void tickDiskJockey(LivingEntity entity) {
        if (TameableUtils.hasEnchant(entity, PHEnchantmentRegistry.DISK_JOCKEY) && !entity.level().isClientSide() && entity.tickCount % 10 == 0) {
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

    /** linked_inventory（联动背包）：强制开启宠物的拾取能力（canPickUpLoot），拾取由 MobMixin 拦截入包。 */
    private void tickLinkedInventory(Mob mob) {
        if (!mob.canPickUpLoot()) {
            mob.setCanPickUpLoot(true);
        }
    }

    /** 各被动光环分发：shepherd 吸引动物、infamy 挑衅、intimidation 威吓、blight 枯萎、rejuvenation 吸经验回血（xp_transfer 走 ServerEvents.xpTransferTick 玩家驱动版）。 */
    private void tickPassiveAuras(LivingEntity entity) {
        int shepherdLvl = TameableUtils.getEnchantLevel(entity, PHEnchantmentRegistry.SHEPHERD);
        if (shepherdLvl > 0) {
            TameableUtils.attractAnimals(entity, shepherdLvl * 3);
        }
        if (TameableUtils.hasEnchant(entity, PHEnchantmentRegistry.INFAMY_CURSE)) {
            TameableUtils.aggroRandomMonsters(entity);
        }
        if (TameableUtils.hasEnchant(entity, PHEnchantmentRegistry.INTIMIDATION)) {
            TameableUtils.scareRandomMonsters(entity, TameableUtils.getEnchantLevel(entity, PHEnchantmentRegistry.INTIMIDATION));
        }
        if (TameableUtils.hasEnchant(entity, PHEnchantmentRegistry.BLIGHT_CURSE)) {
            TameableUtils.destroyRandomPlants(entity);
        }
        if (TameableUtils.hasEnchant(entity, PHEnchantmentRegistry.REJUVENATION)) {
            TameableUtils.absorbExpOrbs(entity);
        }
    }

    /** void_cloud（虚空云）缓降：下落 >3 格时施加反向运动减速悬停（朝主人位置/高度靠拢），并同步 fallDistance。 */
    private void tickVoidCloud(LivingEntity entity) {
        if (TameableUtils.hasEnchant(entity, PHEnchantmentRegistry.VOID_CLOUD) && !entity.isInWaterOrBubble() && entity.fallDistance > 3.0F && !entity.onGround()) {
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

    /** ore_scenting 节流入口：按等级换算嗅探间隔/范围/高亮时长/矿点数上限后委托 {@link TameableUtils#detectRandomOres}。 */
    private void tickOreScenting(LivingEntity entity) {
        int oreLvl = TameableUtils.getEnchantLevel(entity, PHEnchantmentRegistry.ORE_SCENTING);
        if (oreLvl > 0 && !entity.level().isClientSide()) {
            int interval = 100 + Math.max(150, 550 - oreLvl * 100);
            TameableUtils.detectRandomOres(entity, interval, 5 + oreLvl * 2, oreLvl * 50, oreLvl * 3);
        }
    }

    /** psychic_wall（念力墙）：冷却结束后在宠物（或主人仇恨目标）与目标之间竖起 level+1 格宽、level*100 tick 寿命的墙，并置 level*200+40 tick 冷却（目标回退 else-if，对齐 26.3）。 */
    private void tickPsychicWall(Mob mob) {
        int psychicWallLevel = TameableUtils.getEnchantLevel(mob, PHEnchantmentRegistry.PSYCHIC_WALL);
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
                } else if (owner.getLastHurtMob() != null && owner.getLastHurtMob().isAlive() && !TameableUtils.hasSameOwnerAs(mob, owner.getLastHurtMob())) {
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

    /** blazing_protection 充能：护盾未满（2*级）时每 200 tick 充一条。 */
    private void tickBlazingProtection(LivingEntity entity) {
        int bars = TameableUtils.getBlazingProtectionBars(entity);
        if (bars < 2 * TameableUtils.getEnchantLevel(entity, PHEnchantmentRegistry.BLAZING_PROTECTION)) {
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

    /**
     * healing_aura 相位机（时间存独立数据槽）：>0 治疗期——给 4 格内同主受伤者上再生，归零转入
     * 负值冷却（-600~-1200）；<0 冷却期逐 tick 回升；==0 待机——每 200 tick 或受击脉冲（impulse）时
     * 附近有伤员则进入 200 tick 治疗期。负值也经 setter 写槽（实现侧判等防无效同步）。
     */
    private void tickHealingAura(LivingEntity entity) {
        int time = TameableUtils.getHealingAuraTime(entity);
        if (time > 0) {
            List<LivingEntity> hurtNearby = TameableUtils.getAuraHealables(entity);
            for (LivingEntity needsHealing : hurtNearby) {
                if (!needsHealing.hasEffect(MobEffects.REGENERATION)) {
                    needsHealing.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, TameableUtils.getEnchantLevel(entity, PHEnchantmentRegistry.HEALING_AURA) - 1));
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

    // ===== 以下 4 个自 1.21 移植（chaos / night_vision / insight / sonic_boom 附魔） =====

    /** 混乱之脑：混乱(DRUNK)状态的怪物每几秒转向攻击 10 格内随机其他怪物，头顶冒问号粒子 */
    private void tickChaos(Mob attacker) {
        if (attacker.level().isClientSide() || !attacker.hasEffect(PHEffects.DRUNK.get()) || !(attacker instanceof Monster)) {
            return;
        }
        double x = attacker.getX();
        double y = attacker.getY() + attacker.getBbHeight() + 0.4;
        double z = attacker.getZ();
        if (attacker.level().getGameTime() % 10 == 0) {
            ((ServerLevel) (attacker.level())).sendParticles(PHParticleRegistry.QUESTION_MARK.get(), x, y, z, 1, 0, 0, 0, 0.0D);
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
        var insightLevel = TameableUtils.getEnchantLevel(pet, PHEnchantmentRegistry.INSIGHT);
        // %20 节流：发光效果时长 20 tick，到点重刷即可；原先是每 tick 做一次 level*15 格的大范围实体查询
        if (insightLevel > 0 && pet.tickCount % 20 == 0 && pet.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getMaxLocalRawBrightness(pet.getOnPos().above()) < 9) {
                TameableUtils.applyGlowingEffect(pet, insightLevel);
            }
        }
    }

    /** Sonic Boom（音波轰击）：有目标且（距离 10~20 格或周围敌对密集）时，每 200 tick 释放一次 */
    private void tickSonicBoom(Mob pet) {
        if (TameableUtils.hasEnchant(pet, PHEnchantmentRegistry.SONIC_BOOM)) {
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
