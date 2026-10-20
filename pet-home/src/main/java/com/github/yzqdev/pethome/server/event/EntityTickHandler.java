package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.ClientGameEvents;
import com.github.yzqdev.pethome.datagen.ModEnchantments;
import com.github.yzqdev.pethome.server.entity.GiantBubbleEntity;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.PsychicWallEntity;
import com.github.yzqdev.pethome.server.misc.ModEffects;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.util.LivingUtils;
import com.github.yzqdev.pethome.util.TameableUtils;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import static com.github.yzqdev.pethome.server.event.ServerEvent.canTickCollar;

import java.util.List;
import java.util.Random;

/**
 * 宠物附魔 tick 引擎（{@code EntityTickEvent.Pre}，仅 Mob 且通过项圈防抖）：
 * 按四组处理附魔——生存（烈焰保护充能/无敌帧/抗毒/两栖供氧/治愈光环相位）、
 * 实用（夜视/虚空云缓降/矿石嗅探/连锁背包拾取/洞察发光）、
 * 战斗（音波轰击/暗影之手状态机/经验吸收）、控制（枯萎/恶名/威吓/牧羊人/念力墙/引力）；
 * 另含罗盘档案 20 tick 低频刷新与 DRUNK 怪物混乱（mobTick）。
 */
@EventBusSubscriber(modid = PetHomeMod.MODID)
public class EntityTickHandler {
    /** 每 Mob 每 tick 入口：先罗盘低频建档与 mobTick，再依次跑四组附魔 */
    @SubscribeEvent
    public static void onLivingUpdate( EntityTickEvent.Pre event) {
        var entity = event.getEntity();

        if (entity instanceof Mob pet && canTickCollar(entity)) {
            // 宠物罗盘：20 tick 低频档案更新（首次驯服懒初始化 PetId + 位置/名字/维度刷新，无变化不写盘）
            if (pet.tickCount % 20 == 0) {
                PetCompassTracker.updateRecord(pet);
            }
            mobTick(event, pet);

            // 1. 基础状态与生存类附魔
            handleSurvivalEnchants(event, pet);

            // 2. 实用与辅助类附魔 (夜视、传送、矿石等)
            handleUtilityEnchants(pet);

            // 3. 战斗与攻击类附魔 (冲击波、暗影手等)
            handleCombatEnchants(pet);

            // 4. 控制与防御类附魔 (引力、念力墙等)
            handleControlEnchants(pet);
        }
    }
    /**
     * DRUNK 效果副作用（对怪物生效）：10 格内随机转移仇恨到另一怪物并冒问号粒子；
     * AABB 查询仅在有该效果时执行。仅服务端（客户端 level 会 CCE）。
     */
    public static void mobTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event, LivingEntity attacker) {
        // 仅服务端执行：涉及 ServerLevel（粒子、目标选择）等逻辑，客户端 level 为 ClientLevel 会 ClassCastException
        if (attacker.level().isClientSide()) {
            return;
        }
        // 10 格 AABB 查询只在醉酒效果存在时才做（原先每 Mob 每 tick 无条件查询）
        if (attacker.hasEffect(ModEffects.DRUNK) && attacker instanceof Monster monster) {
            List<Monster> genericMobs = attacker.level().getEntitiesOfClass(Monster.class, LivingUtils.getBoundingBoxAroundEntity(attacker, (double) 10.0F));
            if (genericMobs == null) {
                return;
            }
            double x = attacker.getX();
            double y = attacker.getY() + attacker.getBbHeight() + 0.4;
            double z = attacker.getZ();
            if (attacker.level().getGameTime() % 10 == 0) {
                ((ServerLevel) (attacker.level())).sendParticles(PHParticleRegistry.QUESTION_MARK_PARTICLE_TYPE.get(), x, y, z, 1, 0, 0, 0, 0.0D);
            }

            if (genericMobs.size() <= 0) {
                return;
            }

            var random = monster.getRandom();
            Monster others = (Monster) genericMobs.get(random.nextInt(genericMobs.size()));
            if (genericMobs.size() > 2) {
                while (others == monster) {
                    others = (Monster) genericMobs.get(random.nextInt(genericMobs.size()));
                }
            }

            if (others == null) {
                monster.setTarget((LivingEntity) null);
            } else {
                LivingUtils.setAttackTarget(monster, others);
            }
        }
    }

    private static void handleSurvivalEnchants(net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event, Mob pet) {
        // 烈焰保护：宠物着火时向周围实体蔓延火焰
        if (TameableUtils.hasEnchant(pet, ModEnchantments.BLAZING_PROTECTION) && !pet.level().isClientSide()) {
            int bars = TameableUtils.getBlazingProtectionBars(pet);
            if (bars < 2 * TameableUtils.getEnchantLevel(pet, ModEnchantments.BLAZING_PROTECTION)) {
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

        // 无敌帧：受击后短暂无敌
        if (TameableUtils.hasEnchant(pet, ModEnchantments.IMMUNITY_FRAME) && !pet.level().isClientSide()) {
            int i = TameableUtils.getImmuneTime(pet);
            if (i > 0) {
                TameableUtils.setImmuneTime(pet, i - 1);
            }
        }

        // 毒素免疫
        if (pet.hasEffect(MobEffects.POISON) && TameableUtils.hasEnchant(pet, ModEnchantments.POISON_RESISTANCE)) {
            pet.removeEffect(MobEffects.POISON);
        }

        // 两栖：水生宠物离水后获得水下呼吸
        if (TameableUtils.hasEnchant(pet, ModEnchantments.AMPHIBIOUS)) {
            pet.setAirSupply(pet.getMaxAirSupply());
        }

        // 治愈光环：周期性治疗周围玩家
        if (TameableUtils.hasEnchant(pet, ModEnchantments.HEALING_AURA) && !pet.level().isClientSide()) {
            int time = TameableUtils.getHealingAuraTime(pet);
            if (time > 0) {
                List<LivingEntity> hurtNearby = TameableUtils.getAuraHealables(pet);
                for (LivingEntity needsHealing : hurtNearby) {
                    if (!needsHealing.hasEffect(MobEffects.REGENERATION)) {
                        needsHealing.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, TameableUtils.getEnchantLevel(pet, ModEnchantments.HEALING_AURA) - 1));
                    }
                }
                time--;
                if (time == 0) time = -600 - pet.getRandom().nextInt(600);
            } else if (time < 0) {
                time++;
            } else if ((pet.tickCount + pet.getId()) % 200 == 0 || TameableUtils.getHealingAuraImpulse(pet)) {
                List<LivingEntity> hurtNearby = TameableUtils.getAuraHealables(pet);
                if (!hurtNearby.isEmpty()) time = 200;
                TameableUtils.setHealingAuraImpulse(pet, false);
            }
            TameableUtils.setHealingAuraTime(pet, time);
        }
    }

    private static void handleUtilityEnchants(Mob pet) {
        // 夜视：给宠物与主人施加夜视
        if (TameableUtils.hasEnchant(pet, ModEnchantments.NIGHT_VISION) && TameableUtils.isTamed(pet)) {
            var owner = TameableUtils.getOwnerOf(pet);
            if (owner != null && owner.distanceToSqr(pet) < 10 && owner instanceof Player petOwner) {
                if (!petOwner.hasEffect(MobEffects.NIGHT_VISION) && pet.tickCount % 40 == 0) {
                    petOwner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60 * 5));
                }
            }
        }

        // 经验转移（当前停用）
