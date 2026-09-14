package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.ClientGameEvents;
import com.github.yzqdev.pethome.datagen.ModEnchantments;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.block.PetBedBlock;
import com.github.yzqdev.pethome.server.block.PetBedBlockEntity;
import com.github.yzqdev.pethome.server.entity.*;
import com.github.yzqdev.pethome.server.item.NetItem;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.item.Type;
import com.github.yzqdev.pethome.server.misc.*;
import com.github.yzqdev.pethome.server.misc.trades.BuyingItemTrade;
import com.github.yzqdev.pethome.server.misc.trades.EnchantItemTrade;
import com.github.yzqdev.pethome.server.misc.trades.SellingItemTrade;
import com.github.yzqdev.pethome.server.misc.trades.SellingRandomEnchantedBook;
import com.github.yzqdev.pethome.util.FriendlyFireCommon;
import com.github.yzqdev.pethome.util.LivingUtils;
import com.github.yzqdev.pethome.util.TameableUtils;
import com.github.yzqdev.pethome.worldgen.VillageHouseManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import org.antlr.v4.runtime.misc.Triple;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author yzqde
 * @date time 2025/1/9 9:47
 * @modified By:
 */

@EventBusSubscriber(modid = PetHomeMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerEvent {
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (TameableUtils.isTamed(event.getEntity()) && TameableUtils.getPetBedPos(event.getEntity()) != null) {

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void serverStart(ServerAboutToStartEvent event) {
        RegistryAccess registryAccess = event.getServer().registryAccess();
        VillageHouseManager.addAllHouses(registryAccess);
    }


    public static List<Triple<Entity, ServerLevel, UUID>> teleportingPets = new ArrayList<>();

    public static void onWorldTick(ServerPlayer serverPlayer, ServerLevel world) {

        AABB playerArea = serverPlayer.getBoundingBox().inflate(10.0);
        List<TamableAnimal> pets = world.getEntitiesOfClass(TamableAnimal.class, playerArea,
                pet -> pet.isTame() && !pet.isOrderedToSit()&&pet.getTarget()==null && TameableUtils.hasEnchant(pet, ModEnchantments.XP_Transfer));

        for (TamableAnimal pet : pets) {

            AABB searchBox = pet.getBoundingBox().inflate(10.0);
            List<ExperienceOrb> orbs = world.getEntitiesOfClass(ExperienceOrb.class, searchBox);

            if (orbs.isEmpty()) {
                continue;
            }


            if (!(pet.getOwner() instanceof Player player)) {
                continue;
            }


            for (ExperienceOrb orb : orbs) {

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


            ExperienceOrb closestOrb = orbs.getFirst();
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



        @SubscribeEvent
    public static void onServerTick(LevelTickEvent.Post tick) {
        if (!tick.getLevel().isClientSide()) {
            CollarTickTracker tracker = COLLAR_TICK_TRACKER_MAP.computeIfAbsent(tick.getLevel(), k -> new CollarTickTracker());
            tracker.tick();
        }
        if (tick.getLevel().getGameTime() % 10 != 0) return;
        if (!tick.getLevel().isClientSide() && tick.getLevel() instanceof ServerLevel) {
            for (var player : tick.getLevel().players()) {
               if (player instanceof ServerPlayer serverPlayer){
                   onWorldTick(serverPlayer,(ServerLevel) tick.getLevel());
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
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if (event.getEntity() instanceof Player) {
            teleportNearbyPets((Player) event.getEntity(), event.getPrev(), event.getTarget(), event.getEntity().level(), event.getEntity().level());
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

    @SubscribeEvent
    public static void onProjectileImpactEvent(ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult) {
            Entity hit = ((EntityHitResult) event.getRayTraceResult()).getEntity();
            if (event.getProjectile().getOwner() instanceof Player) {
                Player player = (Player) event.getProjectile().getOwner();
                if (TameableUtils.isPetOf(player, hit)) {
                    event.setCanceled(true);
                }
            }
            if (TameableUtils.isTamed(hit)) {
                if (event.getEntity() instanceof AbstractArrow arrow) {
                    //fixes soft crash with vanilla
                    if (arrow.getPierceLevel() > 0) {

                        arrow.setPierceLevel((byte) 0);
                        arrow.remove(Entity.RemovalReason.DISCARDED);
                        event.setCanceled(true);
                        return;
                    }
                }
                if (TameableUtils.hasEnchant((LivingEntity) hit, ModEnchantments.DEFLECTION)) {
                    event.setCanceled(true);
                    float xRot = event.getProjectile().getXRot();
                    float yRot = event.getProjectile().yRotO;
                    Vec3 vec3 = event.getProjectile().position().subtract(hit.position()).normalize().scale(hit.getBbWidth() + 0.5F);
                    Vec3 vec32 = hit.position().add(vec3);
                    hit.level().addParticle(PHParticleRegistry.DEFLECTION_SHIELD.get(), vec32.x, vec32.y, vec32.z, xRot, yRot, 0.0F);
                    event.getProjectile().setDeltaMovement(event.getProjectile().getDeltaMovement().scale(-0.2D));
                    event.getProjectile().setYRot(yRot + 180);
                    event.getProjectile().setXRot(xRot + 180);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (event.getEntityBeingMounted() instanceof GiantBubbleEntity && event.isDismounting() && event.getEntityBeingMounted().isAlive()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorldEvent(EntityJoinLevelEvent event) {


        if (event.getEntity() instanceof LivingEntity living && TameableUtils.couldBeTamed(living)) {
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

    @SubscribeEvent
    public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            if (!living.level().isClientSide() && living.isAlive() && TameableUtils.isTamed(living) && TameableUtils.shouldUnloadToLantern(living)) {
                UUID ownerUUID = TameableUtils.getOwnerUUIDOf(event.getEntity());
                String saveName = event.getEntity().hasCustomName() ? event.getEntity().getCustomName().getString() : "";
                PHWorldData data = PHWorldData.get(living.level());
                if (data != null) {
                    LanternRequest request = new LanternRequest(living.getUUID(), BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString(), ownerUUID, living.blockPosition(), event.getEntity().level().dayTime(), saveName);
                    data.addLanternRequest(request);
                }
            }
            if (TameableUtils.couldBeTamed(living) && TameableUtils.hasEnchant(living, ModEnchantments.HEALTH_BOOST)) {
                TameableUtils.setSafePetHealth(living, living.getHealth());
            }

        }
    }

    @SubscribeEvent
    public static void onLivingDie(LivingDeathEvent event) {
        if (TameableUtils.isTamed(event.getEntity())) {

            BlockPos bedPos = TameableUtils.getPetBedPos(event.getEntity());
            if (bedPos != null) {
                CompoundTag data = new CompoundTag();
                event.getEntity().addAdditionalSaveData(data);
                String saveName = event.getEntity().hasCustomName() ? event.getEntity().getCustomName().getString() : "";
                RespawnRequest request = new RespawnRequest(BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString(), TameableUtils.getPetBedDimension(event.getEntity()), data, bedPos, event.getEntity().level().dayTime(), saveName);
                PHWorldData worldData = PHWorldData.get(event.getEntity().level());
                if (worldData != null) {
                    worldData.addRespawnRequest(request);
                }
            }
            if (!(event.getEntity() instanceof TamableAnimal)) {
                Entity owner = TameableUtils.getOwnerOf(event.getEntity());
                if (!event.getEntity().level().isClientSide && event.getEntity().level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && owner instanceof ServerPlayer) {
                    owner.sendSystemMessage(event.getEntity().getCombatTracker().getDeathMessage());
                }
            }

        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Start event) {
        float dist = 30;
        Vec3 center = event.getExplosion().center();
        Vec3 bottom = center.add(-dist, -dist, -dist);
        Vec3 top = center.add(dist, dist, dist);
        Predicate<Entity> defusal = (animal) -> TameableUtils.isTamed(animal) && TameableUtils.hasEnchant((LivingEntity) animal, ModEnchantments.DEFUSAL);
        boolean flag = false;
        for (LivingEntity defuser : event.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(bottom, top), EntitySelector.NO_SPECTATORS.and(defusal))) {
            float level = 10 * TameableUtils.getEnchantLevel(defuser, ModEnchantments.DEFUSAL);
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

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().getBlock() instanceof PetBedBlock) {
            if (event.getLevel().getBlockEntity(event.getPos()) instanceof PetBedBlockEntity petBedBlockEntity) {
                petBedBlockEntity.removeAllRequestsFor(event.getPlayer());
                petBedBlockEntity.resetBedsForNearbyPets();
            }
        }
    }

    @SubscribeEvent
    public static void onItemDespawnEvent(ItemExpireEvent event) {
        if (event.getEntity().getItem().getItem() == Items.APPLE && PetHomeConfig.rottenApple) {
            if (new Random().nextFloat() < 0.1F * event.getEntity().getItem().getCount()) {
                event.getEntity().getItem().shrink(1);
                event.setExtraLife(10);
                ItemEntity rotten = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(PHItemRegistry.ROTTEN_APPLE.get()));
                event.getEntity().level().addFreshEntity(rotten);
            }
        }
    }





    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {


        if (event.getType() == PHVillagerRegistry.ANIMAL_TAMER.get()) {
            List<VillagerTrades.ItemListing> level1 = new ArrayList<>();
            List<VillagerTrades.ItemListing> level2 = new ArrayList<>();
            List<VillagerTrades.ItemListing> level3 = new ArrayList<>();
            List<VillagerTrades.ItemListing> level4 = new ArrayList<>();
            List<VillagerTrades.ItemListing> level5 = new ArrayList<>();
//            level1.add(new BuyingItemTrade(Items.TROPICAL_FISH, 10, 2, 10, 2));
            level1.add(new SellingItemTrade(Items.BONE, 3, 6, 6, 1));
            level1.add(new SellingItemTrade(Items.EGG, 3, 6, 6, 1));
//            level1.add(new BuyingItemTrade(Items.HAY_BLOCK, 7, 1, 9, 1));
            level1.add(new SellingItemTrade(Items.COD, 2, 6, 1));
            level1.add(new SellingRandomEnchantedBook(1));
//            level1.add(new SellingItemTrade(Items.EGG, 4, 2, 9, 3));
//            level1.add(new SellingItemTrade(DIItemRegistry.FEATHER_ON_A_STICK.get(), 3, 1, 2, 3));
            level2.add(new SellingItemTrade(Items.TROPICAL_FISH_BUCKET, 2, 1, 6, 7));
            level2.add(new BuyingItemTrade(Items.CARROT, 5, 1, 12, 7));
            level2.add(new BuyingItemTrade(Items.STICK, 20, 1, 12, 6));
            level2.add(new SellingItemTrade(Items.APPLE, 4, 12, 3, 7));
            level2.add(new SellingRandomEnchantedBook(5));
//            level2.add(new SellingItemTrade(DIItemRegistry.DEED_OF_OWNERSHIP.get(), 3, 1, 2, 7));
            level3.add(new SellingItemTrade(PHItemRegistry.ROTTEN_APPLE.get(), 4, 1, 1, 10));
//            level3.add(new SellingItemTrade(Items.CARROT_ON_A_STICK, 3, 1, 2, 10));
            level3.add(new SellingRandomEnchantedBook(10));
//            level3.add(new SellingItemTrade(Items.LEATHER_HORSE_ARMOR, 4, 1, 3, 11));
//            level3.add(new SellingItemTrade(DIBlockRegistry.DRUM.get(), 2, 3, 7, 11));
            level3.add(new SellingItemTrade(Items.TADPOLE_BUCKET, 6, 1, 4, 13));
            level3.add(new SellingItemTrade(PHItemRegistry.COLLAR_TAG.get(), 4, 1, 6, 6));
            level3.add(new EnchantItemTrade(PHItemRegistry.COLLAR_TAG.get(), 20, 2, 8, 3, 10));
//            level4.add(new SellingItemTrade(Items.IRON_HORSE_ARMOR, 8, 1, 2, 15));
            level4.add(new SellingItemTrade(Items.AXOLOTL_BUCKET, 11, 1, 2, 15));
            level4.add(new SellingItemTrade(Items.TURTLE_EGG, 26, 1, 2, 15));
            level4.add(new SellingRandomEnchantedBook(15));
            level4.add(new EnchantItemTrade(PHItemRegistry.COLLAR_TAG.get(), 40, 3, 18, 3, 15));
//            level5.add(new SellingItemTrade(Items.GOLDEN_HORSE_ARMOR, 13, 1, 1, 18));
//            level5.add(new SellingItemTrade(Items.SCUTE, 21, 1, 3, 18));
            level5.add(new SellingItemTrade(Items.GOLDEN_CARROT, 6, 1, 6, 10));
            level5.add(new SellingItemTrade(Items.GOLDEN_APPLE, 10, 1, 6, 10));
            level5.add(new SellingItemTrade(PHItemRegistry.COLLAR_TAG.get(), 6, 1, 6, 10));
            level5.add(new EnchantItemTrade(PHItemRegistry.COLLAR_TAG.get(), 50, 4, 38, 3, 20));
            level5.add(new SellingRandomEnchantedBook(15));
            event.getTrades().put(1, level1);
            event.getTrades().put(2, level2);
            event.getTrades().put(3, level3);
            event.getTrades().put(4, level4);
            event.getTrades().put(5, level5);
        }
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
     * show enchantment description
     *
     * @param event
     */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        var tooltip = event.getToolTip();
        var stack = event.getItemStack();
        if (!ModList.get().isLoaded("enchdesc") && !stack.isEmpty() && stack.getItem() instanceof EnchantedBookItem) {
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
