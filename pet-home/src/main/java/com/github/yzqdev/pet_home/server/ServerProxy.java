package com.github.yzqdev.pet_home.server;

import com.github.yzqdev.pet_home.PetHomeConfig;
import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.client.ClientGameEvents;
import com.github.yzqdev.pet_home.datagen.ModEnchantments;
import com.github.yzqdev.pet_home.server.block.PetBedBlock;
import com.github.yzqdev.pet_home.server.block.PetBedBlockEntity;
import com.github.yzqdev.pet_home.server.entity.*;
import com.github.yzqdev.pet_home.server.item.NetItem;
import com.github.yzqdev.pet_home.server.item.PHItemRegistry;
import com.github.yzqdev.pet_home.server.item.Type;
import com.github.yzqdev.pet_home.server.misc.*;
import com.github.yzqdev.pet_home.server.misc.trades.BuyingItemTrade;
import com.github.yzqdev.pet_home.server.misc.trades.EnchantItemTrade;
import com.github.yzqdev.pet_home.server.misc.trades.SellingItemTrade;
import com.github.yzqdev.pet_home.server.misc.trades.SellingRandomEnchantedBook;
import com.github.yzqdev.pet_home.util.FriendlyFireCommon;
import com.github.yzqdev.pet_home.util.LivingUtils;
import com.github.yzqdev.pet_home.util.TameableUtils;
import com.github.yzqdev.pet_home.worldgen.VillageHouseManager;
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
public class ServerProxy {
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

    @SubscribeEvent
    public static void onTameHurt(EntityInvulnerabilityCheckEvent event) {
//        if (event.getEntity() instanceof LivingEntity livingEntity) {
//            if (TameableUtils.isTamed(livingEntity) && event.getSource().getDirectEntity() instanceof Player player && TameableUtils.isPetOf(player, event.getEntity()) && !player.isShiftKeyDown()) {
//                event.setInvulnerable(true);
//
//            }
//            if (livingEntity.isBaby()) {
//                if (event.getSource().getEntity() instanceof Player player) {
//                    if (!player.isShiftKeyDown()) {
//                        event.setInvulnerable(true);
//                    }
//                }
//            }
//        }

    }

