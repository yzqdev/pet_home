package com.github.yzqdev.pethome.server;


import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.block.PetBedBlock;
import com.github.yzqdev.pethome.server.block.PetBedBlockEntity;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
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

    // 待跨维度传送的宠物列表，每 tick 结束后清空
    public static List<Triple<Entity, ServerLevel, UUID>> teleportingPets = new ArrayList<>();

    // 以维度 key 而非 Level 实例为键：Level 在维度重载时会重建，旧实例作键会永久滞留导致泄漏
    private static final Map<ResourceKey<Level>, CollarTickTracker> COLLAR_TICK_TRACKER_MAP = new HashMap<>();

    /** mod 初始化：注册 ForgeChunkManager 的强载票据回调（本 mod 票据随存档加载即清除）并注入村庄宠物店结构。 */
    public static void serverInit() {
        ForgeChunkManager.setForcedChunkLoadingCallback(PetHomeMod.MODID, ServerEvents::removeAllChunkTickets);
        PHVillagePieceRegistry.registerHouses();
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

    /** ForgeChunkManager 加载回调：本 mod 名下的实体强载票据（罗盘临时强载遗留）在读取时全部清除。 */
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

    /** 交互处理标 5 tick 项圈防抖（见 {@link CollarTickTracker}），仅在服务端生效。 */
    public static void blockCollarTick(Entity entity) {
        if (!entity.level().isClientSide()) {
            // computeIfAbsent：tracker 不存在时创建而不是解引用 null（原逻辑在 tracker==null 时 NPE、非空时什么都不做）
            COLLAR_TICK_TRACKER_MAP.computeIfAbsent(entity.level().dimension(), k -> new CollarTickTracker())
                    .addBlockedEntityTick(entity.getUUID(), 5);
        }
    }

    /** 实体进入世界：罗盘档案建档/重绑（宠物床复活实体带原 PetId 回来）；health_boost 宠物恢复安全血量；已驯服实体回归时清除其灯笼召回请求。 */
    @SubscribeEvent
    public static void onEntityJoinWorldEvent(EntityJoinLevelEvent event) {
        // 宠物罗盘：档案更新（区块加载/维度切换/宠物床复活回到世界——复活实体带着原 PetId，重新绑定 Entity UUID）
        if (event.getEntity() instanceof LivingEntity living && !event.getLevel().isClientSide() && TameableUtils.isTamed(living)) {
            PetCompassTracker.updateRecord(living);
        }
        if (event.getEntity() instanceof LivingEntity living && TameableUtils.couldBeTamed(living)) {
            if (TameableUtils.hasEnchant(living, PHEnchantmentRegistry.HEALTH_BOOST)) {
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

    /** 实体离开世界：跟随态宠物卸载时写入灯笼召回请求（{@link TameableUtils#shouldUnloadToLantern}）；health_boost 存安全血量供重进恢复。 */
    @SubscribeEvent
    public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            if (!living.level().isClientSide() && living.isAlive() && TameableUtils.isTamed(living) && TameableUtils.shouldUnloadToLantern(living)) {
                UUID ownerUUID = TameableUtils.getOwnerUUIDOf(event.getEntity());
                String saveName = event.getEntity().hasCustomName() ? event.getEntity().getCustomName().getString() : "";
                PHWorldData data = PHWorldData.get(living.level());
                if (data != null) {
                    LanternRequest request = new LanternRequest(living.getUUID(), ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType()).toString(), ownerUUID, living.blockPosition(), event.getEntity().level().dayTime(), saveName);
                    data.addLanternRequest(request);
                }
            }
            if (TameableUtils.couldBeTamed(living) && TameableUtils.hasEnchant(living, PHEnchantmentRegistry.HEALTH_BOOST)) {
                TameableUtils.setSafePetHealth(living, living.getHealth());
            }

        }
    }

    /**
     * LevelTickEvent 汇总：召回队列每 20 tick（仅主世界、END 相位）；项圈防抖递减、
     * xp_transfer 经验吸附（每 10 tick，逐玩家，对齐 26.3 onWorldTick）与
     * teleportingPets 跨维度重建（unRide → create+restoreFrom → 窒息上探 → addFreshEntity → DISCARD 原体）。
     * 注意历史行为：跨维度重建段只判 isClientSide 不判 Phase，START/END 各执行一次（写时保持原逻辑）。
     */
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
        if (!tick.level.isClientSide() && tick.level.getGameTime() % 10 == 0 && tick.level instanceof ServerLevel serverLevel) {
            for (ServerPlayer serverPlayer : serverLevel.players()) {
                xpTransferTick(serverPlayer, serverLevel);
            }
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
                    // 跨维度重建完成后补发罗盘数据：召回入队时发的刷新包早于实际到位（宠物还在旧维度），
                    // 此时才拿到新维度/新位置；GUI 开着时原位刷新，关闭时 handleData 不开屏
                    if (player instanceof ServerPlayer serverPlayer) {
                        PetHomeMod.LOGGER.debug("[compass-debug] cross-dim pet arrived, resending compass data");
                        PetCompassTracker.handleOpenRequest(serverPlayer);
                    }
                }
            }
            teleportingPets.clear();
        }
    }

    /**
     * xp_transfer 附魔（per-player tick，移植自 26.3 ServerEvent.onWorldTick）：吸附玩家 10 格内
     * 未坐下、无仇恨宠物的经验球（近距给主人加分、远距施加吸力并让宠物寻路追球）。
     * 宠物范围覆盖两类归属载体：原版 {@link TamableAnimal} 与本模组改造宠物（{@link ModifiedToBeTameable}，
     * 兔子等）——26.3 基准版本只查 TamableAnimal，此处为四侧统一的完善版。
     */
    private static void xpTransferTick(ServerPlayer serverPlayer, ServerLevel serverLevel) {
        AABB playerArea = serverPlayer.getBoundingBox().inflate(10.0);
        Predicate<Entity> petFilter = pet ->
                TameableUtils.isTamed(pet)
                        && pet instanceof Mob mobPet
                        && !(mobPet instanceof TamableAnimal tamable && tamable.isOrderedToSit())
                        && mobPet.getTarget() == null
                        && TameableUtils.hasEnchant(mobPet, PHEnchantmentRegistry.XP_Transfer);
        List<Mob> pets = serverLevel.getEntitiesOfClass(Mob.class, playerArea, petFilter);

        for (Mob pet : pets) {
            AABB searchBox = pet.getBoundingBox().inflate(10.0);
            List<ExperienceOrb> orbs = serverLevel.getEntitiesOfClass(ExperienceOrb.class, searchBox);

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

            ExperienceOrb closestOrb = orbs.get(0);
            pet.getLookControl().setLookAt(closestOrb, 30.0F, 30.0F);
            pet.getNavigation().moveTo(closestOrb, 1.2D);
        }
    }

    /** 投射物命中：玩家投射物不命中自家宠物（SKIP_ENTITY）；穿透箭命中宠物降为非穿透并消失（防原版软崩溃）；deflection 附魔反弹投射物并播放护盾粒子。 */
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
                    // 规避原版实体的软崩溃
                    if (arrow.getPierceLevel() > 0) {
                        arrow.setPierceLevel((byte) 0);
                        arrow.remove(Entity.RemovalReason.DISCARDED);
//                        event.setCanceled(true);
                        event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
                        return;
                    }
                }
                if (TameableUtils.hasEnchant((LivingEntity) hit, PHEnchantmentRegistry.DEFLECTION)) {
//                    event.setCanceled(true);
                    event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
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

    /** 苹果掉落物到期时按数量 10%/个概率腐化为烂苹果（rottenApple 配置开启时），续命 10 tick 防连锁到期。 */
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

    /** 友好保护落点：被保护时取消仇恨双向记录（受害方与真正攻击方的 lastHurtByMob 置空），防止 AI 报复循环。 */
    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingHurtEvent event) {
        if (FriendlyFireCommon.preventAttack(event.getEntity(), event.getSource(), event.getAmount())) {


            event.getEntity().setLastHurtByMob(null);

            if (event.getSource().getEntity() instanceof LivingEntity trueSource) {

                trueSource.setLastHurtByMob(null);
            }
        }
    }

    /** immaturity_curse（幼稚诅咒）惩罚：宠物造成的伤害乘 0.7 后向上取整。 */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
