package com.github.yzqdev.pethome.server.event;

import static com.github.yzqdev.pethome.server.event.ServerEventContexts.*;
import net.minecraft.locale.Language;
import net.fabricmc.loader.api.FabricLoader;

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
import com.github.yzqdev.pethome.util.FriendlyFireCommon;
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

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 14 个服务器事件的接线与处理逻辑（决策层，Mixin/回调只转发进来）。
 * {@link #init()} 注册 7 个 Fabric 回调，其余 7 个由 {@code mixin/} 下的 Mixin 调入
 * （映射表见 {@link #init()} 注释）。方法体与 NeoForge 版几乎一致——上下文参数是
 * {@code ServerEventContexts} 里的同名适配类，静态导入后可直接对照阅读。
 *
 * <p>作用侧：服务端为主；{@code onItemTooltip} 的实现被客户端复用（附魔书 tooltip）。</p>
 */
public class ServerEvent {

    /**
     * Fabric 侧事件接线。
     *
     * <p>下表是 NeoForge 事件 → Fabric 回调/mixin 的对应关系：</p>
     * <ul>
     *   <li>{@code ServerAboutToStartEvent} → {@code ServerLifecycleEvents.SERVER_STARTING}</li>
     *   <li>{@code LevelTickEvent.Post} → {@code ServerTickEvents.END_LEVEL_TICK}</li>
     *   <li>{@code EntityJoinLevelEvent} → {@code ServerEntityEvents.ENTITY_LOAD}（lifecycle 模块）</li>
     *   <li>{@code EntityLeaveLevelEvent} → {@code ServerEntityEvents.ENTITY_UNLOAD}</li>
     *   <li>{@code LivingDeathEvent} → {@code ServerLivingEntityEvents.AFTER_DEATH}</li>
     *   <li>{@code BreakBlockEvent} → {@code PlayerBlockBreakEvents.AFTER}</li>
     *   <li>{@code EntityTravelToDimensionEvent} → {@code ServerEntityLevelChangeEvents.AFTER_ENTITY_CHANGE_LEVEL}</li>
     *   <li>{@code LivingDropsEvent} → {@code mixin/LivingEntityDropsMixin}（Fabric 无对应事件）</li>
     *   <li>{@code EntityTeleportEvent} → {@code mixin/EntityTeleportMixin}</li>
     *   <li>{@code ProjectileImpactEvent} → {@code mixin/ProjectileImpactMixin}</li>
     *   <li>{@code EntityMountEvent} → {@code mixin/EntityMountMixin}</li>
     *   <li>{@code ExplosionEvent.Start} → {@code mixin/ExplosionStartMixin}</li>
     *   <li>{@code ItemExpireEvent} → {@code mixin/ItemEntityExpireMixin}</li>
     *   <li>{@code ItemTooltipEvent} → 客户端 {@code ItemTooltipCallback}（见 ClientModEvents）</li>
     * </ul>
     */
    public static void init() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING.register(
                server -> serverStart(new ServerAboutToStartEvent(server)));

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_LEVEL_TICK.register(
                level -> onServerTick(new LevelTickEvent.Post(level)));

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register(
                (entity, world) -> onEntityJoinWorldEvent(new EntityJoinLevelEvent(entity, world)));

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_UNLOAD.register(
                (entity, world) -> onEntityLeaveWorld(new EntityLeaveLevelEvent(entity)));

        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register(
                (entity, source) -> onLivingDie(new LivingDeathEvent(entity, source)));

        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.AFTER.register(
                (world, player, pos, state, blockEntity) -> onBlockBreak(new BreakBlockEvent(player, world, pos, state)));


        // AFTER_ENTITY_CHANGE_WORLD 更名为 AFTER_ENTITY_CHANGE_LEVEL
        net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents.AFTER_ENTITY_CHANGE_LEVEL.register(
                (originalEntity, newEntity, origin, destination) ->
                        onEntityTravelToDimension(new EntityTravelToDimensionEvent(newEntity, destination.dimension(), origin)));
    }

    /** 客户端附魔书 tooltip 入口：由 {@code ItemTooltipCallback} 调用 */
    public static void onItemTooltip(java.util.List<net.minecraft.network.chat.Component> lines,
                                     ItemStack stack) {
        ItemTooltipEvent event = new ItemTooltipEvent(stack, lines);
        onItemTooltip(event);
    }

    /** 由 {@code mixin/LivingEntityDropsMixin}（{@code dropAllDeathLoot}，require=0）调入：绑定宠物床的宠物死亡不掉落（等着复活）。 */
    public static void onLivingDrops(LivingDropsEvent event) {
        if (TameableUtils.isTamed(event.getEntity()) && TameableUtils.getPetBedPos(event.getEntity()) != null) {

            event.setCanceled(true);
        }
    }

    /** 服务器启动期（SERVER_STARTING）：把宠物商店结构池元素按配置权重注入各生物群系村庄，并在主线程预热宠物店刷怪列表（worldgen worker 线程会读）。 */
    public static void serverStart(ServerAboutToStartEvent event) {
        RegistryAccess registryAccess = event.getServer().registryAccess();
        VillageHouseManager.addAllHouses(registryAccess);
        PetshopStructurePoolElement.preloadMobLists();
    }



    /**
     * 待跨维度传送的宠物队列（TETHERED_TELEPORT）：{@link #teleportNearbyPets} 收集，
     * {@link #onServerTick} 每轮消费——26.1 {@code Entity#setLevel} 已 protected，
     * 用「新实体 {@code restoreFrom} + {@code addDuringTeleport}」重建，原实体 discard。
     *
     * @param ownerUUID 目标主人；主人不在线时本轮跳过、实体保留等下一轮
     */
    public record TeleportingPet(Entity entity, ServerLevel targetLevel, UUID ownerUUID) {
    }

    /** 跨维度宠物传送等待队列，仅服务端主线程访问。 */
    public static List<TeleportingPet> teleportingPets = new ArrayList<>();

    /**
     * XP Transfer 的玩家侧驱动：10 格内未坐下、无战斗目标的 XP_Transfer 宠物，
     * 吸附其 10 格内经验球给主人（近距吸收、远距拉近并让宠物看向/走向经验球）。
     * 由 {@link #onServerTick} 每 10 tick 对每个玩家调用。
     */
    public static void onWorldTick(ServerPlayer serverPlayer, ServerLevel world) {

        AABB playerArea = serverPlayer.getBoundingBox().inflate(10.0);
        List<TamableAnimal> pets = world.getEntitiesOfClass(TamableAnimal.class, playerArea,
                pet -> pet.isTame() && !pet.isOrderedToSit()&&pet.getTarget()==null && TameableUtils.hasEnchant(pet, PHEnchantments.XP_Transfer));

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


    /** 项圈换装 tick 抑制查询，转发 {@code CollarTickManager}（供 Mixin 判断本 tick 是否跳过实体逻辑）。 */
    public static boolean canTickCollar(Entity entity) {
        return com.github.yzqdev.pethome.server.misc.CollarTickManager.canTickCollar(entity);
    }

    /** 设置项圈 tick 抑制（换装后 20 tick 内实体逻辑暂停），转发 {@code CollarTickManager}。 */
    public static void blockCollarTick(Entity entity) {
        com.github.yzqdev.pethome.server.misc.CollarTickManager.blockCollarTick(entity);
    }

    /**
     * 每维度 tick 末（END_LEVEL_TICK，仅服务端）：① 主世界每 20 tick 处理罗盘召回队列
     * {@code PetCompassTeleport.processRecallQueue}（跨维度召回要等区块加载，队列分轮次）；
     * ② 每 10 tick 驱动玩家侧 XP Transfer 与 {@link #teleportingPets} 跨维度重建传送
     * （重建时做防窒息抬升、清 fallDistance、设传送冷却）。
     */
    public static void onServerTick(LevelTickEvent.Post tick) {
        // 项圈 tick 抑制已统一由 server/misc/CollarTickManager 管理（EntityTickHandler.init 驱动）
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
                    // 26.1: Entity#setLevel 已 protected，参照原版 teleportCrossDimension 重建实体
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
                    // 此时才拿到新维度/新位置。 refreshOnly=true：GUI 开着才原位刷新，绝不凭空开屏（2026-11-03 用户反馈）
                    if (player instanceof ServerPlayer serverPlayer) {
                        PetCompassTracker.sendCompassData(serverPlayer, true);
                    }
                }
            }
            teleportingPets.clear();
        }
    }

    /** 玩家经传送门等改变维度后（AFTER_ENTITY_CHANGE_LEVEL）：把 20 格内带 TETHERED_TELEPORT 的宠物带走。 */
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (!event.isCanceled()) {
            // AFTER_ENTITY_CHANGE_LEVEL 在实体到达目标维度后触发，entity.level() 已是新维度；宠物仍在原维度，fromLevel 必须用 origin（known-issues 2026-11-03）
            if (event.getFromLevel() instanceof ServerLevel serverLevel && event.getEntity() instanceof Player) {
                MinecraftServer server = serverLevel.getServer();
                Level toLevel = server.getLevel(event.getDimension());
                if (toLevel != null) {
                    teleportNearbyPets((Player) event.getEntity(), event.getEntity().position(), event.getEntity().position(), serverLevel, toLevel);
                }
            }
        }
    }

    /**
     * 玩家原版 {@code teleport(TeleportTransition)} 类传送（mixin/EntityTeleportMixin 调入，require=0）：
     * /tp 与传送门的公共必经点。{@code toLevel} 取 {@code transition.newLevel()}——HEAD 时玩家 level()
     * 仍是原维度，不能直接用 {@code event.getEntity().level()} 当 toLevel。同维度直传，跨维度入队列。
     */
    public static void onEntityTeleport(EntityTeleportEvent event, Level toLevel) {
        if (event.getEntity() instanceof Player) {
            teleportNearbyPets((Player) event.getEntity(), event.getPrev(), event.getTarget(), event.getEntity().level(), toLevel);
        }
    }

    /**
     * 携带主人传送：20 格内 {@code isValidTeleporter}（TETHERED_TELEPORT 且跟随态）的宠物。
     * 同维度直接 {@code teleportTo}（防窒息抬升、清 fallDistance、PORTAL ticket 保证目标区块加载、
     * 设传送冷却）；跨维度加入 {@link #teleportingPets} 队列等 tick 重建。
     */
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
                // 26.1: ChunkPos 为 record，用静态工厂; POST_TELEPORT ticket 已删，改用自动过期的 PORTAL ticket
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
     * 投掷物命中（mixin/ProjectileImpactMixin 调入，require=0）：
     * ① 玩家自己的投掷物不打自家宠物（取消）；② 命中宠物且为穿透箭时降为 0 穿透并移除箭
     * （规避原版软崩溃，{@code setPierceLevel} 已私有走 accessor）；③ 宠物带 DEFLECTION 时
     * 反弹投掷物（转向 180° + DEFLECTION_SHIELD 粒子）。
     */
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

                        // 26.1: setPierceLevel 已私有，通过 accessor 调用
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
                    hit.level().addParticle(PHParticleRegistry.DEFLECTION_SHIELD, vec32.x, vec32.y, vec32.z, xRot, yRot, 0.0F);
                    event.getProjectile().setDeltaMovement(event.getProjectile().getDeltaMovement().scale(-0.2D));
                    event.getProjectile().setYRot(yRot + 180);
                    event.getProjectile().setXRot(xRot + 180);
                }
            }
        }
    }

    /** 巨型泡泡（泡泡控制附魔）存活期间禁止乘客下车（mixin/EntityMountMixin 调入，require=0）。 */
    public static void onEntityMount(EntityMountEvent event) {
        if (event.getEntityBeingMounted() instanceof GiantBubbleEntity && event.isDismounting() && event.getEntityBeingMounted().isAlive()) {
            event.setCanceled(true);
        }
    }

    /**
     * 实体进入世界（ENTITY_LOAD，含区块加载/维度切换/宠物床复活）：
     * ① 已驯服实体刷新罗盘档案（复活实体带原 PetId，重新绑定档案）；② HEALTH_BOOST 宠物
     * 恢复到卸载前保存的"安全血量"；③ 在线宠物移除其对应的灯笼收容请求（自愈：自己回来了）。
     */
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
     * 实体离开世界（ENTITY_UNLOAD，即区块卸载）：① 存活且 {@code shouldUnloadToLantern}
     * （跟随态）的驯服宠物写入 {@code LanternRequest} 等往生灯笼收容；② HEALTH_BOOST 宠物
     * 保存"安全血量"（防属性修饰丢失后血量溢出）。
     */
    public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            if (!living.level().isClientSide() && living.isAlive() && TameableUtils.isTamed(living) && TameableUtils.shouldUnloadToLantern(living)) {
                UUID ownerUUID = TameableUtils.getOwnerUUIDOf(event.getEntity());
                String saveName = event.getEntity().hasCustomName() ? event.getEntity().getCustomName().getString() : "";
                PHWorldData data = PHWorldData.get(living.level());
                if (data != null) {
                    // 26.1: Level#getDayTime 改为 getOverworldClockTime
                    LanternRequest request = new LanternRequest(living.getUUID(), BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString(), ownerUUID, living.blockPosition(), event.getEntity().level().getOverworldClockTime(), saveName, living.level().dimension().identifier().toString());
                    data.addLanternRequest(request);
                }
            }
            if (TameableUtils.couldBeTamed(living) && TameableUtils.hasEnchant(living, PHEnchantments.HEALTH_BOOST)) {
                TameableUtils.setSafePetHealth(living, living.getHealth());
            }

        }
    }

    /**
     * 宠物死亡（AFTER_DEATH）：① 绑定宠物床的宠物把完整实体 NBT 存为 {@code RespawnRequest}
     * （次日清晨由宠物床复活；掉落已被 LivingDropsMixin 抑制）；② 非 {@code TamableAnimal}
     * 的扩展物种死亡时按死亡消息规则通知主人（原版只给 TamableAnimal 发）；③ 罗盘档案标记待复活（不删档）。
     */
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

            // 宠物罗盘：死亡档案保留，仅标记待宠物床复活（ 不因 Entity UUID 消失而删档）
            PetCompassTracker.onPetDeath(event.getEntity());

        }
    }

    /**
     * 爆炸开始前（mixin/ExplosionStartMixin 调入，require=0）：30 格内存在 DEFUSAL 宠物
     * （等级×10 为生效半径）则取消爆炸，播放灭火音效 + 云粒子。
     */
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
            float pitch = 1.5F + new Random().nextFloat();
            event.getLevel().playSound(null, center.x, center.y, center.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, pitch);
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 5; i++) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 1.0F, center.z, 5, 0, 0F, 0, 0.2F);
                }
            }
        }
    }

    /** 玩家破坏宠物床后（PlayerBlockBreakEvents.AFTER）：清空该床的复活请求（提示主人）并重置附近宠物的床绑定。 */
    public static void onBlockBreak(BreakBlockEvent event) {
        if (event.getState().getBlock() instanceof PetBedBlock) {
            if (event.getLevel().getBlockEntity(event.getPos()) instanceof PetBedBlockEntity petBedBlockEntity) {
                petBedBlockEntity.removeAllRequestsFor(event.getPlayer());
                petBedBlockEntity.resetBedsForNearbyPets();
            }
        }
    }

    /** 苹果掉落物到期（mixin/ItemEntityExpireMixin 调入，require=0，按 tick 自行计数）：配置开启时 10%×数量 概率 1 个烂苹果，并续命 10 tick。 */
    public static void onItemDespawnEvent(ItemExpireEvent event) {
        if (event.getEntity().getItem().getItem() == Items.APPLE && PetHomeConfig.rottenApple) {
            if (new Random().nextFloat() < 0.1F * event.getEntity().getItem().getCount()) {
                event.getEntity().getItem().shrink(1);
                event.setExtraLife(10);
                ItemEntity rotten = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(PHItemRegistry.ROTTEN_APPLE));

                if (event.getEntity().level() instanceof ServerLevel serverLevel) {
                    serverLevel.addFreshEntity(rotten);
                }
            }
        }
    }







    /** 描述 lang 键的后缀候选（{@code <base>desc.<level>} / {@code ...description...} / {@code ...info...}），按序探测。 */
    private static final String[] KEY_TYPES = {"desc", "description", "info"};

    /** 按 lang 键前缀 + 等级探测附魔描述行（优先 {@code <前缀><后缀>.<等级>}，无等级键则用通用键）；都没有返回 null。 */
    private static MutableComponent getDescription(String baseKey, int level) {
        for (String keyType : KEY_TYPES) {
            String key = baseKey + keyType;
            if (Language.getInstance().has(key)) {
                return Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY);
            }
            key = key + "." + level;
            if (Language.getInstance().has(key)) {
                return Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY);
            }
        }
        return null;
    }

    /** 附魔描述探测：先按附魔 id 拼 lang 前缀，失败再回退附魔自带的 description 翻译键。 */
    private static MutableComponent getDescription(Holder<Enchantment> enchantment, Identifier id, int level) {
        MutableComponent description = getDescription("enchantment." + id.getNamespace() + "." + id.getPath() + ".", level);
        if (description == null && enchantment.value().description().getContents() instanceof TranslatableContents translatable) {
            description = getDescription(translatable.getKey() + ".", level);
        }
        return description;
    }

    /**
     * 附魔书 tooltip 补充 pet_home 附魔描述行（客户端 {@code ItemTooltipCallback} 调入；
     * 装了 enchdesc 时跳过避免重复）。实现被 {@link #onItemTooltip(List, ItemStack)}
     * 与客户端共用。
     */
    public static void onItemTooltip(ItemTooltipEvent event) {
        var tooltip = event.getToolTip();
        var stack = event.getItemStack();
        if (!FabricLoader.getInstance().isModLoaded("enchdesc") && !stack.isEmpty() && stack.is(Items.ENCHANTED_BOOK)) {
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
