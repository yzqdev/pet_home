package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.ModEnchantments;
import com.github.yzqdev.pethome.platform.ServerRef;
import com.github.yzqdev.pethome.platform.Triple;
import com.github.yzqdev.pethome.server.block.PetBedBlockEntity;
import com.github.yzqdev.pethome.server.entity.PHVillagerRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.misc.CollarTickTracker;
import com.github.yzqdev.pethome.server.misc.PetCompassTeleport;
import com.github.yzqdev.pethome.server.misc.PetCompassTracker;
import com.github.yzqdev.pethome.server.misc.PHWorldData;
import com.github.yzqdev.pethome.server.misc.LanternRequest;
import com.github.yzqdev.pethome.server.misc.RespawnRequest;
import com.github.yzqdev.pethome.util.TameableUtils;
import com.github.yzqdev.pethome.worldgen.VillageHouseManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.loader.api.FabricLoader;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author yzqde
 * @date time 2025/1/9 9:47
 * @modified By:
 */

public class ServerEvent {
    public static void onLivingDrops(LivingEntity entity) {
        // handled by LivingEntityDropMixin, kept for reference
    }

    public static void serverStart(MinecraftServer server) {
        RegistryAccess registryAccess = server.registryAccess();
        VillageHouseManager.addAllHouses(registryAccess);
    }


    public static List<Triple<Entity, ServerLevel, UUID>> teleportingPets = new ArrayList<>();

