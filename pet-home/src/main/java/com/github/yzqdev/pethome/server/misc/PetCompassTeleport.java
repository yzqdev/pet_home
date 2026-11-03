package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.RecallBallEntity;
import com.github.yzqdev.pethome.util.ChunkLoaderHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.github.yzqdev.pethome.platform.Triple;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 宠物罗盘的两个独立传送功能（配置独立控制、服务端验证）：
 * - teleportPlayerToPet：玩家 → 宠物（跨维度；未加载用最后已知位置并提示）
 * - teleportPetToPlayer：宠物 → 玩家（真移动实体；跨维度复用现有 teleportingPets 队列；
 *   未加载走一次性临时强载的召回队列；死亡待复活按宠物床生命周期处理，绝不凭空造实体）
 * 所有操作服务端验证所有权 + 配置，防止伪造 PetId 操控他人宠物。
 */
public class PetCompassTeleport {

    /** 一次性临时强载召回请求（内存队列，不持久化；服务端重启丢失后玩家重试即可） */
    private static final List<RecallRequest> RECALL_QUEUE = new ArrayList<>();
    private static final long RECALL_TIMEOUT_TICKS = 200;

    private record RecallRequest(UUID petId, UUID entityUuid, UUID ownerId,
                                 String petDimension, BlockPos petPos,
                                 String playerDimension, BlockPos playerPos, long deadline, boolean chunksForced) {
    }

