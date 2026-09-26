package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.util.ChunkLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.github.yzqdev.pethome.server.event.ServerEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 宠物罗盘的两个独立传送功能
 */
public class PetCompassTeleport {

    /** 一次性临时强载召回请求（内存队列，不持久化；服务端重启丢失后玩家重试即可） */
    private static final List<RecallRequest> RECALL_QUEUE = new ArrayList<>();
    private static final long RECALL_TIMEOUT_TICKS = 200;

    private record RecallRequest(UUID petId, UUID entityUuid, UUID ownerId,
                                 String petDimension, BlockPos petPos,
                                 String playerDimension, BlockPos playerPos, long deadline, boolean chunksForced) {
    }

    /** C2S pet_compass_action 入口（PropertiesMessage.handleServer 分发） */
    public static void handleAction(ServerPlayer player, CompoundTag tag) {
        if (!PetHomeConfig.petCompassEnable) {
            player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.disabled")));
            return;
        }
        UUID petId = parseUuid(tag.getStringOr("PetId", ""));
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
        String action = tag.getStringOr("Action", "");
        switch (action) {
            case "to_pet" -> {
                if (PetHomeConfig.petCompassTeleportPlayerToPet) {
                    teleportPlayerToPet(player, record);
                }
            }
            case "to_player" -> {
                if (PetHomeConfig.petCompassTeleportPetToPlayer) {
                    teleportPetToPlayer(player, record);
                }
            }
        }
    }

    // ==================== 功能 A：传送玩家到宠物 ====================

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
        // 实体在线 → 用实时位置；否则按最后已知位置
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
        // 26.1: ServerPlayer.teleportTo 带 relatives/resetCamera 参数，Set.of() = 绝对坐标
        player.teleportTo(targetLevel, safe.x, safe.y, safe.z, java.util.Set.of(), player.getYRot(), player.getXRot(), false);
        player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.teleported")));
    }

    // ==================== 功能 B：召回宠物到玩家 ====================

    private static void teleportPetToPlayer(ServerPlayer player, PetRecord record) {
        if (!record.alive) {
            // 死亡等待宠物床复活：按现有宠物床生命周期处理，不凭空创建实体
            player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.dead_wait_respawn")));
            return;
        }
        Entity pet = findEntity(player, record.entityUuid);
        if (pet == null) {
            // 未加载：入一次性临时强载召回队列
            enqueueRecall(player, record);
            return;
        }
        movePetToPlayer(player, pet);
    }

    private static void movePetToPlayer(ServerPlayer player, Entity pet) {
        ServerLevel playerLevel = player.level() instanceof ServerLevel sl ? sl : null;
        if (playerLevel == null) {
            return;
        }
        if (pet.level().dimension() == playerLevel.dimension()) {
            // 同维度：安全点直接传送
            Vec3 safe = findSafeSpot(playerLevel, player.getX(), player.getY(), player.getZ());
            pet.teleportTo(safe.x, safe.y, safe.z);
            pet.fallDistance = 0.0F;
        } else {
            // 跨维度：复用现有 teleportingPets 队列（ServerEvent 每 10 tick 处理，create+restoreFrom 重建）
            ServerEvent.teleportingPets.add(new ServerEvent.TeleportingPet(pet, playerLevel, player.getUUID()));
        }
        player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.recalled"), pet.getName()));
    }

    private static void enqueueRecall(ServerPlayer player, PetRecord record) {
        for (RecallRequest request : RECALL_QUEUE) {
            if (request.petId().equals(record.petId)) {
                return; // 已在队列中
            }
        }
        RECALL_QUEUE.add(new RecallRequest(
                record.petId, UUID.fromString(record.entityUuid), record.ownerId,
                record.dimension, new BlockPos(record.x, record.y, record.z),
                player.level().dimension().identifier().toString(),
                player.blockPosition(),
                player.level().getGameTime() + RECALL_TIMEOUT_TICKS, false));
        player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.recall_started")));
    }

    /**
     * 召回队列处理：由 ServerEvent.onServerTick 在主世界 tick 上调用（全局队列只跑一处）。
     * 模式复用迷途灯笼：临时强载宠物区块 → 等实体出现 → 传送到玩家 → 立即卸载，绝不永久强载。
     * 收集-替换模式处理，避免迭代中修改队列。
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
                    player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.recall_failed")));
                }
                continue;
            }
            ServerLevel petLevel = resolveLevel(server, request.petDimension());
            if (petLevel == null) {
                continue;
            }
            if (!request.chunksForced()) {
                // 第一次处理：临时强载宠物区块，下一轮再尝试取实体（区块加载有延迟）
                ChunkLoader.forceLoadChunk(petLevel, net.minecraft.world.level.ChunkPos.containing(request.petPos()));
                remaining.add(new RecallRequest(request.petId(), request.entityUuid(), request.ownerId(),
                        request.petDimension(), request.petPos(), request.playerDimension(), request.playerPos(),
                        request.deadline(), true));
                continue;
            }
            Entity pet = petLevel.getEntity(request.entityUuid());
            if (pet instanceof LivingEntity living && living.isAlive()) {
                ServerLevel playerLevel = resolveLevel(server, request.playerDimension());
                if (playerLevel != null) {
                    movePetToPlayer(player, living);
                }
                ChunkLoader.unloadChunk(petLevel, net.minecraft.world.level.ChunkPos.containing(request.petPos()));
            } else if (gameTime > request.deadline() - RECALL_TIMEOUT_TICKS / 2) {
                // 强载后仍未出现且已过半程：放弃并卸载
                ChunkLoader.unloadChunk(petLevel, net.minecraft.world.level.ChunkPos.containing(request.petPos()));
                player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.recall_failed")));
            } else {
                remaining.add(request);
            }
        }
        RECALL_QUEUE.clear();
        RECALL_QUEUE.addAll(remaining);
    }

    // ==================== 工具 ====================

    /** 窒息检测：从目标点向上找安全落点（照 ServerEvent.teleportNearbyPets 的现有模式） */
    private static Vec3 findSafeSpot(ServerLevel level, double x, double y, double z) {
        Vec3 toPos = new Vec3(x, y, z);
        AABB suffocationBox = new AABB(-0.45, 0, -0.45, 0.45, 1.8, 0.45);
        while (!level.noCollision(suffocationBox.move(toPos.x, toPos.y, toPos.z)) && toPos.y < level.getMaxY()) {
            toPos = toPos.add(0, 1, 0);
        }
        return toPos;
    }

    @Nullable
    private static ServerLevel resolveLevel(MinecraftServer server, String dimension) {
        try {
            return server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimension)));
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static Entity findEntity(ServerPlayer player, String entityUuid) {
        try {
            UUID uuid = UUID.fromString(entityUuid);
            return player.level() instanceof ServerLevel serverLevel ? serverLevel.getEntityInAnyDimension(uuid) : null;
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
