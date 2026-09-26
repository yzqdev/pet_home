package com.github.yzqdev.pethome.server;


import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.DIBlockRegistry;
import com.github.yzqdev.pethome.server.block.PetBedBlock;
import com.github.yzqdev.pethome.server.block.PetBedBlockEntity;
import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.*;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.misc.*;
import com.github.yzqdev.pethome.server.misc.trades.*;
import com.github.yzqdev.pethome.util.FriendlyFireCommon;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.antlr.v4.runtime.misc.Triple;

import java.util.*;
import java.util.function.Predicate;


@Mod.EventBusSubscriber(modid = PetHomeMod.MODID)
public class ServerEvents {

    public static final TargetingConditions ZOMBIE_TARGET = TargetingConditions.forCombat().range(32.0D);
    //list of pets to be teleported across dimensions, cleared after every tick
    public static List<Triple<Entity, ServerLevel, UUID>> teleportingPets = new ArrayList<>();

    // 以维度 key 而非 Level 实例为键：Level 在维度重载时会重建，旧实例作键会永久滞留导致泄漏
    private static final Map<ResourceKey<Level>, CollarTickTracker> COLLAR_TICK_TRACKER_MAP = new HashMap<>();

    public static void serverInit() {
        ForgeChunkManager.setForcedChunkLoadingCallback(PetHomeMod.MODID, ServerEvents::removeAllChunkTickets);
        DIVillagePieceRegistry.registerHouses();
    }

