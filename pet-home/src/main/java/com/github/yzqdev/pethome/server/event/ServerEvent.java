package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.platform.Triple;
import com.github.yzqdev.pethome.server.block.DIBlockRegistry;
import com.github.yzqdev.pethome.server.block.PetBedBlock;
import com.github.yzqdev.pethome.server.block.PetBedBlockEntity;
import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.*;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.misc.*;
import com.github.yzqdev.pethome.server.misc.trades.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.*;
import java.util.function.Predicate;

/**
 * 服务器端事件与逻辑（原 {@code server/CommonProxy}）。
 *
 * <p>Fabric 侧不再使用 Proxy 模式：所有方法都是静态的，直接由入口 / Mixin 调用。
 * 客户端专属的部分（粒子注册、渲染器注册、名牌绘制、暗影之手视觉数据、唱片机音效）
 * 已迁到 {@code client/ClientGameEvents}。</p>
 */
public class ServerEvent {

    public static final TargetingConditions ZOMBIE_TARGET = TargetingConditions.forCombat().range(32.0D);
    //list of pets to be teleported across dimensions, cleared after every tick
    public static List<Triple<Entity, ServerLevel, UUID>> teleportingPets = new ArrayList<>();

    private static final Map<Level, CollarTickTracker> COLLAR_TICK_TRACKER_MAP = new HashMap<>();

    public static void serverStart() {
        DIVillagePieceRegistry.registerHouses();
        registerVillagerTrades();
    }

    /**
     * Fabric 版直接改写 VillagerTrades.TRADES（原为 Forge 的 VillagerTradesEvent）。
     */
    private static void registerVillagerTrades() {
        // 替换图书馆员的附魔书交易，避免出售宠物附魔
        var librarianTrades = VillagerTrades.TRADES.get(VillagerProfession.LIBRARIAN);
        if (librarianTrades != null) {
            for (int level = 1; level <= 5; level++) {
                var levelTrade = librarianTrades.get(level);
                if (levelTrade != null) {
                    List<VillagerTrades.ItemListing> updated = new ArrayList<>();
                    for (VillagerTrades.ItemListing listing : levelTrade) {
                        if (!(listing instanceof VillagerTrades.EnchantBookForEmeralds)) {
                            updated.add(listing);
                        }
                    }
                    updated.add(new EnchantBookForEmeraldsWithoutPet(5 * level));
                    librarianTrades.put(level, updated.toArray(new VillagerTrades.ItemListing[0]));
                }
            }
        }

        VillagerProfession animalTamer = DIVillagerRegistry.ANIMAL_TAMER;
        List<VillagerTrades.ItemListing> level1 = new ArrayList<>();
        List<VillagerTrades.ItemListing> level2 = new ArrayList<>();
        List<VillagerTrades.ItemListing> level3 = new ArrayList<>();
        List<VillagerTrades.ItemListing> level4 = new ArrayList<>();
        List<VillagerTrades.ItemListing> level5 = new ArrayList<>();
        level1.add(new SellingItemTrade(Items.BONE, 3, 10, 6, 4));
        level1.add(new SellingItemTrade(Items.COD, 2, 7, 6, 3));
        level1.add(new SellingRandomEnchantedBook());
        level1.add(new SellingItemTrade(PHItemRegistry.FEATHER_ON_A_STICK, 3, 1, 2, 3));
        level2.add(new SellingItemTrade(Items.TROPICAL_FISH_BUCKET, 2, 1, 6, 7));
        level2.add(new BuyingItemTrade(PHItemRegistry.COLLAR_TAG, 5, 1, 12, 7));
        level2.add(new SellingItemTrade(Items.APPLE, 4, 12, 3, 7));
        level2.add(new SellingRandomEnchantedBook());
        level2.add(new SellingItemTrade(PHItemRegistry.DEED_OF_OWNERSHIP, 3, 1, 2, 7));
        level3.add(new SellingItemTrade(PHItemRegistry.ROTTEN_APPLE, 4, 1, 1, 10));
        level3.add(new SellingRandomEnchantedBook());
        level3.add(new SellingItemTrade(DIBlockRegistry.DRUM, 2, 3, 7, 11));
        level3.add(new SellingItemTrade(Items.TADPOLE_BUCKET, 6, 1, 4, 13));
        level3.add(new EnchantItemTrade(PHItemRegistry.COLLAR_TAG, 20, 2, 8, 3, 10));
        level4.add(new SellingItemTrade(Items.AXOLOTL_BUCKET, 11, 1, 2, 15));
        level4.add(new SellingItemTrade(Items.TURTLE_EGG, 26, 1, 2, 15));
        level4.add(new EnchantItemTrade(PHItemRegistry.COLLAR_TAG, 40, 3, 18, 3, 15));
        level5.add(new SellingItemTrade(Items.SCUTE, 21, 1, 3, 18));
        level5.add(new EnchantItemTrade(PHItemRegistry.COLLAR_TAG, 50, 4, 38, 3, 20));
        level5.add(new SellingRandomEnchantedBook());
        Int2ObjectMap<VillagerTrades.ItemListing[]> trades = new Int2ObjectOpenHashMap<>();
        trades.put(1, level1.toArray(new VillagerTrades.ItemListing[0]));
        trades.put(2, level2.toArray(new VillagerTrades.ItemListing[0]));
        trades.put(3, level3.toArray(new VillagerTrades.ItemListing[0]));
        trades.put(4, level4.toArray(new VillagerTrades.ItemListing[0]));
        trades.put(5, level5.toArray(new VillagerTrades.ItemListing[0]));
        VillagerTrades.TRADES.put(animalTamer, trades);
    }