    /**
     * 召回反馈：聊天栏文字 + 音效。不用 player.playSound：其实现把玩家自己作为 except 排除，执行方自己听不到。
     */
    private static void sendRecallFeedback(ServerPlayer player, Component message, boolean withSound) {
        player.sendSystemMessage(message);
        if (withSound) {
            // 不用 player.playSound：其实现把玩家自己作为 except 排除，执行方自己听不到
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, player.getSoundSource(), 0.8F, 1.0F);
        }
    }

    /** C2S pet_compass_action 入口（PetCompassActionPacket 服务端分发） */
    public static void handleAction(ServerPlayer player, CompoundTag tag) {
        if (!PetHomeMod.CONFIG.petCompassEnable.get()) {
            player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.disabled")));
            return;
        }
        UUID petId = parseUuid(tag.getString("PetId"));
        if (petId == null) {
            return;
        }
        PHWorldData data = PHWorldData.get(player.level());
        if (data == null) {
            return;
        }
        // 服务端所有权验证：档案必须存在且属于该玩家
        PetRecord record = data.getPetRecord(petId);
        if (record == null || !record.ownerId.equals(player.getUUID())) {
            return;
        }
        String action = tag.getString("Action");
        switch (action) {
            case "to_pet" -> {
                if (PetHomeMod.CONFIG.petCompassTeleportPlayerToPet.get()) {
                    teleportPlayerToPet(player, record);
                }
            }
            case "to_player" -> {
                if (PetHomeMod.CONFIG.petCompassTeleportPetToPlayer.get()) {
                    teleportPetToPlayer(player, record, true);
                }
            }
        }
    }

    /**
     * 哨子（whistle）批量召回：对玩家罗盘档案里的全部存活宠物逐只走「召回宠物→玩家」路径（实体直传 / 跨维重建 / 未加载入强载召回队列；宝盒召回同维直传），死亡待复活的跳过并计数。
     * 结束后给一条汇总提示。哨子不受 petCompassTeleportPetToPlayer 开关约束。
     */
    public static void recallAllPets(ServerPlayer player) {
        PHWorldData data = PHWorldData.get(player.level());
        if (data == null) {
            return;
        }
        int handled = 0;
        int dead = 0;
        for (PetRecord record : data.getPetRecordsFor(player.getUUID())) {
            if (!record.alive) {
                dead++;
                continue;
            }
            // 哨子批量召回不刷罗盘 GUI（quiet）：GUI 开着时数据由下次正常打开/操作刷新
            if (teleportPetToPlayer(player, record, false)) {
                handled++;
            }
        }
        if (handled > 0) {
            sendRecallFeedback(player, Component.translatable(LangDefinition.message("whistle_summary"), handled), true);
        } else if (dead > 0) {
            player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.dead_wait_respawn")));
        } else {
            player.sendSystemMessage(Component.translatable(LangDefinition.message("whistle_none")));
        }
    }

    // ==================== 功能 A：传送玩家到宠物 ====================

    /** 玩家传送到宠物：死亡待复活直接提示；实体在线用实时位置，离线用最后已知位置并提示；落点经窒息检测上探。 */
    private static void teleportPlayerToPet(ServerPlayer player, PetRecord record) {
        if (!record.alive) {
            player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.dead_wait_respawn")));
            return;
        }
        ServerLevel targetLevel = resolveLevel(player.level().getServer(), record.dimension);
        if (targetLevel == null) {
            return;
        }
        boolean loaded = false;
        double x = record.x + 0.5;
        double y = record.y;
        double z = record.z + 0.5;
        // 实体在线 → 用实时位置；否则按档案中的最后已知位置传送（需给玩家提示）
        Entity pet = findEntity(player, record.entityUuid);
        if (pet != null && pet.isAlive()) {
            loaded = true;
            x = pet.getX();
            y = pet.getY();
            z = pet.getZ();
        }
        if (!loaded) {
            player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.not_loaded_confirm")));
        }
        Vec3 safe = findSafeSpot(targetLevel, x, y, z);
        player.teleportTo(targetLevel, safe.x, safe.y, safe.z, player.getYRot(), player.getXRot());
        player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.teleported")));
    }

    // ==================== 功能 B：召回宠物到玩家 ====================

    /**
     * 召回宠物：在线走 movePetToPlayer（同维直传/跨维进 teleportingPets 队列）；未加载入临时强载召回队列；死亡待复活只提示不造实体。
     * 在召回宝盒中的宠物：把宝盒实体送到玩家身边（同维直传；跨维读 NBT 在玩家处重建宝盒），右键打开即可取回宠物。
     */
    /**
     * @param refreshGui 召回后是否补发罗盘列表数据（罗盘路径 true；哨子批量召回 false，避免不必要地开 GUI）
     * @return 是否已发起召回（直传 / 入队 / 宝盒召回均算）；死亡待复活等无法召回时 false
     */
    private static boolean teleportPetToPlayer(ServerPlayer player, PetRecord record, boolean refreshGui) {
        if (!record.alive) {
            // 死亡等待宠物床复活：按现有宠物床生命周期处理，不凭空创建实体
            player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.dead_wait_respawn")));
            return false;
        }
        if (record.inBall) {
            return recallRecallBall(player, record, refreshGui);
        }
        Entity pet = findEntity(player, record.entityUuid);
        if (pet == null) {
            // 未加载：入一次性临时强载召回队列（用后即释放票据，避免区块永久强载）
            enqueueRecall(player, record);
            return true;
        }
        return movePetToPlayer(player, pet, refreshGui);
    }

    /**
     * 宝盒召回：把收存宠物的宝盒送到玩家身边（同维直传；跨维读宝盒 NBT 在玩家处重建）。
     *
     * @return 召回是否成功（宝盒丢失走强载召回入队也计成功）
     */
    private static boolean recallRecallBall(ServerPlayer player, PetRecord record, boolean refreshGui) {
        ServerLevel playerLevel = player.level() instanceof ServerLevel sl ? sl : null;
        if (playerLevel == null) {
            return false;
        }
        RecallBallEntity ball = findRecallBall(playerLevel, record.ballUuid);
        if (ball == null) {
            // 宝盒实体丢失（旧存档 / 区块异常）：清标记并按未加载路径走强载召回
            record.inBall = false;
            PHWorldData data = PHWorldData.get(player.level());
            if (data != null) {
                data.putPetRecord(record);
            }
            enqueueRecall(player, record);
            return true;
        }
        Vec3 safe = findSafeSpot(playerLevel, player.getX(), player.getY(), player.getZ());
        if (ball.level().dimension() == playerLevel.dimension()) {
            // 同维度：宝盒直传到玩家旁（未打开状态原样保留）
            ball.teleportTo(safe.x, safe.y, safe.z);
            ball.fallDistance = 0.0F;
        } else {
            // 跨维度：读宝盒数据在玩家处重建（宝盒数据仅 owner/类型/内容 NBT），原宝盒移除
            RecallBallEntity copy = PHEntityRegistry.RECALL_BALL.create(playerLevel);
            copy.setOwnerUUID(ball.getOwnerUUID());
            copy.setContainedEntityType(ball.getContainedEntityType());
            copy.setContainedData(ball.getContainedData());
            copy.setInvulnerable(true);
            copy.setPos(safe.x, safe.y, safe.z);
            copy.setYRot(ball.getYRot());
            ball.discard();
            playerLevel.addFreshEntity(copy);
        }
        sendRecallFeedback(player,
                Component.translatable(LangDefinition.message("pet_compass.ball_recalled"), record.displayName), true);
        // 宝盒到位后补发列表数据：GUI 开着时原位刷新（位置已变），关闭时不开屏（2026-11-03 用户反馈）；哨子批量路径不刷（refreshGui=false）
        if (refreshGui) {
            PetCompassTracker.sendCompassData(player, true);
        }
        return true;
    }

    /** 跨维度查找宝盒实体（按 ballUuid）；格式非法返回 null。 */
    @Nullable
    private static RecallBallEntity findRecallBall(ServerLevel playerLevel, String ballUuid) {
        try {
            UUID uuid = UUID.fromString(ballUuid);
            Entity entity = playerLevel.getEntity(uuid);
            return entity instanceof RecallBallEntity ball && ball.isAlive() ? ball : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** 真正移动宠物到玩家身边：同维度找安全点直传（清 fallDistance）；跨维度走 ServerEvent.teleportingPets 重建队列。 */
    private static boolean movePetToPlayer(ServerPlayer player, Entity pet, boolean refreshGui) {
        ServerLevel playerLevel = player.level() instanceof ServerLevel sl ? sl : null;
        if (playerLevel == null) {
            return false;
        }
        if (pet.level().dimension() == playerLevel.dimension()) {
            // 同维度：安全点直接传送
            Vec3 safe = findSafeSpot(playerLevel, player.getX(), player.getY(), player.getZ());
            pet.teleportTo(safe.x, safe.y, safe.z);
            pet.fallDistance = 0.0F;
        } else {
            // 跨维度：复用现有 teleportingPets 队列（ServerEvents 每 tick 处理，create+restoreFrom 重建）
            ServerEvent.teleportingPets.add(new Triple<>(pet, playerLevel, player.getUUID()));
        }
        sendRecallFeedback(player, Component.translatable(LangDefinition.message("pet_compass.recalled"), pet.getName()), true);
        // 召回实际到位后补发列表数据：GUI 开着时原位刷新（此时刷新包里的才是新位置），关闭时不开屏；哨子批量路径不刷
        if (refreshGui) {
            PetCompassTracker.sendCompassData(player, true);
        }
        return true;
    }

    /** 未加载宠物入召回队列（按 petId 去重，200 tick 截止）；纯内存请求，不持久化。 */
    private static void enqueueRecall(ServerPlayer player, PetRecord record) {
        for (RecallRequest request : RECALL_QUEUE) {
            if (request.petId().equals(record.petId)) {
                return; // 已在队列中
            }
        }
        RECALL_QUEUE.add(new RecallRequest(
                record.petId, UUID.fromString(record.entityUuid), record.ownerId,
                record.dimension, new BlockPos(record.x, record.y, record.z),
                player.level().dimension().location().toString(),
                player.blockPosition(),
                player.level().getGameTime() + RECALL_TIMEOUT_TICKS, false));
        sendRecallFeedback(player, Component.translatable(LangDefinition.message("pet_compass.recall_started")), false);
    }

    /**
     * 召回队列处理：由 ServerEvents.onServerTick 在主世界 tick 上调用（全局队列只跑一处）。
     * 模式复用迷途灯笼：临时强载宠物区块 → 等实体出现 → 传送到玩家 → 立即卸载，绝不永久强载。
     */
    public static void processRecallQueue(MinecraftServer server, long gameTime) {
        if (RECALL_QUEUE.isEmpty()) {
            return;
        }
        List<RecallRequest> remaining = new ArrayList<>();
        for (RecallRequest request : RECALL_QUEUE) {
            ServerPlayer player = server.getPlayerList().getPlayer(request.ownerId());
            if (player == null || gameTime > request.deadline()) {
                if (player != null) {
                    sendRecallFeedback(player, Component.translatable(LangDefinition.message("pet_compass.recall_failed")), false);
                }
                continue;
            }
            ServerLevel petLevel = resolveLevel(server, request.petDimension());
            if (petLevel == null) {
                continue;
            }
            if (!request.chunksForced()) {
                // 第一次处理：临时强载宠物区块，下一轮再尝试取实体（区块加载有延迟）
                ChunkLoaderHelper.forceLoadAround(petLevel, request.petPos(), request.petId());
                remaining.add(new RecallRequest(request.petId(), request.entityUuid(), request.ownerId(),
                        request.petDimension(), request.petPos(), request.playerDimension(), request.playerPos(),
                        request.deadline(), true));
                continue;
            }
            Entity pet = petLevel.getEntity(request.entityUuid());
            if (pet instanceof LivingEntity living && living.isAlive()) {
                ServerLevel playerLevel = resolveLevel(server, request.playerDimension());
                if (playerLevel != null) {
                    movePetToPlayer(player, living, true);
                }
                ChunkLoaderHelper.unloadAround(petLevel, request.petPos(), request.petId());
            } else if (gameTime > request.deadline() - RECALL_TIMEOUT_TICKS / 2) {
                // 强载后仍未出现且已过半程：放弃并卸载
                ChunkLoaderHelper.unloadAround(petLevel, request.petPos(), request.petId());
                sendRecallFeedback(player, Component.translatable(LangDefinition.message("pet_compass.recall_failed")), false);
            } else {
                remaining.add(request);
            }
        }
        RECALL_QUEUE.clear();
        RECALL_QUEUE.addAll(remaining);
    }

    // ==================== 工具 ====================

    /** 窒息检测：从目标点向上找安全落点（照 ServerEvents.teleportNearbyPets 的现有模式） */
    private static Vec3 findSafeSpot(ServerLevel level, double x, double y, double z) {
        Vec3 toPos = new Vec3(x, y, z);
        AABB suffocationBox = new AABB(-0.45, 0, -0.45, 0.45, 1.8, 0.45);
        while (!level.noCollision(suffocationBox.move(toPos.x, toPos.y, toPos.z)) && toPos.y < level.getMaxBuildHeight()) {
            toPos = toPos.add(0, 1, 0);
        }
        return toPos;
    }

    @Nullable
    static ServerLevel resolveLevel(MinecraftServer server, String dimension) {
        try {
            return server.getLevel(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, new ResourceLocation(dimension)));
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static Entity findEntity(ServerPlayer player, String entityUuid) {
        try {
            UUID uuid = UUID.fromString(entityUuid);
            return player.level() instanceof ServerLevel serverLevel ? serverLevel.getEntity(uuid) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    private static UUID parseUuid(String value) {
        try {
            return value.isEmpty() ? null : UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