    /** 服务器启动时把注册好的宠物商店元素注入村庄结构池（26.1 侧同模式） */
    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        VillageHouseManager.addAllHouses(event.getServer().registryAccess());
    }

    /** swing_through_pets：主人的攻击不落在自家宠物身上，而是穿透命中宠物身后的生物 */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!PetHomeMod.CONFIG.swingThroughPets.get()) {
            return;
        }
        if (!(event.getTarget() instanceof LivingEntity pet) || !TameableUtils.isPetOf(event.getEntity(), pet)) {
            return;
        }
        event.setCanceled(true);
        Player attacker = event.getEntity();
        if (attacker.level().isClientSide()) {
            return;
        }
        Vec3 from = attacker.getEyePosition();
        Vec3 dir = attacker.getViewVector(1.0F);
        Vec3 to = from.add(dir.scale(4.5D));
        AABB box = attacker.getBoundingBox().expandTowards(dir.scale(4.5D)).inflate(1.0D);
        EntityHitResult behind = ProjectileUtil.getEntityHitResult(attacker.level(), attacker, from, to, box,
                e -> e != pet && e != attacker && !e.isSpectator() && e.isPickable() && e instanceof LivingEntity);
        if (behind != null) {
            attacker.attack(behind.getEntity());
        }
    }

    private static void removeAllChunkTickets(ServerLevel serverLevel, ForgeChunkManager.TicketHelper ticketHelper) {
        int i = 0;
        for (Map.Entry<UUID, Pair<LongSet, LongSet>> entry : ticketHelper.getEntityTickets().entrySet()) {
            ticketHelper.removeAllTickets(entry.getKey());
            i++;
        }
        PetHomeMod.LOGGER.debug("Removed " + i + " chunkloading tickets");
    }

    /** 项圈刚被取下/装备后短时间内不再 tick */
    public static boolean canTickCollar(Entity entity) {
        if (entity.level().isClientSide()) {
            return true;
        } else {
            CollarTickTracker tracker = COLLAR_TICK_TRACKER_MAP.get(entity.level().dimension());
            return tracker == null || !tracker.isEntityBlocked(entity);
        }
    }

    public static void blockCollarTick(Entity entity) {
        if (!entity.level().isClientSide()) {
            // computeIfAbsent：tracker 不存在时创建而不是解引用 null（原逻辑在 tracker==null 时 NPE、非空时什么都不做）
            COLLAR_TICK_TRACKER_MAP.computeIfAbsent(entity.level().dimension(), k -> new CollarTickTracker())
                    .addBlockedEntityTick(entity.getUUID(), 5);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorldEvent(EntityJoinLevelEvent event) {
        // 宠物罗盘：档案更新（区块加载/维度切换/宠物床复活回到世界——复活实体带着原 PetId，重新绑定 Entity UUID）
        if (event.getEntity() instanceof LivingEntity living && !event.getLevel().isClientSide() && TameableUtils.isTamed(living)) {
            PetCompassTracker.updateRecord(living);
        }
        if (event.getEntity() instanceof LivingEntity living && TameableUtils.couldBeTamed(living)) {
            if (TameableUtils.hasEnchant(living, DIEnchantmentRegistry.HEALTH_BOOST)) {
                living.setHealth((float) Math.max(living.getHealth(), TameableUtils.getSafePetHealth(living)));
            }
            if (living.isAlive() && TameableUtils.isTamed(living)) {
                DIWorldData data = DIWorldData.get(living.level());
                if (data != null) {
                    data.removeMatchingLanternRequests(living.getUUID());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            if (!living.level().isClientSide() && living.isAlive() && TameableUtils.isTamed(living) && TameableUtils.shouldUnloadToLantern(living)) {
                UUID ownerUUID = TameableUtils.getOwnerUUIDOf(event.getEntity());
                String saveName = event.getEntity().hasCustomName() ? event.getEntity().getCustomName().getString() : "";
                DIWorldData data = DIWorldData.get(living.level());
                if (data != null) {
                    LanternRequest request = new LanternRequest(living.getUUID(), ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType()).toString(), ownerUUID, living.blockPosition(), event.getEntity().level().dayTime(), saveName);
                    data.addLanternRequest(request);
                }
            }
            if (TameableUtils.couldBeTamed(living) && TameableUtils.hasEnchant(living, DIEnchantmentRegistry.HEALTH_BOOST)) {
                TameableUtils.setSafePetHealth(living, living.getHealth());
            }

        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.LevelTickEvent tick) {
        // 宠物罗盘召回队列：全局队列只在主世界 tick 上处理（每 20 tick 一轮）
        if (!tick.level.isClientSide() && tick.phase == TickEvent.Phase.END && tick.level.dimension() == Level.OVERWORLD && tick.level.getGameTime() % 20 == 0) {
            PetCompassTeleport.processRecallQueue(tick.level.getServer(), tick.level.getGameTime());
        }
        if (!tick.level.isClientSide()) {
            COLLAR_TICK_TRACKER_MAP.computeIfAbsent(tick.level.dimension(), k -> new CollarTickTracker());
            CollarTickTracker tracker = COLLAR_TICK_TRACKER_MAP.get(tick.level.dimension());
            tracker.tick();
        }
        if (!tick.level.isClientSide() && tick.level instanceof ServerLevel) {
            for (final var triple : teleportingPets) {
                Entity entity = triple.a;
                ServerLevel endpointWorld = triple.b;
                UUID ownerUUID = triple.c;
                entity.unRide();
                entity.setLevel(endpointWorld);
                Entity player = endpointWorld.getPlayerByUUID(ownerUUID);
                if (player != null) {
                    Entity teleportedEntity = entity.getType().create(endpointWorld);
                    if (teleportedEntity != null) {
                        teleportedEntity.restoreFrom(entity);
                        Vec3 toPos = player.position();
                        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
                        AABB suffocationBox = new AABB(-dimensions.width / 2.0F, 0, -dimensions.width / 2.0F, dimensions.width / 2.0F, dimensions.height, dimensions.width / 2.0F);
                        while (!endpointWorld.noCollision(entity, suffocationBox.move(toPos.x, toPos.y, toPos.z)) && toPos.y < 300) {
                            toPos = toPos.add(0, 1, 0);
                        }
                        teleportedEntity.moveTo(toPos.x, toPos.y, toPos.z, entity.getYRot(), entity.getXRot());
                        teleportedEntity.setYHeadRot(entity.getYHeadRot());
                        teleportedEntity.fallDistance = 0.0F;
                        teleportedEntity.setPortalCooldown();
                        endpointWorld.addFreshEntity(teleportedEntity);
                    }
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
            teleportingPets.clear();
        }
    }

    @SubscribeEvent
    public static void onProjectileImpactEvent(ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult) {
            Entity hit = ((EntityHitResult) event.getRayTraceResult()).getEntity();
            if (event.getProjectile().getOwner() instanceof Player) {
                Player player = (Player) event.getProjectile().getOwner();
                if (TameableUtils.isPetOf(player, hit)) {
//                    event.setCanceled(true);
                    event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
                }
            }
            if (TameableUtils.isTamed(hit)) {
                if (event.getEntity() instanceof AbstractArrow arrow) {
                    //fixes soft crash with vanilla
                    if (arrow.getPierceLevel() > 0) {
                        arrow.setPierceLevel((byte) 0);
                        arrow.remove(Entity.RemovalReason.DISCARDED);
//                        event.setCanceled(true);
                        event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
                        return;
                    }
                }
                if (TameableUtils.hasEnchant((LivingEntity) hit, DIEnchantmentRegistry.DEFLECTION)) {
//                    event.setCanceled(true);
                    event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
                    float xRot = event.getProjectile().getXRot();
                    float yRot = event.getProjectile().yRotO;
                    Vec3 vec3 = event.getProjectile().position().subtract(hit.position()).normalize().scale(hit.getBbWidth() + 0.5F);
                    Vec3 vec32 = hit.position().add(vec3);
                    hit.level().addParticle(DIParticleRegistry.DEFLECTION_SHIELD.get(), vec32.x, vec32.y, vec32.z, xRot, yRot, 0.0F);
                    event.getProjectile().setDeltaMovement(event.getProjectile().getDeltaMovement().scale(-0.2D));
                    event.getProjectile().setYRot(yRot + 180);
                    event.getProjectile().setXRot(xRot + 180);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemDespawnEvent(ItemExpireEvent event) {
        if (event.getEntity().getItem().getItem() == Items.APPLE && PetHomeMod.CONFIG.rottenApple.get()) {
            if (new Random().nextFloat() < 0.1F * event.getEntity().getItem().getCount()) {
                event.getEntity().getItem().shrink(1);
                event.setExtraLife(10);
                ItemEntity rotten = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(PHItemRegistry.ROTTEN_APPLE.get()));
                event.getEntity().level().addFreshEntity(rotten);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingHurtEvent event) {
//friendly fire
        if (FriendlyFireCommon.preventAttack(event.getEntity(), event.getSource(), event.getAmount())) {


            event.getEntity().setLastHurtByMob(null);

            if (event.getSource().getEntity() instanceof LivingEntity trueSource) {

                trueSource.setLastHurtByMob(null);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
//        if(TameableUtils.isTamed(event.getEntity()) && event.getSource().getDirectEntity() instanceof Player player && TameableUtils.isPetOf(player, event.getEntity()) && !player.isShiftKeyDown()){
//            event.setCanceled(true);
//        }
        if (event.getSource().getEntity() instanceof LivingEntity pet && TameableUtils.isTamed(event.getSource().getEntity())) {
            if (TameableUtils.hasEnchant(pet, DIEnchantmentRegistry.IMMATURITY_CURSE)) {
                event.setAmount((float) Math.ceil(event.getAmount() * 0.7F));
            }
        }

    }

    @SubscribeEvent
    public static void onLivingDie(LivingDeathEvent event) {
        if (TameableUtils.isTamed(event.getEntity()) && !TameableUtils.isZombiePet(event.getEntity())) {

            BlockPos bedPos = TameableUtils.getPetBedPos(event.getEntity());
            if (bedPos != null) {
                CompoundTag data = new CompoundTag();
                event.getEntity().addAdditionalSaveData(data);
                String saveName = event.getEntity().hasCustomName() ? event.getEntity().getCustomName().getString() : "";
                RespawnRequest request = new RespawnRequest(ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType()).toString(), TameableUtils.getPetBedDimension(event.getEntity()), data, bedPos, event.getEntity().level().dayTime(), saveName);
                DIWorldData worldData = DIWorldData.get(event.getEntity().level());
                if (worldData != null) {
                    worldData.addRespawnRequest(request);
                }
            }
            if (!(event.getEntity() instanceof TamableAnimal)) {
                Entity owner = TameableUtils.getOwnerOf(event.getEntity());
                if (!event.getEntity().level().isClientSide() && event.getEntity().level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && owner instanceof ServerPlayer) {
                    owner.sendSystemMessage(event.getEntity().getCombatTracker().getDeathMessage());
                }
            }
            if (event.getEntity() instanceof Mob mob && event.getEntity().level().getDifficulty() != Difficulty.PEACEFUL && TameableUtils.hasEnchant(mob, DIEnchantmentRegistry.UNDEAD_CURSE)) {
                Mob zombieCopy = (Mob) mob.getType().create(mob.level());
                int id = zombieCopy.getId();
                Entity owner = TameableUtils.getOwnerOf(mob);
                CompoundTag livingNbt = new CompoundTag();
                mob.addAdditionalSaveData(livingNbt);
                livingNbt.putString("DeathLootTable", BuiltInLootTables.EMPTY.toString());
                zombieCopy.readAdditionalSaveData(livingNbt);
                zombieCopy.setId(id);
                if (zombieCopy instanceof TamableAnimal tamed) {
                    tamed.setTame(false);
                    tamed.setOwnerUUID(null);
                    tamed.setOrderedToSit(false);
                }
                if (zombieCopy instanceof ModifedToBeTameable tameable) {
                    tameable.setTame(false);
                    tameable.setTameOwnerUUID(null);
                }
                if (zombieCopy instanceof IComandableMob commandableMob) {
                    commandableMob.setCommand(0);
                }
                zombieCopy.copyPosition(mob);
                zombieCopy.setTarget(owner instanceof Player && !((Player) owner).isCreative() ? (Player) owner : mob.level().getNearestPlayer(ZOMBIE_TARGET, mob));
                mob.level().addFreshEntity(zombieCopy);
                zombieCopy.setHealth(zombieCopy.getMaxHealth());
                TameableUtils.setZombiePet(zombieCopy, true);
            }

            // 宠物罗盘：死亡档案保留，仅标记待宠物床复活（compass.md：不因 Entity UUID 消失而删档）
            PetCompassTracker.onPetDeath(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {

        if (event.getEntityBeingMounted() instanceof GiantBubbleEntity && event.isDismounting() && event.getEntityBeingMounted().isAlive()) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().getBlock() instanceof PetBedBlock) {
            if (event.getLevel().getBlockEntity(event.getPos()) instanceof PetBedBlockEntity entity1) {
                entity1.removeAllRequestsFor(event.getPlayer());
                entity1.resetBedsForNearbyPets();
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(MobSpawnEvent.FinalizeSpawn event) {
        try {
            if (event.getEntity() != null && event.getEntity() instanceof Ravager && PetHomeMod.CONFIG.rabbitsScareRavagers.get()) {
                Ravager ravager = (Ravager) event.getEntity();
                ravager.goalSelector.addGoal(4, new AvoidEntityGoal(ravager, Rabbit.class, 13.0F, 1.5D, 2.0D, EntitySelector.NO_SPECTATORS));
            }
        } catch (Exception e) {
            PetHomeMod.LOGGER.warn("could not add ai tasks to ravager");
        }
    }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {

        if (event.getType() == VillagerProfession.LIBRARIAN) {
            var tradeList=event.getTrades();
            for (int level = 1; level <= 5; level++){
                var levelTrade=tradeList.get(level);
                if (levelTrade!=null){
                    levelTrade.removeIf(i->i instanceof VillagerTrades.EnchantBookForEmeralds);
                    levelTrade.add(new EnchantBookForEmeraldsWithoutPet(5*level));
                }
            }

        }
        if (event.getType() == DIVillagerRegistry.ANIMAL_TAMER.get()) {
            List<VillagerTrades.ItemListing> level1 = new ArrayList<>();
            List<VillagerTrades.ItemListing> level2 = new ArrayList<>();
            List<VillagerTrades.ItemListing> level3 = new ArrayList<>();
            List<VillagerTrades.ItemListing> level4 = new ArrayList<>();
            List<VillagerTrades.ItemListing> level5 = new ArrayList<>();
//            level1.add(new BuyingItemTrade(Items.TROPICAL_FISH, 10, 2, 10, 2));
            level1.add(new SellingItemTrade(Items.BONE, 3, 10, 6, 4));
//            level1.add(new BuyingItemTrade(Items.HAY_BLOCK, 7, 1, 9, 1));
            level1.add(new SellingItemTrade(Items.COD, 2, 7, 6, 3));
             level1.add(new SellingRandomEnchantedBook());
//            level1.add(new SellingItemTrade(Items.EGG, 4, 2, 9, 3));
            level1.add(new SellingItemTrade(PHItemRegistry.FEATHER_ON_A_STICK.get(), 3, 1, 2, 3));
            level2.add(new SellingItemTrade(Items.TROPICAL_FISH_BUCKET, 2, 1, 6, 7));
            level2.add(new BuyingItemTrade(PHItemRegistry.COLLAR_TAG.get(), 5, 1, 12, 7));
            level2.add(new SellingItemTrade(Items.APPLE, 4, 12, 3, 7));
            level2.add(new SellingRandomEnchantedBook());
            level2.add(new SellingItemTrade(PHItemRegistry.DEED_OF_OWNERSHIP.get(), 3, 1, 2, 7));
            level3.add(new SellingItemTrade(PHItemRegistry.ROTTEN_APPLE.get(), 4, 1, 1, 10));
//            level3.add(new SellingItemTrade(Items.CARROT_ON_A_STICK, 3, 1, 2, 10));
            level3.add(new SellingRandomEnchantedBook());
//            level3.add(new SellingItemTrade(Items.LEATHER_HORSE_ARMOR, 4, 1, 3, 11));
            level3.add(new SellingItemTrade(DIBlockRegistry.DRUM.get(), 2, 3, 7, 11));
            level3.add(new SellingItemTrade(Items.TADPOLE_BUCKET, 6, 1, 4, 13));
            level3.add(new EnchantItemTrade(PHItemRegistry.COLLAR_TAG.get(), 20, 2, 8, 3, 10));
//            level4.add(new SellingItemTrade(Items.IRON_HORSE_ARMOR, 8, 1, 2, 15));
            level4.add(new SellingItemTrade(Items.AXOLOTL_BUCKET, 11, 1, 2, 15));
            level4.add(new SellingItemTrade(Items.TURTLE_EGG, 26, 1, 2, 15));
            level4.add(new EnchantItemTrade(PHItemRegistry.COLLAR_TAG.get(), 40, 3, 18, 3, 15));
//            level5.add(new SellingItemTrade(Items.GOLDEN_HORSE_ARMOR, 13, 1, 1, 18));
            level5.add(new SellingItemTrade(Items.SCUTE, 21, 1, 3, 18));
            level5.add(new EnchantItemTrade(PHItemRegistry.COLLAR_TAG.get(), 50, 4, 38, 3, 20));
            level5.add(new SellingRandomEnchantedBook());
            event.getTrades().put(1, level1);
            event.getTrades().put(2, level2);
            event.getTrades().put(3, level3);
            event.getTrades().put(4, level4);
            event.getTrades().put(5, level5);
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (TameableUtils.isTamed(event.getEntity()) && TameableUtils.getPetBedPos(event.getEntity()) != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Start event) {
        float dist = 30;
        Vec3 center = event.getExplosion().getPosition();
        Vec3 bottom = center.add(-dist, -dist, -dist);
        Vec3 top = center.add(dist, dist, dist);
        Predicate<Entity> defusal = (animal) -> TameableUtils.isTamed(animal) && TameableUtils.hasEnchant((LivingEntity) animal, DIEnchantmentRegistry.DEFUSAL);
        boolean flag = false;
        for (LivingEntity defuser : event.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(bottom, top), EntitySelector.NO_SPECTATORS.and(defusal))) {
            float level = 10 * TameableUtils.getEnchantLevel(defuser, DIEnchantmentRegistry.DEFUSAL);
            if (defuser.distanceToSqr(center) <= level * level) {
                flag = true;
                break;
            }
        }
        if (flag) {
            event.setCanceled(true);
            float pitch = 1.5F + new Random().nextFloat();
            event.getLevel().playSound(null, center.x, center.y, center.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, pitch);
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 5; i++) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 1.0F, center.z, 5, 0, 0F, 0, 0.2F);
                }
            }
        }
    }

    private static void teleportNearbyPets(Player owner, Vec3 fromPos, Vec3 toPos, Level fromLevel, Level toLevel) {
        double dist = 20;
        boolean removeAndReadd = fromLevel.dimension() != toLevel.dimension();
        Predicate<Entity> enchantedPet = (animal) -> animal instanceof Mob && TameableUtils.isPetOf(owner, animal) && TameableUtils.isValidTeleporter(owner, (Mob) animal);
        for (Mob entity : fromLevel.getEntitiesOfClass(Mob.class, new AABB(fromPos.x - dist, fromPos.y - dist, fromPos.z - dist, fromPos.x + dist, fromPos.y + dist, fromPos.z + dist), EntitySelector.NO_SPECTATORS.and(enchantedPet))) {
            if (removeAndReadd) {
                teleportingPets.add(new Triple(entity, toLevel, owner.getUUID()));
            } else {
                EntityDimensions dimensions = entity.getDimensions(entity.getPose());
                AABB suffocationBox = new AABB(-dimensions.width / 2.0F, 0, -dimensions.width / 2.0F, dimensions.width / 2.0F, dimensions.height, dimensions.width / 2.0F);
                while (!toLevel.noCollision(entity, suffocationBox.move(toPos.x, toPos.y, toPos.z)) && toPos.y < 300) {
                    toPos = toPos.add(0, 1, 0);
                }
                entity.fallDistance = 0.0F;
                entity.teleportToWithTicket(toPos.x, toPos.y, toPos.z);
                entity.setPortalCooldown();
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if (event.getEntity() instanceof Player) {
            teleportNearbyPets((Player) event.getEntity(), event.getPrev(), event.getTarget(), event.getEntity().level(), event.getEntity().level());
        }
    }

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (!event.isCanceled()) {
            if (event.getEntity().level() instanceof ServerLevel serverLevel && event.getEntity() instanceof Player) {
                MinecraftServer server = serverLevel.getServer();
                Level toLevel = server.getLevel(event.getDimension());
                if (toLevel != null) {
                    teleportNearbyPets((Player) event.getEntity(), event.getEntity().position(), event.getEntity().position(), event.getEntity().level(), toLevel);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onSetAttackTarget(LivingChangeTargetEvent event) {
        if (TameableUtils.isTamed(event.getEntity()) && event.getNewTarget() instanceof Player player && TameableUtils.isPetOf(player, event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onUpdateAnvil(AnvilUpdateEvent event) {
        if(event.getLeft().is(PHItemRegistry.COLLAR_TAG.get()) && !event.getLeft().getAllEnchantments().isEmpty() && event.getRight().is(PHItemRegistry.COLLAR_TAG.get()) && !event.getRight().getAllEnchantments().isEmpty()){

            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(event.getLeft());
            Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(event.getRight());
            boolean canCombine = true;
            int i = 0;
            for(Enchantment enchantment1 : map1.keySet()) {
                if (enchantment1 != null) {
                    int i2 = map.getOrDefault(enchantment1, 0);
                    int j2 = map1.get(enchantment1);
                    j2 = i2 == j2 ? j2 + 1 : Math.max(j2, i2);

                    for(Enchantment enchantment : map.keySet()) {
                        if (enchantment != enchantment1 && !enchantment1.isCompatibleWith(enchantment)) {
                            canCombine = false;
                            ++i;
                        }
                    }

                    if (canCombine) {
                        if (j2 > enchantment1.getMaxLevel()) {
                            j2 = enchantment1.getMaxLevel();
                        }

                        map.put(enchantment1, j2);
                        int k3 = 0;
                        switch (enchantment1.getRarity()) {
                            case COMMON:
                                k3 = 1;
                                break;
                            case UNCOMMON:
                                k3 = 2;
                                break;
                            case RARE:
                                k3 = 4;
                                break;
                            case VERY_RARE:
                                k3 = 8;
                        }
                        i += k3 * j2;
                    }
                }
            }
            event.setCost(i);
            ItemStack copy = event.getLeft().copy();
            EnchantmentHelper.setEnchantments(map, copy);
            event.setOutput(copy);
        }
    }
}