    public static void onWorldTick(ServerPlayer serverPlayer, ServerLevel world) {

        AABB playerArea = serverPlayer.getBoundingBox().inflate(10.0);
        List<net.minecraft.world.entity.TamableAnimal> pets = world.getEntitiesOfClass(net.minecraft.world.entity.TamableAnimal.class, playerArea,
                pet -> pet.isTame() && !pet.isOrderedToSit()&&pet.getTarget()==null && TameableUtils.hasEnchant(pet, ModEnchantments.XP_Transfer));

        for (net.minecraft.world.entity.TamableAnimal pet : pets) {

            AABB searchBox = pet.getBoundingBox().inflate(10.0);
            List<net.minecraft.world.entity.ExperienceOrb> orbs = world.getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class, searchBox);

            if (orbs.isEmpty()) {
                continue;
            }


            if (!(pet.getOwner() instanceof Player player)) {
                continue;
            }


            for (net.minecraft.world.entity.ExperienceOrb orb : orbs) {

                Vec3 targetPos = new Vec3(pet.getX(), pet.getY() + (double) pet.getEyeHeight() / 2.0D, pet.getZ());
                Vec3 vec3 = targetPos.subtract(orb.position());
                double distSqr = vec3.lengthSqr();


                if (distSqr < 2.0D) {
                    player.giveExperiencePoints(orb.getValue());
                    orb.discard();
                    continue;
                }


                if (distSqr < 64.0D) {
                    double d1 = 1.0D - Math.sqrt(distSqr) / 8.0D;
                    // 给经验球施加向宠物的加速度
                    orb.setDeltaMovement(orb.getDeltaMovement().add(vec3.normalize().scale(d1 * d1 * 0.5D)));
                }
            }


            net.minecraft.world.entity.ExperienceOrb closestOrb = orbs.getFirst();
            pet.getLookControl().setLookAt(closestOrb, 30.0F, 30.0F);
            pet.getNavigation().moveTo(closestOrb, 1.2D);
        }
    }

    private static final Map<Level, CollarTickTracker> COLLAR_TICK_TRACKER_MAP = new HashMap<>();

    /** 项圈刚被取下/装备后短时间内不再 tick（自 1.20 移植） */
    public static boolean canTickCollar(Entity entity) {
        if (entity.level().isClientSide()) {
            return true;
        }
        CollarTickTracker tracker = COLLAR_TICK_TRACKER_MAP.get(entity.level());
        return tracker == null || !tracker.isEntityBlocked(entity);
    }

    public static void blockCollarTick(Entity entity) {
        if (!entity.level().isClientSide()) {
            CollarTickTracker tracker = COLLAR_TICK_TRACKER_MAP.computeIfAbsent(entity.level(), k -> new CollarTickTracker());
            tracker.addBlockedEntityTick(entity.getUUID(), 5);
        }
    }

    public static void onServerTick(Level level) {
        if (!level.isClientSide()) {
            CollarTickTracker tracker = COLLAR_TICK_TRACKER_MAP.computeIfAbsent(level, k -> new CollarTickTracker());
            tracker.tick();
        }
        // 宠物罗盘召回队列：全局队列只在主世界 tick 上处理（每 20 tick 一轮）
        if (!level.isClientSide() && level.dimension() == Level.OVERWORLD && level.getGameTime() % 20 == 0) {
            PetCompassTeleport.processRecallQueue(level.getServer(), level.getGameTime());
        }
        if (level.getGameTime() % 10 != 0) return;
        if (!level.isClientSide() && level instanceof ServerLevel) {
            for (var player : level.players()) {
               if (player instanceof ServerPlayer serverPlayer){
                   onWorldTick(serverPlayer,(ServerLevel) level);
               }
            }
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
                        AABB suffocationBox = new AABB(-dimensions.width() / 2.0F, 0, -dimensions.width() / 2.0F, dimensions.width() / 2.0F, dimensions.height(), dimensions.width() / 2.0F);
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

    public static void onEntityTravelToDimension(Player player, Level toLevel) {
        if (player.level() instanceof ServerLevel serverLevel) {
            teleportNearbyPets(player, player.position(), player.position(), player.level(), toLevel);
        }
    }

    public static void onEntityTeleport(Player player, Vec3 toPos) {
        teleportNearbyPets(player, player.position(), toPos, player.level(), player.level());
    }

    private static void teleportNearbyPets(Player owner, Vec3 fromPos, Vec3 toPos, Level fromLevel, Level toLevel) {
        double dist = 20;
        boolean removeAndReadd = fromLevel.dimension() != toLevel.dimension();
        Predicate<Entity> enchantedPet = (animal) -> animal instanceof net.minecraft.world.entity.Mob && TameableUtils.isPetOf(owner, animal) && TameableUtils.isValidTeleporter(owner, (net.minecraft.world.entity.Mob) animal);
        for (net.minecraft.world.entity.Mob entity : fromLevel.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, new AABB(fromPos.x - dist, fromPos.y - dist, fromPos.z - dist, fromPos.x + dist, fromPos.y + dist, fromPos.z + dist), EntitySelector.NO_SPECTATORS.and(enchantedPet))) {
            if (removeAndReadd) {
                teleportingPets.add(new Triple(entity, (ServerLevel) toLevel, owner.getUUID()));
            } else {
                EntityDimensions dimensions = entity.getDimensions(entity.getPose());
                AABB suffocationBox = new AABB(-dimensions.width() / 2.0F, 0, -dimensions.width() / 2.0F, dimensions.width() / 2.0F, dimensions.height(), dimensions.width() / 2.0F);
                while (!toLevel.noCollision(entity, suffocationBox.move(toPos.x, toPos.y, toPos.z)) && toPos.y < 300) {
                    toPos = toPos.add(0, 1, 0);
                }
                entity.fallDistance = 0.0F;
                ChunkPos chunkpos = new ChunkPos(BlockPos.containing(toPos.x, toPos.y, toPos.z));
                ((ServerLevel) entity.level()).getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 0, entity.getId());
                entity.level().getChunk(chunkpos.x, chunkpos.z);
                entity.teleportTo(toPos.x, toPos.y, toPos.z);
                entity.setPortalCooldown();
            }
        }
    }

    /**
     * ProjectileImpactEvent 的替代，由 ProjectileImpactMixin 调用。
     *
     * @return true 取消本次命中（弹射物按原逻辑继续飞行）
     */
    public static boolean onProjectileImpactEvent(net.minecraft.world.entity.projectile.Projectile projectile, HitResult rayTraceResult) {
        if (rayTraceResult instanceof EntityHitResult) {
            Entity hit = ((EntityHitResult) rayTraceResult).getEntity();
            if (projectile.getOwner() instanceof Player player) {
                if (TameableUtils.isPetOf(player, hit)) {
                    return true;
                }
            }
            if (TameableUtils.isTamed(hit) && hit instanceof LivingEntity livingHit) {
                if (projectile instanceof AbstractArrow arrow) {
                    //fixes soft crash with vanilla
                    if (arrow.getPierceLevel() > 0) {
                        arrow.setPierceLevel((byte) 0);
                        arrow.remove(Entity.RemovalReason.DISCARDED);
                        return true;
                    }
                }
                if (TameableUtils.hasEnchant(livingHit, ModEnchantments.DEFLECTION)) {
                    float xRot = projectile.getXRot();
                    float yRot = projectile.yRotO;
                    Vec3 vec3 = projectile.position().subtract(hit.position()).normalize().scale(hit.getBbWidth() + 0.5F);
                    Vec3 vec32 = hit.position().add(vec3);
                    hit.level().addParticle(com.github.yzqdev.pethome.server.misc.PHParticleRegistry.DEFLECTION_SHIELD, vec32.x, vec32.y, vec32.z, xRot, yRot, 0.0F);
                    projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-0.2D));
                    projectile.setYRot(yRot + 180);
                    projectile.setXRot(xRot + 180);
                    return true;
                }
            }
        }
        return false;
    }

    public static void onEntityMount(Entity entityBeingMounted, boolean isDismounting) {
        // handled by EntityRemoveVehicleMixin; kept for reference
    }

    public static void onEntityJoinWorldEvent(Entity entity) {
        // 宠物罗盘：档案更新（区块加载/维度切换/宠物床复活回到世界——复活实体带着原 PetId，重新绑定 Entity UUID）
        if (entity instanceof LivingEntity living && !living.level().isClientSide() && TameableUtils.isTamed(living)) {
            PetCompassTracker.updateRecord(living);
        }
        if (entity instanceof LivingEntity living && TameableUtils.couldBeTamed(living)) {
            if (TameableUtils.hasEnchant(living, ModEnchantments.HEALTH_BOOST)) {
                living.setHealth((float) Math.max(living.getHealth(), TameableUtils.getSafePetHealth(living)));
            }
            if (living.isAlive() && TameableUtils.isTamed(living)) {
                PHWorldData data = PHWorldData.get(living.level());
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
                PHWorldData data = PHWorldData.get(living.level());
                if (data != null) {
                    LanternRequest request = new LanternRequest(living.getUUID(), BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(), ownerUUID, living.blockPosition(), entity.level().dayTime(), saveName);
                    data.addLanternRequest(request);
                }
            }
            if (TameableUtils.couldBeTamed(living) && TameableUtils.hasEnchant(living, ModEnchantments.HEALTH_BOOST)) {
                TameableUtils.setSafePetHealth(living, living.getHealth());
            }

        }
    }

    public static void onLivingDie(Entity entity) {
        LivingEntity dead = (LivingEntity) entity;
        if (TameableUtils.isTamed(dead)) {

            BlockPos bedPos = TameableUtils.getPetBedPos(dead);
            if (bedPos != null) {
                CompoundTag data = new CompoundTag();
                dead.addAdditionalSaveData(data);
                String saveName = dead.hasCustomName() ? dead.getCustomName().getString() : "";
                RespawnRequest request = new RespawnRequest(BuiltInRegistries.ENTITY_TYPE.getKey(dead.getType()).toString(), TameableUtils.getPetBedDimension(dead), data, bedPos, dead.level().dayTime(), saveName);
                PHWorldData worldData = PHWorldData.get(dead.level());
                if (worldData != null) {
                    worldData.addRespawnRequest(request);
                }
            }
            if (!(dead instanceof net.minecraft.world.entity.TamableAnimal)) {
                Entity owner = TameableUtils.getOwnerOf(dead);
                if (!dead.level().isClientSide && dead.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && owner instanceof ServerPlayer) {
                    owner.sendSystemMessage(dead.getCombatTracker().getDeathMessage());
                }
            }

            // 宠物罗盘：死亡档案保留，仅标记待宠物床复活（ 不因 Entity UUID 消失而删档）
            PetCompassTracker.onPetDeath(dead);

        }
    }


    public static boolean onExplosionStart(net.minecraft.world.level.Explosion explosion) {
        float dist = 30;
        Vec3 center = explosion.center();
        Vec3 bottom = center.add(-dist, -dist, -dist);
        Vec3 top = center.add(dist, dist, dist);
        Predicate<Entity> defusal = (animal) -> TameableUtils.isTamed(animal) && TameableUtils.hasEnchant((LivingEntity) animal, ModEnchantments.DEFUSAL);
        boolean flag = false;
        for (LivingEntity defuser : explosion.level.getEntitiesOfClass(LivingEntity.class, new AABB(bottom, top), EntitySelector.NO_SPECTATORS.and(defusal))) {
            float level = 10 * TameableUtils.getEnchantLevel(defuser, ModEnchantments.DEFUSAL);
            if (defuser.distanceToSqr(center) <= level * level) {
                flag = true;
                break;
            }
        }
        if (flag) {
            float pitch = 1.5F + new Random().nextFloat();
            explosion.level.playSound(null, center.x, center.y, center.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, pitch);
            if (explosion.level instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 5; i++) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 1.0F, center.z, 5, 0, 0F, 0, 0.2F);
                }
            }
        }
        return flag;
    }

    public static void onBlockBreak(Level level, Player player, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof PetBedBlockEntity petBedBlockEntity) {
            petBedBlockEntity.removeAllRequestsFor(player);
            petBedBlockEntity.resetBedsForNearbyPets();
        }
    }


    public static boolean onItemDespawnEvent(ItemEntity itemEntity) {
        if (itemEntity.getItem().getItem() == Items.APPLE && PetHomeConfig.rottenApple) {
            if (new Random().nextFloat() < 0.1F * itemEntity.getItem().getCount()) {
                itemEntity.getItem().shrink(1);
                // extraLife(10) 的等效实现
                itemEntity.age = 6000 - 10;
                ItemEntity rotten = new ItemEntity(itemEntity.level(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), new ItemStack(PHItemRegistry.ROTTEN_APPLE));
                itemEntity.level().addFreshEntity(rotten);
                return true;
            }
        }
        return false;
    }


    /**
     * 带实体的捕网落地后发光、
     * 无敌且不会掉出世界
     */
    public static void onNetItemEntityTick(ItemEntity itemEntity) {
        if (itemEntity.getItem().getItem() instanceof com.github.yzqdev.pethome.server.item.NetItem
                && com.github.yzqdev.pethome.server.item.NetItem.containsEntity(itemEntity.getItem())) {
            if (!itemEntity.isCurrentlyGlowing()) {
                itemEntity.setGlowingTag(true);
            }
            if (!itemEntity.isInvulnerable()) {
                itemEntity.setInvulnerable(true);
            }
            Vec3 position = itemEntity.position();
            int minY = itemEntity.level().getMinBuildHeight();
            if (position.y < minY) {
                itemEntity.setNoGravity(true);
                itemEntity.setDeltaMovement(Vec3.ZERO);
                itemEntity.setPos(position.x, minY, position.z);
            }
        }
    }

    @FunctionalInterface
    public interface TradeRegistrar {
        void register(VillagerProfession profession, int level, Consumer<List<VillagerTrades.ItemListing>> factories);
    }

    public static void registerVillagerTrades(TradeRegistrar registrar) {

        List<VillagerTrades.ItemListing> level1 = new ArrayList<>();
        List<VillagerTrades.ItemListing> level2 = new ArrayList<>();
        List<VillagerTrades.ItemListing> level3 = new ArrayList<>();
        List<VillagerTrades.ItemListing> level4 = new ArrayList<>();
        List<VillagerTrades.ItemListing> level5 = new ArrayList<>();
        level1.add(new com.github.yzqdev.pethome.server.misc.trades.SellingItemTrade(Items.BONE, 3, 6, 6, 1));
        level1.add(new com.github.yzqdev.pethome.server.misc.trades.SellingItemTrade(Items.EGG, 3, 6, 6, 1));
        level1.add(new com.github.yzqdev.pethome.server.misc.trades.SellingItemTrade(Items.COD, 2, 6, 1));
        level1.add(new com.github.yzqdev.pethome.server.misc.trades.SellingRandomEnchantedBook(1));
        level2.add(new com.github.yzqdev.pethome.server.misc.trades.SellingItemTrade(Items.TROPICAL_FISH_BUCKET, 2, 1, 6, 7));
        level2.add(new com.github.yzqdev.pethome.server.misc.trades.BuyingItemTrade(Items.CARROT, 5, 1, 12, 7));
        level2.add(new com.github.yzqdev.pethome.server.misc.trades.BuyingItemTrade(Items.STICK, 20, 1, 12, 6));
        level2.add(new com.github.yzqdev.pethome.server.misc.trades.SellingItemTrade(Items.APPLE, 4, 12, 3, 7));
        level2.add(new com.github.yzqdev.pethome.server.misc.trades.SellingRandomEnchantedBook(5));
        level3.add(new com.github.yzqdev.pethome.server.misc.trades.SellingItemTrade(PHItemRegistry.ROTTEN_APPLE, 4, 1, 1, 10));
        level3.add(new com.github.yzqdev.pethome.server.misc.trades.SellingRandomEnchantedBook(10));
        level3.add(new com.github.yzqdev.pethome.server.misc.trades.SellingItemTrade(Items.TADPOLE_BUCKET, 6, 1, 4, 13));
        level3.add(new com.github.yzqdev.pethome.server.misc.trades.SellingItemTrade(PHItemRegistry.COLLAR_TAG, 4, 1, 6, 6));
        level3.add(new com.github.yzqdev.pethome.server.misc.trades.EnchantItemTrade(PHItemRegistry.COLLAR_TAG, 20, 2, 8, 3, 10));
        level4.add(new com.github.yzqdev.pethome.server.misc.trades.SellingItemTrade(Items.AXOLOTL_BUCKET, 11, 1, 2, 15));
        level4.add(new com.github.yzqdev.pethome.server.misc.trades.SellingItemTrade(Items.TURTLE_EGG, 26, 1, 2, 15));
        level4.add(new com.github.yzqdev.pethome.server.misc.trades.SellingRandomEnchantedBook(15));
        level4.add(new com.github.yzqdev.pethome.server.misc.trades.EnchantItemTrade(PHItemRegistry.COLLAR_TAG, 40, 3, 18, 3, 15));
        level5.add(new com.github.yzqdev.pethome.server.misc.trades.SellingItemTrade(Items.GOLDEN_CARROT, 6, 1, 6, 10));
        level5.add(new com.github.yzqdev.pethome.server.misc.trades.SellingItemTrade(Items.GOLDEN_APPLE, 10, 1, 6, 10));
        level5.add(new com.github.yzqdev.pethome.server.misc.trades.SellingItemTrade(PHItemRegistry.COLLAR_TAG, 6, 1, 6, 10));
        level5.add(new com.github.yzqdev.pethome.server.misc.trades.EnchantItemTrade(PHItemRegistry.COLLAR_TAG, 50, 4, 38, 3, 20));
        level5.add(new com.github.yzqdev.pethome.server.misc.trades.SellingRandomEnchantedBook(15));
        VillagerProfession animalTamer = PHVillagerRegistry.ANIMAL_TAMER;
        registrar.register(animalTamer, 1, list -> list.addAll(level1));
        registrar.register(animalTamer, 2, list -> list.addAll(level2));
        registrar.register(animalTamer, 3, list -> list.addAll(level3));
        registrar.register(animalTamer, 4, list -> list.addAll(level4));
        registrar.register(animalTamer, 5, list -> list.addAll(level5));
    }

    private static final String[] KEY_TYPES = {"desc", "description", "info"};

    private static MutableComponent getDescription(String baseKey, int level) {
        for (String keyType : KEY_TYPES) {
            String key = baseKey + keyType;
            if (I18n.exists(key)) {
                return Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY);
            }
            key = key + "." + level;
            if (I18n.exists(key)) {
                return Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY);
            }
        }
        return null;
    }

    private static MutableComponent getDescription(Holder<Enchantment> enchantment, ResourceLocation id, int level) {
        MutableComponent description = getDescription("enchantment." + id.getNamespace() + "." + id.getPath() + ".", level);
        if (description == null && enchantment.value().description().getContents() instanceof TranslatableContents translatable) {
            description = getDescription(translatable.getKey() + ".", level);
        }
        return description;
    }

    /**
     * 显示附魔描述
     */
    public static void onItemTooltip(ItemStack stack, List<Component> tooltip) {
        if (!FabricLoader.getInstance().isModLoaded("enchdesc") && !stack.isEmpty() && stack.getItem() instanceof EnchantedBookItem) {
            var enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
            if (enchantments != null && !enchantments.isEmpty()) {
                for (Holder<Enchantment> enchantmentHolder : enchantments.keySet()) {
                    enchantmentHolder.unwrapKey().ifPresent(enchantmentResourceKey -> {
                        if (enchantmentResourceKey.location().getNamespace().contains(PetHomeMod.MODID)) {
                            final MutableComponent description = getDescription(enchantmentHolder, enchantmentHolder.unwrapKey().orElseThrow().location(), enchantmentHolder.value().getMaxLevel());
                            if (description != null) {
                                tooltip.add(description);
                            }
                        }
                    });
                }
            }

        }
    }
}