//        if(TameableUtils.isTamed(event.getEntity()) && event.getSource().getDirectEntity() instanceof Player player && TameableUtils.isPetOf(player, event.getEntity()) && !player.isShiftKeyDown()){
//            event.setCanceled(true);
//        }
        if (event.getSource().getEntity() instanceof LivingEntity pet && TameableUtils.isTamed(event.getSource().getEntity())) {
            if (TameableUtils.hasEnchant(pet, PHEnchantmentRegistry.IMMATURITY_CURSE)) {
                event.setAmount((float) Math.ceil(event.getAmount() * 0.7F));
            }
        }

    }

    /**
     * 宠物死亡总处理：绑定宠物床时把完整实体 NBT 存为 RespawnRequest（次日复活）；
     * 非 TamableAnimal 的宠物向在线主人发死亡消息（原版只对 TamableAnimal 发）；
     * 最后交罗盘标记档案（有床保留 alive=false，无床删档）。
     */
    @SubscribeEvent
    public static void onLivingDie(LivingDeathEvent event) {
        if (TameableUtils.isTamed(event.getEntity())) {

            BlockPos bedPos = TameableUtils.getPetBedPos(event.getEntity());
            if (bedPos != null) {
                CompoundTag data = new CompoundTag();
                event.getEntity().addAdditionalSaveData(data);
                String saveName = event.getEntity().hasCustomName() ? event.getEntity().getCustomName().getString() : "";
                RespawnRequest request = new RespawnRequest(ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType()).toString(), TameableUtils.getPetBedDimension(event.getEntity()), data, bedPos, event.getEntity().level().dayTime(), saveName);
                PHWorldData worldData = PHWorldData.get(event.getEntity().level());
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

            // 宠物罗盘：死亡档案保留，仅标记待宠物床复活（档案不因实体消失而删除）
            PetCompassTracker.onPetDeath(event.getEntity());
        }
    }

    /** 巨泡存活时禁止玩家主动下车（bubbling 附魔困住目标），破裂后自然解除。 */
    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {

        if (event.getEntityBeingMounted() instanceof GiantBubbleEntity && event.isDismounting() && event.getEntityBeingMounted().isAlive()) {
            event.setCanceled(true);
        }
    }


    /** 宠物床被拆：清该床全部复活请求（向主人发告别消息）并重置周边宠物的床绑定。 */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().getBlock() instanceof PetBedBlock) {
            if (event.getLevel().getBlockEntity(event.getPos()) instanceof PetBedBlockEntity entity1) {
                entity1.removeAllRequestsFor(event.getPlayer());
                entity1.resetBedsForNearbyPets();
            }
        }
    }

    /** 掠夺兽生成时（rabbitsScareRavagers 配置）加一个怕兔子的回避 AI（对应原版彩蛋）。 */
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

    /**
     * 村民交易：图书管理员各等级移除原版附魔书交易，替换为不含宠物附魔的版本（附魔书交易由战利品修改器提供）；
     * 为驯兽师（animal_tamer）职业逐级配置 1-5 级交易（宠物物品/食材/带附魔项圈等）。
     */
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
        if (event.getType() == PHVillagerRegistry.ANIMAL_TAMER.get()) {
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
//            level3.add(new SellingItemTrade(Items.LEATHER_HORSE_ARMOR, 8, 1, 2, 15));
            level3.add(new SellingItemTrade(PHBlockRegistry.DRUM.get(), 2, 3, 7, 11));
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

    /** 有宠物床的宠物死亡不掉落战利品/经验（装备与 NBT 都随复活请求带走）。 */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (TameableUtils.isTamed(event.getEntity()) && TameableUtils.getPetBedPos(event.getEntity()) != null) {
            event.setCanceled(true);
        }
    }

    /** defusal（拆除）附魔：30 格内存在等级覆盖爆炸点的宠物时取消爆炸，播放灭火音效与云雾粒子。 */
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Start event) {
        float dist = 30;
        Vec3 center = event.getExplosion().getPosition();
        Vec3 bottom = center.add(-dist, -dist, -dist);
        Vec3 top = center.add(dist, dist, dist);
        Predicate<Entity> defusal = (animal) -> TameableUtils.isTamed(animal) && TameableUtils.hasEnchant((LivingEntity) animal, PHEnchantmentRegistry.DEFUSAL);
        boolean flag = false;
        for (LivingEntity defuser : event.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(bottom, top), EntitySelector.NO_SPECTATORS.and(defusal))) {
            float level = 10 * TameableUtils.getEnchantLevel(defuser, PHEnchantmentRegistry.DEFUSAL);
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

    /** tethered_teleport 宠物随主人传送：20 格内符合 {@link TameableUtils#isValidTeleporter} 的宠物，同维度窒息上探后直传，跨维度进 teleportingPets 队列。 */
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

    /** 玩家原版传送事件（末影珍珠/紫颂果等）触发宠物随行。 */
    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if (event.getEntity() instanceof Player) {
            teleportNearbyPets((Player) event.getEntity(), event.getPrev(), event.getTarget(), event.getEntity().level(), event.getEntity().level());
        }
    }

    /** 玩家跨维度（下界传送门等）触发宠物随行，目标维度为玩家即将进入的维度。 */
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

    /** 阻止怪物把自家宠物设为攻击目标（原版不保护改造宠物的场景兜底）。 */
    @SubscribeEvent
    public static void onSetAttackTarget(LivingChangeTargetEvent event) {
        if (TameableUtils.isTamed(event.getEntity()) && event.getNewTarget() instanceof Player player && TameableUtils.isPetOf(player, event.getEntity())) {
            event.setCanceled(true);
        }
    }

    /** 铁砧合并两张带附魔项圈：按原版附魔合并规则（同附魔升一级/取高、冲突不可合并）计算经验消耗并产出合成结果。 */
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
