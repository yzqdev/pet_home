package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.ClientGameEvents;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.mixin.ArrowAccessor;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.block.PetBedBlock;
import com.github.yzqdev.pethome.server.block.PetBedBlockEntity;
import com.github.yzqdev.pethome.server.entity.*;
import com.github.yzqdev.pethome.server.item.NetItem;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.item.Type;
import com.github.yzqdev.pethome.server.misc.*;
import com.github.yzqdev.pethome.PHModLogger;
import com.github.yzqdev.pethome.util.FriendlyFireCommon;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.util.LivingUtils;
import com.github.yzqdev.pethome.util.TameableUtils;
import com.github.yzqdev.pethome.worldgen.PetshopStructurePoolElement;
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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author yzqde
 * @date time 2025/1/9 9:47
 * @modified By:
 */

@EventBusSubscriber(modid = PetHomeMod.MODID)
public class ServerEvent {
    /** 有宠物床的宠物死亡不掉落战利品（物品将由复活实体重新拥有），取消掉落事件。 */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (TameableUtils.isTamed(event.getEntity()) && TameableUtils.getPetBedPos(event.getEntity()) != null) {

            event.setCanceled(true);
        }
    }

    /** 服务器启动：注入村庄宠物店房屋到结构池，并在主线程预热宠物店刷怪列表（worldgen worker 线程会读）。 */
    @SubscribeEvent
    public static void serverStart(ServerAboutToStartEvent event) {
        RegistryAccess registryAccess = event.getServer().registryAccess();
        VillageHouseManager.addAllHouses(registryAccess);
        PetshopStructurePoolElement.preloadMobLists();
    }




    public record TeleportingPet(Entity entity, ServerLevel targetLevel, UUID ownerUUID) {
    }

    /** 跨维度宠物搬运队列：玩家换维度/罗盘召回写入，onServerTick 每 10 tick 用 create+restoreFrom+addDuringTeleport 重建实体（Entity#setLevel 已 protected）。 */
    public static List<TeleportingPet> teleportingPets = new ArrayList<>();

    /**
     * xp_transfer 的玩家驱动实现：吸附玩家 10 格内非坐下、无仇恨宠物的经验球（近距给经验、远距施加吸力并让宠物追球），由 onServerTick 每 10 tick 对每个玩家调用。
     * 宠物范围覆盖两类归属载体：原版 {@link TamableAnimal} 与本模组改造宠物（{@link ModifiedToBeTameable}，兔子等）。
     */
    public static void onWorldTick(ServerPlayer serverPlayer, ServerLevel world) {

        AABB playerArea = serverPlayer.getBoundingBox().inflate(10.0);
        Predicate<Entity> petFilter = pet ->
                pet instanceof Mob mobPet
                        && TameableUtils.isTamed(mobPet)
                        && !(mobPet instanceof TamableAnimal tamable && tamable.isOrderedToSit())
                        && mobPet.getTarget() == null
                        && TameableUtils.hasEnchant(mobPet, PHEnchantments.XP_Transfer);
        List<Mob> pets = world.getEntitiesOfClass(Mob.class, playerArea, petFilter);

        for (Mob pet : pets) {

            AABB searchBox = pet.getBoundingBox().inflate(10.0);
            List<ExperienceOrb> orbs = world.getEntitiesOfClass(ExperienceOrb.class, searchBox);

            if (orbs.isEmpty()) {
                continue;
            }


            Entity owner = TameableUtils.getOwnerOf(pet);
            if (!(owner instanceof Player player)) {
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

    private static final Map<ResourceKey<Level>, CollarTickTracker> COLLAR_TICK_TRACKER_MAP = new HashMap<>();

    /** 项圈刚被取下/装备后短时间内不再 tick */
    public static boolean canTickCollar(Entity entity) {
        if (entity.level().isClientSide()) {
            return true;
        }
        CollarTickTracker tracker = COLLAR_TICK_TRACKER_MAP.get(entity.level().dimension());
        return tracker == null || !tracker.isEntityBlocked(entity);
    }

    /** 给实体起 5 tick 项圈防抖（PlayerInteractEntityHandler 换项圈时调用）。 */
    public static void blockCollarTick(Entity entity) {
        if (!entity.level().isClientSide()) {
            CollarTickTracker tracker = COLLAR_TICK_TRACKER_MAP.computeIfAbsent(entity.level().dimension(), k -> new CollarTickTracker());
            tracker.addBlockedEntityTick(entity.getUUID(), 5);
        }
    }

    /**
     * 每 tick 收尾（服务端）：项圈防抖递减；召回队列只在主世界 tick 上按 20 tick 分频处理；
     * 每 10 tick 分频——逐玩家跑 {@link #onWorldTick}（经验吸附），再处理 {@link #teleportingPets} 跨维度重建。
     */
    @SubscribeEvent
    public static void onServerTick(LevelTickEvent.Post tick) {
        if (!tick.getLevel().isClientSide()) {
            CollarTickTracker tracker = COLLAR_TICK_TRACKER_MAP.computeIfAbsent(tick.getLevel().dimension(), k -> new CollarTickTracker());
            tracker.tick();
        }
        // 宠物罗盘召回队列：全局队列只在主世界 tick 上处理（每 20 tick 一轮）
        if (!tick.getLevel().isClientSide() && tick.getLevel().dimension() == Level.OVERWORLD && tick.getLevel().getGameTime() % 20 == 0) {
            PetCompassTeleport.processRecallQueue(tick.getLevel().getServer(), tick.getLevel().getGameTime());
        }
        if (tick.getLevel().getGameTime() % 10 != 0) return;
        if (!tick.getLevel().isClientSide() && tick.getLevel() instanceof ServerLevel) {
            for (var player : tick.getLevel().players()) {
               if (player instanceof ServerPlayer serverPlayer){
                   onWorldTick(serverPlayer,(ServerLevel) tick.getLevel());
               }
            }
            for (final var triple : teleportingPets) {
                Entity entity = triple.entity();
                ServerLevel endpointWorld = triple.targetLevel();
                UUID ownerUUID = triple.ownerUUID();
                entity.unRide();
                Entity player = endpointWorld.getPlayerByUUID(ownerUUID);
                if (player != null) {

                    Entity teleportedEntity = entity.getType().create(endpointWorld, EntitySpawnReason.DIMENSION_TRAVEL);
                    if (teleportedEntity != null) {
                        teleportedEntity.restoreFrom(entity);
                        Vec3 toPos = player.position();
                        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
                        AABB suffocationBox = new AABB(-dimensions.width() / 2.0F, 0, -dimensions.width() / 2.0F, dimensions.width() / 2.0F, dimensions.height(), dimensions.width() / 2.0F);
                        while (!endpointWorld.noCollision(entity, suffocationBox.move(toPos.x, toPos.y, toPos.z)) && toPos.y < 300) {
                            toPos = toPos.add(0, 1, 0);
                        }
                        teleportedEntity.setPos(toPos.x, toPos.y, toPos.z);
                        teleportedEntity.setYRot(entity.getYRot());
                        teleportedEntity.setXRot(entity.getXRot());
                        teleportedEntity.setYHeadRot(entity.getYHeadRot());
                        teleportedEntity.fallDistance = 0.0F;
                        teleportedEntity.setPortalCooldown();
                        endpointWorld.addDuringTeleport(teleportedEntity);
                    }
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    // 跨维度重建完成后补发罗盘数据：召回入队时发的刷新包早于实际到位（宠物还在旧维度），
                    // 此时才拿到新维度/新位置；GUI 开着时原位刷新，关闭时 handleData 不开屏
                    if (player instanceof ServerPlayer serverPlayer) {
                        PHModLogger.debugLog("[compass-debug] cross-dim pet arrived, resending compass data");
                        PetCompassTracker.handleOpenRequest(serverPlayer);
                    }
                }
            }
            teleportingPets.clear();
        }
    }

    /** 玩家换维度：把 20 格内 tethered_teleport 且可跟传的宠物带走（跨维度入 {@link #teleportingPets}）。 */
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

    /** 玩家同维度传送（末影珍珠等）：同上筛选 20 格内可跟传宠物，落点窒息上探 + PORTAL 票据防未加载。 */
    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if (event.getEntity() instanceof Player) {
            teleportNearbyPets((Player) event.getEntity(), event.getPrev(), event.getTarget(), event.getEntity().level(), event.getEntity().level());
        }
    }

    /** 上述两个传送事件的共用实现：筛选 owner 的 {@code isValidTeleporter} 宠物，同维度安全点直传（打 PORTAL 票据），跨维度进 {@link #teleportingPets} 队列。 */
    private static void teleportNearbyPets(Player owner, Vec3 fromPos, Vec3 toPos, Level fromLevel, Level toLevel) {
        double dist = 20;
        boolean removeAndReadd = fromLevel.dimension() != toLevel.dimension();
        Predicate<Entity> enchantedPet = (animal) -> animal instanceof Mob && TameableUtils.isPetOf(owner, animal) && TameableUtils.isValidTeleporter(owner, (Mob) animal);
        for (Mob entity : fromLevel.getEntitiesOfClass(Mob.class, new AABB(fromPos.x - dist, fromPos.y - dist, fromPos.z - dist, fromPos.x + dist, fromPos.y + dist, fromPos.z + dist), EntitySelector.NO_SPECTATORS.and(enchantedPet))) {
            if (removeAndReadd) {
                // toLevel 来自 server.getLevel()，跨维度时必为 ServerLevel
                teleportingPets.add(new TeleportingPet(entity, (ServerLevel) toLevel, owner.getUUID()));
            } else {
                EntityDimensions dimensions = entity.getDimensions(entity.getPose());
                AABB suffocationBox = new AABB(-dimensions.width() / 2.0F, 0, -dimensions.width() / 2.0F, dimensions.width() / 2.0F, dimensions.height(), dimensions.width() / 2.0F);
                while (!toLevel.noCollision(entity, suffocationBox.move(toPos.x, toPos.y, toPos.z)) && toPos.y < 300) {
                    toPos = toPos.add(0, 1, 0);
                }
                entity.fallDistance = 0.0F;

                ChunkPos chunkpos = ChunkPos.containing(BlockPos.containing(toPos.x, toPos.y, toPos.z));
                ServerLevel entityLevel = (ServerLevel) entity.level();
                entityLevel.getChunkSource().addTicketWithRadius(TicketType.PORTAL, chunkpos, 0);
                entity.level().getChunk(chunkpos.x(), chunkpos.z());
                entity.teleportTo(toPos.x, toPos.y, toPos.z);
                entity.setPortalCooldown();
            }
        }
    }

    /**
     * 投射物命中保护：玩家投射物不伤自家宠物（取消）；穿透箭打到宠物时清穿透并 discard（修原版软崩溃）；
     * 宠物有 deflection 附魔时反弹投射物（速度取反 + 粒子 + 转向）。
     */
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
                    // 规避原版实体的软崩溃
                    if (arrow.getPierceLevel() > 0) {


                        ((ArrowAccessor) arrow).invokeSetPierceLevel((byte) 0);
                        arrow.remove(Entity.RemovalReason.DISCARDED);
                        event.setCanceled(true);
                        return;
                    }
                }
                if (TameableUtils.hasEnchant((LivingEntity) hit, PHEnchantments.DEFLECTION)) {
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

    /** 玩家主动下巨泡时阻止下潜取消（泡未破前骑乘者无法自行脱离）；破泡导致的下客走 ejectPassengers 不受影响。 */
    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (event.getEntityBeingMounted() instanceof GiantBubbleEntity && event.isDismounting() && event.getEntityBeingMounted().isAlive()) {
            event.setCanceled(true);
        }
    }

    /**
     * 实体进入世界（仅服务端）：① 罗盘档案更新（区块加载/维度切换/宠物床复活，复活实体带原 PetId 重新绑定）；
     * ② health_boost 宠物用安全血量恢复血上限；③ 已驯服实体 join 时清除其全部迷途灯笼请求。
     */
    @SubscribeEvent
    public static void onEntityJoinWorldEvent(EntityJoinLevelEvent event) {


        // 宠物罗盘：档案更新（区块加载/维度切换/宠物床复活回到世界——复活实体带着原 PetId，重新绑定 Entity UUID）
        if (event.getEntity() instanceof LivingEntity living && !event.getLevel().isClientSide() && TameableUtils.isTamed(living)) {
            PetCompassTracker.updateRecord(living);
        }

        if (event.getEntity() instanceof LivingEntity living && TameableUtils.couldBeTamed(living)) {
            if (TameableUtils.hasEnchant(living, PHEnchantments.HEALTH_BOOST)) {
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

    /**
     * 实体离开世界（仅服务端）：存活的已驯服宠物若满足 {@code shouldUnloadToLantern}（跟随态被卸载），
     * 写入含维度的 {@link LanternRequest}（坐标按 blockPosition）；health_boost 宠物离开前存安全血量。
     */
    @SubscribeEvent
    public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            if (!living.level().isClientSide() && living.isAlive() && TameableUtils.isTamed(living)) {
                boolean recallToLantern = TameableUtils.shouldUnloadToLantern(living);
                PHModLogger.debugLog("[lantern-debug] {} 离开世界: type={} comandable={} command={} orderedToSit={} 生成灯笼请求={}",
                        living.getName().getString(),
                        BuiltInRegistries.ENTITY_TYPE.getKey(living.getType()),
                        living instanceof IComandableMob,
                        living instanceof IComandableMob commandable ? commandable.getCommand() : -1,
                        living instanceof TamableAnimal animal && animal.isOrderedToSit(),
                        recallToLantern);
                if (recallToLantern) {
                    UUID ownerUUID = TameableUtils.getOwnerUUIDOf(event.getEntity());
                    String saveName = event.getEntity().hasCustomName() ? event.getEntity().getCustomName().getString() : "";
                    PHWorldData data = PHWorldData.get(living.level());
                    if (data != null) {
                        // 26.1: Level#getDayTime 改为 getOverworldClockTime
                        LanternRequest request = new LanternRequest(living.getUUID(), BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString(), ownerUUID, living.blockPosition(), event.getEntity().level().getOverworldClockTime(), saveName, living.level().dimension().identifier().toString());
                        data.addLanternRequest(request);
                    }
                }
            }
            if (TameableUtils.couldBeTamed(living) && TameableUtils.hasEnchant(living, PHEnchantments.HEALTH_BOOST)) {
                TameableUtils.setSafePetHealth(living, living.getHealth());
            }

        }
    }

    /**
     * 宠物死亡：有宠物床 → 用 TagValueOutput 序列化完整实体 NBT 生成 {@link RespawnRequest} 入世界数据（次日复活）；
     * 无床的非 TamableAnimal 宠物给在线主人发死亡消息（原版死亡消息规则只覆盖 TamableAnimal）；
     * 最后交给罗盘 {@code onPetDeath} 更新档案。
     */
    @SubscribeEvent
    public static void onLivingDie(LivingDeathEvent event) {
        if (TameableUtils.isTamed(event.getEntity())) {

            BlockPos bedPos = TameableUtils.getPetBedPos(event.getEntity());
            if (bedPos != null) {

                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, event.getEntity().registryAccess());
                event.getEntity().saveWithoutId(output);
                CompoundTag data = output.buildResult();
                String saveName = event.getEntity().hasCustomName() ? event.getEntity().getCustomName().getString() : "";

                RespawnRequest request = new RespawnRequest(BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString(), TameableUtils.getPetBedDimension(event.getEntity()), data, bedPos, event.getEntity().level().getOverworldClockTime(), saveName);
                PHWorldData worldData = PHWorldData.get(event.getEntity().level());
                if (worldData != null) {
                    worldData.addRespawnRequest(request);
                }
            }
            if (!(event.getEntity() instanceof TamableAnimal)) {
                Entity owner = TameableUtils.getOwnerOf(event.getEntity());
                if (!event.getEntity().level().isClientSide()
                        && event.getEntity().level() instanceof ServerLevel serverLevel
                        && serverLevel.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES)
                        && owner instanceof ServerPlayer) {
                    ((ServerPlayer) owner).sendSystemMessage(event.getEntity().getCombatTracker().getDeathMessage());
                }
            }

            // 宠物罗盘：死亡档案保留，仅标记待宠物床复活（档案不因实体消失而删除）
            PetCompassTracker.onPetDeath(event.getEntity());

        }
    }

    /** defusal（拆除）附魔：30 格内若有等级足够的拆除宠物，取消爆炸并播放灭火音效 + 云粒子。 */
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Start event) {
        float dist = 30;
        Vec3 center = event.getExplosion().center();
        Vec3 bottom = center.add(-dist, -dist, -dist);
        Vec3 top = center.add(dist, dist, dist);
        Predicate<Entity> defusal = (animal) -> TameableUtils.isTamed(animal) && TameableUtils.hasEnchant((LivingEntity) animal, PHEnchantments.DEFUSAL);
        boolean flag = false;
        for (LivingEntity defuser : event.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(bottom, top), EntitySelector.NO_SPECTATORS.and(defusal))) {
            float level = 10 * TameableUtils.getEnchantLevel(defuser, PHEnchantments.DEFUSAL);
            if (defuser.distanceToSqr(center) <= level * level) {
                flag = true;
                break;
            }
        }
        if (flag) {
            event.setCanceled(true);
            float pitch = 1.5F + event.getLevel().getRandom().nextFloat();
            event.getLevel().playSound(null, center.x, center.y, center.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, pitch);
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 5; i++) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 1.0F, center.z, 5, 0, 0F, 0, 0.2F);
                }
            }
        }
    }

    /** 宠物床被拆：清掉本床的全部复活请求（发 goodbye 消息）并重绑周边 10 格宠物的床绑定。 */
    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        if (event.getState().getBlock() instanceof PetBedBlock) {
            if (event.getLevel().getBlockEntity(event.getPos()) instanceof PetBedBlockEntity petBedBlockEntity) {
                petBedBlockEntity.removeAllRequestsFor(event.getPlayer());
                petBedBlockEntity.resetBedsForNearbyPets();
            }
        }
    }

    /** 苹果掉落物到期消失时按概率（0.1×数量）变烂苹果（rottenApple 配置开启才生效），并续命 10 tick。 */
    @SubscribeEvent
    public static void onItemDespawnEvent(ItemExpireEvent event) {
        if (event.getEntity().getItem().getItem() == Items.APPLE && PetHomeConfig.rottenApple) {
            if (event.getEntity().getRandom().nextFloat() < 0.1F * event.getEntity().getItem().getCount()) {
                event.getEntity().getItem().shrink(1);
                event.setExtraLife(10);
                ItemEntity rotten = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(PHItemRegistry.ROTTEN_APPLE.get()));

                if (event.getEntity().level() instanceof ServerLevel serverLevel) {
                    serverLevel.addFreshEntity(rotten);
                }
            }
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

    private static MutableComponent getDescription(Holder<Enchantment> enchantment, Identifier id, int level) {
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
        if (!ModList.get().isLoaded("enchdesc") && !stack.isEmpty() && stack.is(Items.ENCHANTED_BOOK)) {
            var enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
            if (enchantments != null && !enchantments.isEmpty()) {
                for (Holder<Enchantment> enchantmentHolder : enchantments.keySet()) {
                    enchantmentHolder.unwrapKey().ifPresent(enchantmentResourceKey -> {
                        if (enchantmentResourceKey.identifier().getNamespace().contains(PetHomeMod.MODID)) {
                            final MutableComponent description = getDescription(enchantmentHolder, enchantmentHolder.unwrapKey().orElseThrow().identifier(), enchantmentHolder.value().getMaxLevel());
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