    public static void onEntityJoinWorldEvent(Entity entity) {
        // 宠物罗盘：档案更新（区块加载/维度切换/宠物床复活回到世界——复活实体带着原 PetId，重新绑定 Entity UUID）
        if (entity instanceof LivingEntity living && !living.level().isClientSide() && TameableUtils.isTamed(living)) {
            PetCompassTracker.updateRecord(living);
        }
        if (entity instanceof LivingEntity living && TameableUtils.couldBeTamed(living)) {
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

    public static void onEntityLeaveWorld(Entity entity) {
        if (entity instanceof LivingEntity living) {
            if (!living.level().isClientSide() && living.isAlive() && TameableUtils.isTamed(living) && TameableUtils.shouldUnloadToLantern(living)) {
                UUID ownerUUID = TameableUtils.getOwnerUUIDOf(entity);
                String saveName = entity.hasCustomName() ? entity.getCustomName().getString() : "";
                DIWorldData data = DIWorldData.get(living.level());
                if (data != null) {
                    LanternRequest request = new LanternRequest(living.getUUID(), BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(), ownerUUID, living.blockPosition(), entity.level().dayTime(), saveName, entity.level().dimension().location().toString());
                    data.addLanternRequest(request);
                }
            }
            if (TameableUtils.couldBeTamed(living) && TameableUtils.hasEnchant(living, DIEnchantmentRegistry.HEALTH_BOOST)) {
                TameableUtils.setSafePetHealth(living, living.getHealth());
            }

        }
    }

    public static boolean canTickCollar(Entity entity){
        if(entity.level().isClientSide()){
            return true;
        }else{
            CollarTickTracker tracker = COLLAR_TICK_TRACKER_MAP.get(entity.level());
            return tracker == null || !tracker.isEntityBlocked(entity);
        }
    }

    public static void blockCollarTick(Entity entity){
        if(!entity.level().isClientSide()){
            CollarTickTracker tracker = COLLAR_TICK_TRACKER_MAP.computeIfAbsent(entity.level(), k -> new CollarTickTracker());
            tracker.addBlockedEntityTick(entity.getUUID(), 5);
        }
    }

    public static void onServerTick(Level level) {
        if (!level.isClientSide()) {
            COLLAR_TICK_TRACKER_MAP.computeIfAbsent(level, k -> new CollarTickTracker());
            CollarTickTracker tracker = COLLAR_TICK_TRACKER_MAP.get(level);
            tracker.tick();
        }
        // 宠物罗盘召回队列：全局队列只在主世界 tick 上处理（每 20 tick 一轮）
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel
                && serverLevel.dimension() == Level.OVERWORLD && serverLevel.getGameTime() % 20 == 0) {
            PetCompassTeleport.processRecallQueue(serverLevel.getServer(), serverLevel.getGameTime());
        }
        // 宠物罗盘：每 10 game tick 刷新手持罗盘 NBT（指针模式已移除）
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            for (final var triple : teleportingPets) {
                Entity entity = triple.a();
                ServerLevel endpointWorld = triple.b();
                UUID ownerUUID = triple.c();
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

    /**
     * 替代 Forge ProjectileImpactEvent，返回 true 表示跳过实体命中（继续飞行）。
     */
    public static boolean onProjectileImpactEvent(Projectile projectile, HitResult result) {
        if (result instanceof EntityHitResult entityHitResult) {
            Entity hit = entityHitResult.getEntity();
            if (projectile.getOwner() instanceof Player player) {
                if (TameableUtils.isPetOf(player, hit)) {
                    return true;
                }
            }
            if (TameableUtils.isTamed(hit)) {
                if (projectile instanceof AbstractArrow arrow) {
                    //fixes soft crash with vanilla
                    if (arrow.getPierceLevel() > 0) {
                        arrow.setPierceLevel((byte) 0);
                        arrow.remove(Entity.RemovalReason.DISCARDED);
                        return true;
                    }
                }
                if (TameableUtils.hasEnchant((LivingEntity) hit, DIEnchantmentRegistry.DEFLECTION)) {
                    float xRot = projectile.getXRot();
                    float yRot = projectile.yRotO;
                    Vec3 vec3 = projectile.position().subtract(hit.position()).normalize().scale(hit.getBbWidth() + 0.5F);
                    Vec3 vec32 = hit.position().add(vec3);
                    hit.level().addParticle(DIParticleRegistry.DEFLECTION_SHIELD, vec32.x, vec32.y, vec32.z, xRot, yRot, 0.0F);
                    projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-0.2D));
                    projectile.setYRot(yRot + 180);
                    projectile.setXRot(xRot + 180);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 替代 Forge ItemExpireEvent；返回 true 表示本次过期被转换为腐烂苹果（取消过期）。
     */
    public static boolean onItemDespawnEvent(ItemEntity itemEntity) {
        if (itemEntity.getItem().getItem() == Items.APPLE && PetHomeMod.CONFIG.rottenApple.get()) {
            if (new Random().nextFloat() < 0.1F * itemEntity.getItem().getCount()) {
                itemEntity.getItem().shrink(1);
                itemEntity.age = 0;
                ItemEntity rotten = new ItemEntity(itemEntity.level(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), new ItemStack(PHItemRegistry.ROTTEN_APPLE));
                itemEntity.level().addFreshEntity(rotten);
                return true;
            }
        }
        return false;
    }

    /**
     * 替代 Forge LivingHurtEvent（actuallyHurt 之前触发）。
     * 返回修改后的伤害值；返回 0 表示取消伤害。
     */
    public static float onLivingHurt(LivingEntity entity, net.minecraft.world.damagesource.DamageSource source, float amount) {
        validateHealth(entity);
        if (Float.isNaN(amount)) {
            return 0.0F;
        }
        return amount;
    }

    public static void validateHealth(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        float health = entity.getHealth();
        if (Float.isNaN(health) || health < 0.0F) {
            PetHomeMod.LOGGER.error(entity.getName());
            entity.setHealth(0.0F);
        }
    }

    /**
     * 替代 Forge LivingDamageEvent 中的伤害修正（IMMATURITY_CURSE）。
     */
    public static float modifyDamage(LivingEntity entity, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity pet && TameableUtils.isTamed(source.getEntity())) {
            if (TameableUtils.hasEnchant(pet, DIEnchantmentRegistry.IMMATURITY_CURSE)) {
                return (float) Math.ceil(amount * 0.7F);
            }
        }
        return amount;
    }

    /**
     * 替代 Forge LivingDeathEvent。
     */
    public static void onLivingDie(LivingEntity entity, net.minecraft.world.damagesource.DamageSource source) {
        validateHealth(entity);
        if (TameableUtils.isTamed(entity) && !TameableUtils.isZombiePet(entity)) {

            BlockPos bedPos = TameableUtils.getPetBedPos(entity);
            if (bedPos != null) {
                CompoundTag data = new CompoundTag();
                entity.addAdditionalSaveData(data);
                String saveName = entity.hasCustomName() ? entity.getCustomName().getString() : "";
                RespawnRequest request = new RespawnRequest(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(), TameableUtils.getPetBedDimension(entity), data, bedPos, entity.level().dayTime(), saveName);
                DIWorldData worldData = DIWorldData.get(entity.level());
                if (worldData != null) {
                    worldData.addRespawnRequest(request);
                }
            }
            if (!(entity instanceof TamableAnimal)) {
                Entity owner = TameableUtils.getOwnerOf(entity);
                if (!entity.level().isClientSide() && entity.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && owner instanceof ServerPlayer) {
                    owner.sendSystemMessage(entity.getCombatTracker().getDeathMessage());
                }
            }

            // 宠物罗盘：死亡档案保留，仅标记待宠物床复活（ 不因 Entity UUID 消失而删档）
            PetCompassTracker.onPetDeath(entity);
            if (entity instanceof Mob mob && entity.level().getDifficulty() != Difficulty.PEACEFUL && TameableUtils.hasEnchant(mob, DIEnchantmentRegistry.UNDEAD_CURSE)) {
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
        }
    }

    /**
     * 替代 Forge EntityMountEvent：返回 true 表示阻止乘客从巨型泡泡上下来。
     */
    public static boolean onEntityMount(Entity passenger) {
        return passenger.getVehicle() instanceof GiantBubbleEntity bubble && bubble.isAlive();
    }

    /**
     * 替代 Forge BlockEvent.BreakEvent。
     */
    public static void onBlockBreak(ServerLevel level, Player player, BlockPos pos,  BlockState state) {
        if (state.getBlock() instanceof PetBedBlock) {
            if (level.getBlockEntity(pos) instanceof PetBedBlockEntity entity1) {
                entity1.removeAllRequestsFor(player);
                entity1.resetBedsForNearbyPets();
            }
        }
    }

    /**
     * 替代 Forge MobSpawnEvent.FinalizeSpawn（掠夺者惧怕兔子）。
     */
    public static void onFinalizeSpawn(Entity entity) {
        try {
            if (entity != null && entity instanceof Ravager ravager && PetHomeMod.CONFIG.rabbitsScareRavagers.get()) {
                ravager.goalSelector.addGoal(4, new AvoidEntityGoal(ravager, Rabbit.class, 13.0F, 1.5D, 2.0D, EntitySelector.NO_SPECTATORS));
            }
        } catch (Exception e) {
            PetHomeMod.LOGGER.warn("could not add ai tasks to ravager");
        }
    }

    /**
     * 替代 Forge LivingDropsEvent：返回 true 表示取消掉落（宠物有床时由 mixin 取消）。
     */
    public static boolean shouldCancelLivingDrops(LivingEntity entity) {
        return TameableUtils.isTamed(entity) && TameableUtils.getPetBedPos(entity) != null;
    }

    /**
     * 替代 Forge ExplosionEvent.Start：返回 true 表示取消爆炸（偏移结界附魔）。
     */
    public static boolean onExplosionStart(net.minecraft.world.level.Level level, net.minecraft.world.level.Explosion explosion) {
        float dist = 30;
        Vec3 center = new Vec3(explosion.x, explosion.y, explosion.z);
        Vec3 bottom = center.add(-dist, -dist, -dist);
        Vec3 top = center.add(dist, dist, dist);
        Predicate<Entity> defusal = (animal) -> TameableUtils.isTamed(animal) && TameableUtils.hasEnchant((LivingEntity) animal, DIEnchantmentRegistry.DEFUSAL);
        boolean flag = false;
        for (LivingEntity defuser : level.getEntitiesOfClass(LivingEntity.class, new AABB(bottom, top), EntitySelector.NO_SPECTATORS.and(defusal))) {
            float level2 = 10 * TameableUtils.getEnchantLevel(defuser, DIEnchantmentRegistry.DEFUSAL);
            if (defuser.distanceToSqr(center) <= level2 * level2) {
                flag = true;
                break;
            }
        }
        if (flag) {
            float pitch = 1.5F + new Random().nextFloat();
            level.playSound(null, center.x, center.y, center.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, pitch);
            if (level instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 5; i++) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 1.0F, center.z, 5, 0, 0F, 0, 0.2F);
                }
            }
        }
        return flag;
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

    /**
     * 替代 Forge EntityTravelToDimensionEvent：跨维度时把"拴魂"宠物一起带走。
     */
    public static void onEntityTravelToDimension(Player player, Level toLevel) {
        teleportNearbyPets(player, player.position(), player.position(), player.level(), toLevel);
    }

    /**
     * 替代 Forge EntityTeleportEvent：同维度传送时把"拴魂"宠物一起带走。
     */
    public static void onEntityTeleport(Player player, Vec3 toPos) {
        teleportNearbyPets(player, player.position(), toPos, player.level(), player.level());
    }

    /**
     * 替代 Forge LivingChangeTargetEvent：返回 true 表示取消目标切换。
     */
    public static boolean onSetAttackTarget(Mob mob, LivingEntity newTarget) {
        return TameableUtils.isTamed(mob) && newTarget instanceof Player player && TameableUtils.isPetOf(player, mob);
    }

    /**
     * 替代 Forge AnvilUpdateEvent：铁砧合并两个鞍带项圈（在 AnvilMenuMixin 的 createResult 尾部调用）。
     * 返回合并后的结果，未处理返回 null。
     */
    public static Pair<ItemStack, Integer> onAnvilUpdate(ItemStack left, ItemStack right) {
        if (left.is(PHItemRegistry.COLLAR_TAG) && !EnchantmentHelper.getEnchantments(left).isEmpty() && right.is(PHItemRegistry.COLLAR_TAG) && !EnchantmentHelper.getEnchantments(right).isEmpty()) {

            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(left);
            Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(right);
            boolean canCombine = true;
            int i = 0;
            for (Enchantment enchantment1 : map1.keySet()) {
                if (enchantment1 != null) {
                    int i2 = map.getOrDefault(enchantment1, 0);
                    int j2 = map1.get(enchantment1);
                    j2 = i2 == j2 ? j2 + 1 : Math.max(j2, i2);

                    for (Enchantment enchantment : map.keySet()) {
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
            ItemStack copy = left.copy();
            EnchantmentHelper.setEnchantments(map, copy);
            return new Pair<>(copy, i);
        }
        return null;
    }

    /**
     * 附魔 desc 提示行（客户端展示，由 ClientModEvents.registerItemTooltips 接入 Fabric 的
     * {@code ItemTooltipCallback}）。
     *
     * <p>对齐 1.21：本方法位于 {@code ServerEvent}（原来在 client/ItemTooltipHandler）；
     * 若已安装 Enchantment Descriptions（enchdesc）模组则交由它统一展示，这里跳过以免重复。</p>
     */
    public static void onItemTooltip(ItemStack stack, List<Component> tooltip) {
        if (FabricLoader.getInstance().isModLoaded("enchdesc")) {
            return;
        }
        ListTag enchantments;
        if (stack.is(Items.ENCHANTED_BOOK)) {
            enchantments = EnchantedBookItem.getEnchantments(stack);
        } else if (stack.hasTag() && stack.getTag().contains("Enchantments", CompoundTag.TAG_LIST)) {
            enchantments = stack.getTag().getList("Enchantments", CompoundTag.TAG_COMPOUND);
        } else {
            return;
        }

        for (int i = 0; i < enchantments.size(); i++) {
            CompoundTag enchantmentTag = enchantments.getCompound(i);
            ResourceLocation id = EnchantmentHelper.getEnchantmentId(enchantmentTag);
            if (id != null && PetHomeMod.MODID.equals(id.getNamespace())) {
                tooltip.add(Component.translatable("enchantment." + PetHomeMod.MODID + "." + id.getPath() + ".desc").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}