//        if (TameableUtils.hasEnchant(pet, ModEnchantments.XP_Transfer)) {
//            TameableUtils.xpTransfer(pet);
//        }

        // 虚空云：缓慢下落防摔伤害
        if (TameableUtils.hasEnchant(pet, ModEnchantments.VOID_CLOUD) && !pet.isInWaterOrBubble() && pet.fallDistance > 3.0F && !pet.onGround()) {
            Entity owner = TameableUtils.getOwnerOf(pet);
            boolean shouldMoveToOwnerXZ = owner != null && Math.abs(owner.getY() - pet.getY()) < 1;
            double targetX = shouldMoveToOwnerXZ ? owner.getX() : pet.getX();
            double targetY = Math.max(pet.level().getMinBuildHeight() + 0.5F, owner == null ? 64F : owner.getY() < pet.getY() ? owner.getY() + 0.6F : owner.getY(1.0F) + pet.getBbHeight());
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
            pet.setDeltaMovement(pet.getDeltaMovement().add(move.normalize().scale(0.15D)).multiply(0.5F, 0.5F, 0.5F));
            if (pet.level() instanceof ServerLevel) {
                TameableUtils.setFallDistance(pet, pet.fallDistance);
                ((ServerLevel) pet.level()).sendParticles(ParticleTypes.REVERSE_PORTAL, pet.getRandomX(1.5F), pet.getY() - pet.getRandom().nextFloat(), pet.getRandomZ(1.5F), 0, 0, -0.2F, 0, 1.0D);
            }
        }

        // 矿石嗅探：高亮附近矿石
        int oreLvl = TameableUtils.getEnchantLevel(pet, ModEnchantments.ORE_SCENTING);
        if (oreLvl > 0 && !pet.level().isClientSide && pet.isAlive()) {
            int interval = 100 + Math.max(150, 550 - oreLvl * 100);
            TameableUtils.detectRandomOres(pet, interval, 5 + oreLvl * 2, oreLvl * 50, oreLvl * 3);
        }

        // 连锁背包
        if (TameableUtils.hasEnchant(pet, ModEnchantments.LINKED_INVENTORY)) {
            if (!pet.canPickUpLoot()) pet.setCanPickUpLoot(true);
        }

        // 洞察：对目标施加发光
        var insightLevel = TameableUtils.getEnchantLevel(pet, ModEnchantments.INSIGHT);
        // 效果时长 20 tick，每 20 tick 刷新一次即可（原先每 tick 做亮度查询+发光刷新）
        if (insightLevel > 0 && pet.tickCount % 20 == 0 && pet.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getMaxLocalRawBrightness(pet.getOnPos().above()) < 9) {
                TameableUtils.applyGlowingEffect(pet, insightLevel);
            }
        }
    }

    private static void handleCombatEnchants(Mob pet) {
        // 音波轰击：周期性对周围敌对生物造成音波伤害
        if (TameableUtils.hasEnchant(pet, ModEnchantments.SonicBoom) && !pet.level().isClientSide()) {
            var beingAttacked = pet.getTarget();
            // 200 tick 冷却判断前置，范围查询只在真正触发的 tick 做（原先每 tick 白做查询）
            if (beingAttacked != null && pet.tickCount % 200 == 0) {
                if (pet.closerThan(beingAttacked, 10.0, 20.0) || TameableUtils.getNearbyMobs(pet, 10).size() > 3) {
                    TameableUtils.performSonicBook(pet, beingAttacked, (ServerLevel) pet.level());
                }
            }
        }

        // 暗影之手
        int shadowHandsLevel = TameableUtils.getEnchantLevel(pet, ModEnchantments.SHADOW_HANDS);
        if (shadowHandsLevel > 0) {
            handleShadowHandsLogic(pet, shadowHandsLevel);
        }

        // 经验吸收：宠物吸收地上经验瓶回复自身
        if (TameableUtils.hasEnchant(pet, ModEnchantments.REJUVENATION)) {
            TameableUtils.absorbExpOrbs(pet);
        }
    }

    /**
     * 暗影之手主逻辑：目标从主人仇恨继承（玩家/驯服动物不作目标），按等级维护
     * 每只手的出拳进度/击打状态数组（存 citadel tag 经 sync 同步）并结算伤害；
     * 客户端分支只把插值数据写 {@code ClientGameEvents.shadowPunchRenderData} 供渲染。
     */
    private static void handleShadowHandsLogic(Mob mob, int level) {
        if (mob.level().isClientSide()) {
            // 渲染插值用的上一 tick 数据只在客户端本地维护；原先服务端也写这个客户端 Map，
            // 专用服务器上无人消费且实体卸载后不清理（内存泄漏）
            ClientGameEvents.updateVisualDataForMob(mob, TameableUtils.getShadowPunchTimes(mob));
            return;
        }

        var targetEntity = TameableUtils.getPetAttackTarget(mob);
        Entity punching = ((targetEntity instanceof Player) || (targetEntity instanceof TamableAnimal)) ? null : targetEntity;
        int[] punchProgress = TameableUtils.getShadowPunchTimes(mob);

        if (punching != null && punching.isAlive() && mob.hasLineOfSight(punching) && mob.distanceTo(punching) < 16) {
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
    }

    private static void handleControlEnchants(Mob pet) {
        // 枯萎诅咒
        if (TameableUtils.hasEnchant(pet, ModEnchantments.BLIGHT_CURSE)) {
            TameableUtils.destroyRandomPlants(pet);
        }
        // 臭名昭著：怪物优先攻击该宠物
        if (TameableUtils.hasEnchant(pet, ModEnchantments.INFAMY_CURSE)) {
            TameableUtils.aggroRandomMonsters(pet);
        }
        // 威吓：威吓粒子+击退周围敌对生物
        if (TameableUtils.hasEnchant(pet, ModEnchantments.INTIMIDATION)) {
            TameableUtils.scareRandomMonsters(pet, TameableUtils.getEnchantLevel(pet, ModEnchantments.INTIMIDATION));
        }
        // 牧羊人：引导周围同主人宠物跟随
        int shepherdLvl = TameableUtils.getEnchantLevel(pet, ModEnchantments.SHEPHERD);
        if (shepherdLvl > 0) {
            TameableUtils.attractAnimals(pet, shepherdLvl * 3);
        }

        // 念力墙
        int psychicWallLevel = TameableUtils.getEnchantLevel(pet, ModEnchantments.PSYCHIC_WALL);
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
                    if (owner.getLastHurtByMob() != null && owner.getLastHurtByMob().isAlive() && !TameableUtils.hasSameOwnerAs(pet, owner.getLastHurtByMob())) {
                        blocking = owner.getLastHurtByMob();
                        blockingFrom = owner;
                    } else if (owner.getLastHurtMob() != null && owner.getLastHurtMob().isAlive() && !TameableUtils.hasSameOwnerAs(pet, owner.getLastHurtMob())) {
                        blocking = owner.getLastHurtMob();
                        blockingFrom = owner;
                    }
                }
                if (blocking != null) {
                    int width = psychicWallLevel + 1;
                    float yAdd = blocking.getBbHeight() * 0.5F + width * 0.5F;
                    Vec3 avg = blockingFrom.position().add(0, yAdd, 0).add(blocking.position().add(0, yAdd, 0)).scale(0.5D);
                    avg = new Vec3(avg.x, Math.floor(avg.y), avg.z);
                    Vec3 rotationFrom = avg.subtract(blockingFrom.position().add(0, yAdd, 0));
                    Direction dir = Direction.getNearest(rotationFrom.x, rotationFrom.y, rotationFrom.z);
                    PsychicWallEntity wall = PHEntityRegistry.PSYCHIC_WALL.get().create(pet.level());
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

        // 引力：吸附周围掉落物
        if (TameableUtils.hasEnchant(pet, ModEnchantments.MAGNETIC)) {
            handleMagneticLogic(pet);
        }
    }

    /** magnetic：服务端锁定目标给双方互拉速度；客户端按目标画引力粒子并循环音效 */
    private static void handleMagneticLogic(Mob mob) {
        Entity sucking = TameableUtils.getPetAttackTarget(mob);
        if (!mob.level().isClientSide()) {
            if (mob.getTarget() == null || !mob.getTarget().isAlive() || mob.distanceTo(mob.getTarget()) < 0.5F + mob.getBbWidth() || mob.getRootVehicle() instanceof GiantBubbleEntity) {
                if (TameableUtils.getPetAttackTargetID(mob) != -1) TameableUtils.setPetAttackTarget(mob, -1);
            } else {
                TameableUtils.setPetAttackTarget(mob, mob.getTarget().getId());
            }
        } else if (sucking != null) {
            double dist = mob.distanceTo(sucking);
            Vec3 start = mob.position().add(0, mob.getBbHeight() * 0.5F, 0);
            Vec3 end = sucking.position().add(0, sucking.getBbHeight() * 0.5F, 0).subtract(start);
            for (float step = mob.getBbWidth() + 0.8F; step < (int) Math.ceil(dist); step++) {
                Vec3 vec = start.add(end.scale(step / dist));
                mob.level().addParticle(PHParticleRegistry.MAGNET.get(), vec.x + 0.5 * (mob.getRandom().nextFloat() - 0.5), vec.y + 0.5 * (mob.getRandom().nextFloat() - 0.5), vec.z + 0.5 * (mob.getRandom().nextFloat() - 0.5), 0, 0, 0);
            }
        }
        if (sucking != null) {
            if (mob.tickCount % 15 == 0) mob.playSound(PHSoundRegistry.MAGNET_LOOP.get(), 1F, 1F);
            mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.88D, 1.0D, 0.88D));
            Vec3 move = new Vec3(mob.getX() - sucking.getX(), mob.getY() - (double) sucking.getEyeHeight() / 2.0D - sucking.getY(), mob.getZ() - sucking.getZ());
            sucking.setDeltaMovement(sucking.getDeltaMovement().add(move.normalize().scale(mob.onGround() ? 0.15D : 0.05D)));
        }
    }


}