    @SubscribeEvent
    public static void onServerTick(LevelTickEvent.Post tick) {

        if (!tick.getLevel().isClientSide && tick.getLevel() instanceof ServerLevel) {
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        var level = event.getEntity().level();
        if (!level.isClientSide() && FriendlyFireCommon.preventAttack(event.getEntity(), event.getSource(), event.getAmount())) {

            event.setCanceled(true);
            event.getEntity().setLastHurtByMob(null);

            if (event.getSource().getEntity() instanceof LivingEntity trueSource) {

                trueSource.setLastHurtByMob(null);
            }
        }
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {

            if (TameableUtils.isTamed(event.getEntity()) && !event.getSource().is(PHDamageTypes.SIPHON)) {

                var beingAttacked = event.getEntity();
                boolean flag = false;

                //pet being attacked
                if (TameableUtils.hasEnchant(event.getEntity(), ModEnchantments.BLAZING_PROTECTION)) {
                    int bars = TameableUtils.getBlazingProtectionBars(event.getEntity());
                    if (bars > 0) {

                        if (attacker instanceof LivingEntity livingAttacker && !TameableUtils.hasSameOwnerAs(livingAttacker, event.getEntity())) {
                            livingAttacker.igniteForTicks(20 * (5 + event.getEntity().getRandom().nextInt(3)));
                            livingAttacker.knockback(0.4, event.getEntity().getX() - livingAttacker.getX(), event.getEntity().getZ() - livingAttacker.getZ());
                        }
                        event.setCanceled(true);
                        flag = true;
                        if (attacker != null) {
                            for (int i = 0; i < 3 + event.getEntity().getRandom().nextInt(3); i++) {
                                attacker.level().addParticle(ParticleTypes.FLAME, event.getEntity().getRandomX(0.8F), event.getEntity().getRandomY(), event.getEntity().getRandomZ(0.8F), 0.0F, 0.0F, 0.0F);
                            }
                        }
                        event.getEntity().playSound(PHSoundRegistry.BLAZING_PROTECTION.get(), 1, event.getEntity().getVoicePitch());
                        TameableUtils.setBlazingProtectionBars(event.getEntity(), bars - 1);
                        TameableUtils.setBlazingProtectionCooldown(event.getEntity(), 600);
                    }
                }
                if (TameableUtils.isTamed(event.getEntity())) {
                    if (!flag && TameableUtils.hasEnchant(event.getEntity(), ModEnchantments.HEALTH_SIPHON)) {
                        Entity owner = TameableUtils.getOwnerOf(event.getEntity());
                        if (owner != null && owner.isAlive() && owner.distanceTo(event.getEntity()) < 100 && owner != event.getEntity()) {
                            owner.hurt(event.getSource(), event.getAmount());
                            event.setCanceled(true);
                            flag = true;
                            event.getEntity().hurt(PHDamageTypes.causeSiphonDamage(owner.level().registryAccess()), 0.0F);
                        }
                    }
                }
                if (!flag && TameableUtils.hasEnchant(event.getEntity(), ModEnchantments.TOTAL_RECALL) && event.getEntity().getHealth() - event.getAmount() <= 2.0D) {
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
            }

            //when tamed animal attack others
            if (event.getSource().getEntity() != null && TameableUtils.isTamed(event.getSource().getEntity())) {

                int lightningLevel = TameableUtils.getEnchantLevel(attacker, ModEnchantments.CHAIN_LIGHTNING);
                int bubblingLevel = TameableUtils.getEnchantLevel(attacker, ModEnchantments.BUBBLING);
                int vampireLevel = TameableUtils.getEnchantLevel(attacker, ModEnchantments.VAMPIRE);
                if (lightningLevel > 0) {
                    ChainLightningEntity lightning = PHEntityRegistry.CHAIN_LIGHTNING.get().create(event.getEntity().level());
                    lightning.setCreatorEntityID(attacker.getId());
                    lightning.setFromEntityID(attacker.getId());
                    lightning.setToEntityID(event.getEntity().getId());
                    lightning.copyPosition(event.getEntity());
                    lightning.setChainsLeft(3 + lightningLevel * 3);
                    event.getEntity().level().addFreshEntity(lightning);
                    event.getEntity().playSound(PHSoundRegistry.CHAIN_LIGHTNING.get(), 1F, 1F);
                }
                if (vampireLevel > 0) {
                    if (attacker.getHealth() < attacker.getMaxHealth()) {
                        float f = Mth.clamp(event.getAmount() * vampireLevel * 0.5F, 1F, 10F);
                        attacker.heal(f);
                        if (event.getEntity().level() instanceof ServerLevel) {
                            for (int i = 0; i < 5 + event.getEntity().getRandom().nextInt(3); i++) {
                                double f1 = event.getEntity().getRandomX(0.7F);
                                double f2 = event.getEntity().getY(0.4F + event.getEntity().getRandom().nextFloat() * 0.2F);
                                double f3 = event.getEntity().getRandomZ(0.7F);
                                Vec3 motion = attacker.getEyePosition().subtract(f1, f2, f3).normalize().scale(0.2F);
                                ((ServerLevel) event.getEntity().level()).sendParticles(PHParticleRegistry.VAMPIRE.get(), f1, f2, f3, 1, motion.x, motion.y, motion.z, 0.2F);
                            }
                        }
                    }
                }
                if (bubblingLevel > 0) {
                    if (!(event.getEntity().getRootVehicle() instanceof GiantBubbleEntity) && (event.getEntity().onGround() || event.getEntity().isInWaterOrBubble() || event.getEntity().isInLava())) {
                        GiantBubbleEntity bubble = PHEntityRegistry.GIANT_BUBBLE.get().create(event.getEntity().level());
                        bubble.copyPosition(event.getEntity());
                        event.getEntity().startRiding(bubble, true);
                        bubble.setpopsIn(bubblingLevel * 40 + 40);
                        event.getEntity().level().addFreshEntity(bubble);
                        event.getEntity().playSound(PHSoundRegistry.GIANT_BUBBLE_INFLATE.get(), 1F, 1F);

                    }
                }


                if (TameableUtils.hasEnchant(attacker, ModEnchantments.FROST_FANG)) {
                    event.getEntity().setTicksFrozen(event.getEntity().getTicksRequiredToFreeze() + 200);
                    Vec3 vec3 = event.getEntity().getEyePosition().subtract(attacker.getEyePosition()).normalize().scale(attacker.getBbWidth() + 0.5F);
                    Vec3 vec32 = attacker.getEyePosition().add(vec3);
                    for (int i = 0; i < 3 + attacker.getRandom().nextInt(3); i++) {
                        float f1 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
                        float f2 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
                        float f3 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
                        attacker.level().addParticle(ParticleTypes.SNOWFLAKE, vec32.x + f1, vec32.y + f2, vec32.z + f3, 0.0F, 0.0F, 0.0F);
                    }
                    TameableUtils.setFrozenTimeTag(event.getEntity(), 60);
                }

                if (!event.getEntity().level().isClientSide && TameableUtils.hasEnchant(attacker, ModEnchantments.WARPING_BITE)) {
                    for (int i = 0; i < 16; ++i) {
                        double d3 = event.getEntity().getX() + (attacker.getRandom().nextDouble() - 0.5D) * 16.0D;
                        double d4 = Mth.clamp(event.getEntity().getY() + (double) (attacker.getRandom().nextInt(16) - 8), event.getEntity().level().getMinBuildHeight(), event.getEntity().level().getMinBuildHeight() + ((ServerLevel) event.getEntity().level()).getLogicalHeight() - 1);
                        double d5 = event.getEntity().getZ() + (attacker.getRandom().nextDouble() - 0.5D) * 16.0D;
                        if (event.getEntity().randomTeleport(d3, d4, d5, true)) {
                            SoundEvent soundevent = event.getEntity() instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                            event.getEntity().playSound(soundevent, 1.0F, 1.0F);
                            break;
                        }
                    }
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

        }


    }

    @SubscribeEvent
    public static void onEntityHurtPre(LivingDamageEvent.Pre event) {
        var maid = event.getSource().getEntity();

        var monster = event.getEntity();
        var level = monster.level();
        var chance = level.random.nextFloat();

//        var logStr = "ab.yzq.mod.pet_home.server.ServerProxy.onEntityHurtPre(ServerProxy.java:425)";
//        PetHomeMod.LOGGER.info(logStr);
//        PetHomeMod.LOGGER.info(String.valueOf(chance));


        if (maid instanceof LivingEntity maidLiving && TameableUtils.hasEnchant(maidLiving, ModEnchantments.VIOLENT)) {
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
                event.setNewDamage(event.getOriginalDamage() + 3);
            } else if (chance < 0.60) {
                monster.igniteForTicks(20 * 5);
            } else if (chance < 0.7) {
                if (monster.getHealth() > 30) {
                    event.setNewDamage(monster.getHealth() / 3);
                } else {
                    event.setNewDamage(event.getOriginalDamage() + 5);
                }

            } else if (chance < 0.8) {
                monster.addEffect(new MobEffectInstance(ModEffects.DRUNK, 20 * 5));
            } else {
                monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1 * 20, 50, false, false));
                monster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1 * 20, 50, false, false));
            }
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingDamageEvent.Post event) {
        var hurtEntity = event.getEntity();
        var attacker = event.getSource().getEntity();
        if (TameableUtils.hasEnchant(hurtEntity, ModEnchantments.CHAOS) && attacker instanceof LivingEntity) {

            ((LivingEntity) attacker).addEffect(new MobEffectInstance(ModEffects.DRUNK, 120, 1));
        }
        var shareEnchantLevel = TameableUtils.getEnchantLevel(hurtEntity, ModEnchantments.SHARE);
        if (shareEnchantLevel > 0 && attacker instanceof LivingEntity attackerLiving) {
            var monsterEntities = TameableUtils.getNearbyMobs(hurtEntity, 20).stream().filter(i -> i instanceof Enemy).collect(Collectors.toSet());

            if (monsterEntities.size() > 1) {
                float originalDamage = event.getOriginalDamage();
                monsterEntities.forEach(i -> {
                    i.hurt(event.getSource(), (float) (originalDamage * 0.3));
                });

            }
        }
        var paralysicLevel = TameableUtils.getEnchantLevel(hurtEntity, ModEnchantments.PARALYSIS);
        if (paralysicLevel > 0 && attacker instanceof LivingEntity attackerLiving) {

            attackerLiving.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, paralysicLevel * 20, 100, false, false));
            attackerLiving.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, paralysicLevel * 20, 100, false, false));
            attackerLiving.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, paralysicLevel * 20, 100, false, false));

        }

        if (FriendlyFireCommon.preventAttack(event.getEntity(), event.getSource(), event.getNewDamage())) {


            event.getEntity().setLastHurtByMob(null);

            if (event.getSource().getEntity() instanceof LivingEntity trueSource) {

                trueSource.setLastHurtByMob(null);
            }
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
            if (!living.level().isClientSide && living.isAlive() && TameableUtils.isTamed(living) && TameableUtils.shouldUnloadToLantern(living)) {
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

    public static void mobTick(EntityTickEvent.Pre event, LivingEntity attacker) {
        // 仅服务端执行：涉及 ServerLevel（粒子、目标选择）等逻辑，客户端 level 为 ClientLevel 会 ClassCastException
        if (attacker.level().isClientSide()) {
            return;
        }
        List<Monster> genericMobs = attacker.level().getEntitiesOfClass(Monster.class, LivingUtils.getBoundingBoxAroundEntity(attacker, (double) 10.0F));
        Random random = new Random();
        if (attacker.hasEffect(ModEffects.DRUNK) && attacker instanceof Monster) {

            double x = attacker.getX();
            double y = attacker.getY() + attacker.getBbHeight() + 0.4;
            double z = attacker.getZ();
            if (attacker.level().getGameTime() % 10 == 0) {
                ((ServerLevel) (attacker.level())).sendParticles(PHParticleRegistry.QUESTION_MARK_PARTICLE_TYPE.get(), x, y, z, 1, 0, 0, 0, 0.0D);
            }


            if (genericMobs == null) {
                return;
            }

            Monster Monster = (Monster) attacker;
            if (genericMobs.size() <= 0) {
                return;
            }

            Monster others = (Monster) genericMobs.get(random.nextInt(genericMobs.size()));
            if (genericMobs.size() > 2) {
                while (others == Monster) {
                    others = (Monster) genericMobs.get(random.nextInt(genericMobs.size()));
                }
            }

            if (others == null) {
                Monster.setTarget((LivingEntity) null);
            } else {
                LivingUtils.setAttackTarget(Monster, others);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(EntityTickEvent.Pre event) {
        var entity = event.getEntity();

        if (entity instanceof Mob pet) {
            mobTick(event, pet);
            if (TameableUtils.hasEnchant(pet, ModEnchantments.BLAZING_PROTECTION) && !event.getEntity().level().isClientSide) {
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

            if (TameableUtils.hasEnchant(pet, ModEnchantments.NIGHT_VISION) && TameableUtils.isTamed(pet)) {
                var owner = TameableUtils.getOwnerOf(pet);
                if (owner != null && owner.distanceToSqr(pet) < 10 && owner instanceof Player petOwner) {
                    if (!petOwner.hasEffect(MobEffects.NIGHT_VISION) && pet.tickCount % 40 == 0) {
                        petOwner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60 * 5));
                    }
                }
            }
            if (TameableUtils.hasEnchant(pet, ModEnchantments.XP_Transfer)) {
                TameableUtils.xpTransfer(pet);
            }
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
            if (TameableUtils.hasEnchant(pet, ModEnchantments.IMMUNITY_FRAME) && !event.getEntity().level().isClientSide) {
                int i = TameableUtils.getImmuneTime(pet);
                if (i > 0) {
                    TameableUtils.setImmuneTime(pet, i - 1);
                }
            }
            if (pet.hasEffect(MobEffects.POISON) && TameableUtils.hasEnchant(pet, ModEnchantments.POISON_RESISTANCE)) {
                pet.removeEffect(MobEffects.POISON);
            }
            if (TameableUtils.hasEnchant(pet, ModEnchantments.BLIGHT_CURSE)) {
                TameableUtils.destroyRandomPlants(pet);
            }
            if (TameableUtils.hasEnchant(pet, ModEnchantments.REJUVENATION)) {
                TameableUtils.absorbExpOrbs(pet);
            }
            if (TameableUtils.hasEnchant(pet, ModEnchantments.INFAMY_CURSE)) {
                TameableUtils.aggroRandomMonsters(pet);
            }
            if (TameableUtils.hasEnchant(pet, ModEnchantments.AMPHIBIOUS)) {
                pet.setAirSupply(pet.getMaxAirSupply());
            }
            if (TameableUtils.hasEnchant(pet, ModEnchantments.INTIMIDATION)) {
                TameableUtils.scareRandomMonsters(pet, TameableUtils.getEnchantLevel(pet, ModEnchantments.INTIMIDATION));
            }
            var insightLevel = TameableUtils.getEnchantLevel(pet, ModEnchantments.INSIGHT);
            if (insightLevel > 0) {
                if (entity.level() instanceof ServerLevel serverLevel) {
                    var brightness = serverLevel.getMaxLocalRawBrightness(pet.getOnPos().above()) < 9;
                    if (brightness) {
                        TameableUtils.applyGlowingEffect(pet, insightLevel);
                    }
                }

            }
            if (TameableUtils.hasEnchant(pet, ModEnchantments.SonicBoom)) {
                var attacker = pet;
                var beingAttacked = pet.getTarget();
                if (beingAttacked != null) {
                    if (pet.closerThan(beingAttacked, 10.0, 20.0) || TameableUtils.getNearbyMobs(pet, 10).size() > 3) {
//                       long nextSonicBoomTime=TameableUtils.getSonicboomAuraTime(attacker);

                        if (pet.tickCount % 200 == 0) {
                            System.out.println("冲击波!");
                            TameableUtils.performSonicBook(attacker, beingAttacked, (ServerLevel) attacker.level());
//                            TameableUtils.setSonicboomAuraTime(attacker, Instant.now().getEpochSecond() + 5);
                        }
                    }
                }

            }
            int shadowHandsLevel = TameableUtils.getEnchantLevel(pet, ModEnchantments.SHADOW_HANDS);
            if (shadowHandsLevel > 0 && event.getEntity() instanceof Mob mob) {
                ClientGameEvents.updateVisualDataForMob(event.getEntity(), TameableUtils.getShadowPunchTimes(mob));
                if (!mob.level().isClientSide) {
                    var targetEntity = TameableUtils.getPetAttackTarget(mob);
                    Entity punching = ((targetEntity instanceof Player) || (targetEntity instanceof TamableAnimal)) ? null : targetEntity;
                    int[] punchProgress = TameableUtils.getShadowPunchTimes(mob);
                    if (punching != null && punching.isAlive() && mob.hasLineOfSight(punching) && mob.distanceTo(punching) < 16) {
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
                    } else {
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
                }
            }
            int oreLvl = TameableUtils.getEnchantLevel(pet, ModEnchantments.ORE_SCENTING);
            if (oreLvl > 0 && !event.getEntity().level().isClientSide && event.getEntity().isAlive()) {
                int interval = 100 + Math.max(150, 550 - oreLvl * 100);
                TameableUtils.detectRandomOres(pet, interval, 5 + oreLvl * 2, oreLvl * 50, oreLvl * 3);
            }
            if (TameableUtils.hasEnchant(pet, ModEnchantments.LINKED_INVENTORY) && event.getEntity() instanceof Mob mob) {
                if (!mob.canPickUpLoot()) {
                    mob.setCanPickUpLoot(true);
                }
            }
            if (TameableUtils.hasEnchant(pet, ModEnchantments.HEALING_AURA) && !pet.level().isClientSide) {
                int time = TameableUtils.getHealingAuraTime(pet);
                if (time > 0) {
                    List<LivingEntity> hurtNearby = TameableUtils.getAuraHealables(pet);
                    for (LivingEntity needsHealing : hurtNearby) {
                        if (!needsHealing.hasEffect(MobEffects.REGENERATION)) {
                            needsHealing.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, TameableUtils.getEnchantLevel(pet, ModEnchantments.HEALING_AURA) - 1));
                        }
                    }
                    time--;
                    if (time == 0) {
                        time = -600 - pet.getRandom().nextInt(600);
                    }
                } else if (time < 0) {
                    time++;
                } else if ((pet.tickCount + pet.getId()) % 200 == 0 || TameableUtils.getHealingAuraImpulse(pet)) {
                    List<LivingEntity> hurtNearby = TameableUtils.getAuraHealables(pet);
                    if (!hurtNearby.isEmpty()) {
                        time = 200;
                    }
                    TameableUtils.setHealingAuraImpulse(pet, false);
                }
                TameableUtils.setHealingAuraTime(pet, time);
            }
            int psychicWallLevel = TameableUtils.getEnchantLevel(pet, ModEnchantments.PSYCHIC_WALL);
            if (psychicWallLevel > 0 && event.getEntity() instanceof Mob mob && !event.getEntity().level().isClientSide) {
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


            int shepherdLvl = TameableUtils.getEnchantLevel(pet, ModEnchantments.SHEPHERD);
            if (shepherdLvl > 0) {
                TameableUtils.attractAnimals(pet, shepherdLvl * 3);
            }
            if (TameableUtils.hasEnchant(pet, ModEnchantments.MAGNETIC) && event.getEntity() instanceof Mob mob) {
                Entity sucking = TameableUtils.getPetAttackTarget(mob);
                if (!mob.level().isClientSide) {
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
        }

    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractWithEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        var hand = event.getHand();
        var level = event.getLevel();
        Entity pet = event.getTarget();
        ItemStack itemstack = event.getItemStack();
        if (!level.isClientSide() && itemstack.is(PHItemRegistry.NET_ITEM)) {
            if (!pet.isAlive() || NetItem.containsEntity(itemstack)) {
                return;
            }
            var netItem = (NetItem) itemstack.getItem();
            if (netItem.getType() == Type.EMPTY) {
                EntityType<?> entityID = pet.getType();
                if (!NetItem.canCatchMob(pet)) {
                    event.setCanceled(true);
                    return;
                }
                ItemStack newStack = new ItemStack(PHItemRegistry.NET_HAS_ITEM.get());
                CompoundTag nbt = NetItem.getNBTfromEntity(pet);
                ItemStack newerStack = newStack.split(1);
                newerStack.set(PHDataComponents.ENTITY_HOLDER, nbt);

                player.swing(hand);
                player.setItemInHand(hand, newStack);
                if (!player.addItem(newerStack)) {
                    ItemEntity itemEntity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), newerStack);
                    player.level().addFreshEntity(itemEntity);
                }
                pet.discard();
                player.getCooldowns().addCooldown(itemstack.getItem(), 5);

            }

        }

        if (event.getTarget() instanceof LivingEntity living && TameableUtils.isPetOf(player, pet)) {
            if (TameableUtils.hasEnchant(living, ModEnchantments.GLUTTONOUS)) {
                var foodProperty = itemstack.getItem().getFoodProperties(itemstack, living);
                if (foodProperty != null && living.getHealth() < living.getMaxHealth()) {
                    living.heal((float) Math.floor(foodProperty.nutrition() * 1.5F));
                    if (!event.getEntity().isCreative()) {
                        itemstack.shrink(1);
                    }
                    living.playSound(living.getRandom().nextBoolean() ? SoundEvents.PLAYER_BURP : SoundEvents.GENERIC_EAT, 1F, living.getVoicePitch());
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
            if (itemstack.is(PHItemRegistry.COLLAR_TAG.get())) {


                if (!event.getEntity().level().isClientSide && living.isAlive()) {
                    var itemEnchantments = itemstack.getTagEnchantments();
                    Map<ResourceLocation, Integer> entityEnchantments = TameableUtils.getEnchants(living);
                    if (itemstack.has(DataComponents.CUSTOM_NAME)) {
                        living.setCustomName(itemstack.getHoverName());
                    }
                    if (!event.getEntity().isCreative()) {
                        itemstack.shrink(1);
                    }
                    if (TameableUtils.hasCollar(living)) {
                        ItemStack collarFrom = new ItemStack(PHItemRegistry.COLLAR_TAG.get());
                        if (entityEnchantments != null) {

                            var reg = living.level().registryAccess().registry(Registries.ENCHANTMENT);
                            if (reg.isPresent()) {
                                for (Map.Entry<ResourceLocation, Integer> entry : entityEnchantments.entrySet()) {
                                    var oneEnchant = reg.get().get(entry.getKey());
                                    if (oneEnchant != null) {
                                        collarFrom.enchant(reg.get().wrapAsHolder(oneEnchant), entry.getValue());
                                    }

                                }
                            }


                        }
                        living.spawnAtLocation(collarFrom);
                    }
                    living.playSound(PHSoundRegistry.COLLAR_TAG.get(), 1, 1);
                    if (!itemEnchantments.isEmpty()) {

                        TameableUtils.clearEnchants(living);
                        TameableUtils.addEnchant(living, itemEnchantments);

                    } else {
                        TameableUtils.clearEnchants(living);
                    }
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
            if (TameableUtils.isTamed(event.getTarget())) {
                if (player.getItemInHand(hand).is(PHItemRegistry.DEED_OF_OWNERSHIP.get())) {
                    TamableAnimal tamableAnimal = (TamableAnimal) pet;
                    tamableAnimal.setTame(false, false);
                    tamableAnimal.setOwnerUUID(null);
                    tamableAnimal.setOrderedToSit(false);
                    tamableAnimal.setInSittingPose(false);
                    player.swing(hand);
                    event.setCanceled(true);
                }

            }
        }

        if (pet instanceof LivingEntity living) {
            if (pet.getType() == EntityType.HORSE && itemstack.is(PHItemRegistry.ROTTEN_APPLE) && EventHooks.canLivingConvert(living, EntityType.ZOMBIE_HORSE, (timer) -> {
            })) {
                player.swing(hand);
                Horse horse = (Horse) pet;
                horse.playSound(SoundEvents.HORSE_DEATH, 0.8F, horse.getVoicePitch());
                horse.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, horse.getVoicePitch());
                CompoundTag horseExtras = new CompoundTag();
                if (!horse.getBodyArmorItem().isEmpty()) {
                    horse.spawnAtLocation(horse.getBodyArmorItem().copy());
                    horse.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                }
                horse.addAdditionalSaveData(horseExtras);
                for (int i = 0; i < 6 + horse.getRandom().nextInt(5); i++) {
                    horse.level().addParticle(ParticleTypes.SNEEZE, horse.getRandomX(1.0F), horse.getRandomY(), horse.getRandomZ(1.0F), 0F, 0F, 0F);
                }
                ZombieHorse zombie = EntityType.ZOMBIE_HORSE.create(horse.level());
                if (horse.isLeashed()) {
                    zombie.setLeashedTo(horse.getLeashHolder(), true);
                }
                zombie.moveTo(horse.getX(), horse.getY(), horse.getZ(), horse.getYRot(), horse.getXRot());
                zombie.setNoAi(horse.isNoAi());
                zombie.setBaby(horse.isBaby());
                if (horse.hasCustomName()) {
                    zombie.setCustomName(horse.getCustomName());
                    zombie.setCustomNameVisible(horse.isCustomNameVisible());
                }
                zombie.readAdditionalSaveData(horseExtras);
                zombie.setPersistenceRequired();
                EventHooks.onLivingConvert(horse, zombie);
                player.level().addFreshEntity(zombie);
                horse.discard();
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
//           return InteractionResult.CONSUME;
            }
            if (pet.getType() == EntityType.RABBIT && itemstack.is(PHItemRegistry.SINISTER_CARROT) && TameableUtils.isTamed(pet) && TameableUtils.isPetOf(player, pet) && EventHooks.canLivingConvert(living, EntityType.RABBIT, (timer) -> {
            })) {
                if (pet instanceof Rabbit rabbit && rabbit.getVariant() != Rabbit.Variant.EVIL) {
                    player.swing(hand);
                    rabbit.playSound(SoundEvents.RABBIT_ATTACK, 0.8F, rabbit.getVoicePitch());
                    rabbit.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, rabbit.getVoicePitch());
                    rabbit.setVariant(Rabbit.Variant.EVIL);
                    if (!player.isCreative()) {
                        itemstack.shrink(1);
                    }
//               return InteractionResult.CONSUME;
                }
            }
            if (pet.getType() == EntityType.ZOMBIE_HORSE && itemstack.is(PHItemRegistry.SINISTER_CARROT) && EventHooks.canLivingConvert(living, EntityType.SKELETON_HORSE, (timer) -> {
            })) {
                player.swing(hand);
                ZombieHorse horse = (ZombieHorse) pet;
                horse.playSound(SoundEvents.HORSE_DEATH, 0.8F, horse.getVoicePitch());
                horse.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, horse.getVoicePitch());
                CompoundTag horseExtras = new CompoundTag();
                horse.addAdditionalSaveData(horseExtras);
                for (int i = 0; i < 6 + horse.getRandom().nextInt(5); i++) {
                    horse.level().addParticle(ParticleTypes.SNEEZE, horse.getRandomX(1.0F), horse.getRandomY(), horse.getRandomZ(1.0F), 0F, 0F, 0F);
                }
                SkeletonHorse skeleton = EntityType.SKELETON_HORSE.create(horse.level());
                if (horse.isLeashed()) {
                    skeleton.setLeashedTo(horse.getLeashHolder(), true);
                }
                skeleton.moveTo(horse.getX(), horse.getY(), horse.getZ(), horse.getYRot(), horse.getXRot());
                skeleton.setNoAi(horse.isNoAi());
                skeleton.setBaby(horse.isBaby());
                if (horse.hasCustomName()) {
                    skeleton.setCustomName(horse.getCustomName());
                    skeleton.setCustomNameVisible(horse.isCustomNameVisible());
                }
                skeleton.readAdditionalSaveData(horseExtras);
                skeleton.setPersistenceRequired();
                EventHooks.onLivingConvert(horse, skeleton);
                player.level().addFreshEntity(skeleton);
                horse.discard();
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
//           return InteractionResult.CONSUME;
            }
//       return InteractionResult.PASS;
        }
//       return InteractionResult.PASS;

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
                    var e = enchantmentHolder.value();
                    if (enchantmentHolder.getKey().location().getNamespace().contains(PetHomeMod.MODID)) {
                        final MutableComponent description = getDescription(enchantmentHolder, enchantmentHolder.unwrapKey().orElseThrow().location(), e.getMaxLevel());
                        if (description != null) {

                            tooltip.add(description);
                        }
                    }


                }
            }

        }
    }
}
