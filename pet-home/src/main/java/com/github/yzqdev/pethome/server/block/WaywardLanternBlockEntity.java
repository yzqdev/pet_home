package com.github.yzqdev.pethome.server.block;


import com.github.yzqdev.pethome.server.misc.LanternRequest;
import com.github.yzqdev.pethome.server.misc.PHWorldData;
import com.github.yzqdev.pethome.util.ChunkLoader;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WaywardLanternBlockEntity extends BlockEntity {

    /** 一轮等待实体从强载区块中出现的耐心（tick） */
    private static final int ENTITY_LOAD_TIMEOUT = 200;
    /** 放弃前最多尝试的轮数：连续找不到实体就释放区块并跳过，请求保留在世界数据里等自愈 */
    private static final int MAX_ATTEMPTS = 3;

    private int checkAgainIn = 100;
    private final List<WorkingRequest> workingRequests = new ArrayList<>();
    private final List<UUID> finishedRequests = new ArrayList<>();
    /** 本次加载期间已放弃的请求（按宠物 UUID），刷新拉取时跳过，避免刚放弃又被拉回来继续空转 */
    private final Set<UUID> abandonedRequests = new HashSet<>();

    /** 灯笼处理中的请求 + 每请求独立的等待/尝试计数（原先共享一个 entityLoadTimeout，多请求时耐心被摊薄） */
    private static final class WorkingRequest {
        final LanternRequest request;
        int ticksWaiting;
        int attempts;

        WorkingRequest(LanternRequest request) {
            this.request = request;
        }
    }

    public WaywardLanternBlockEntity(BlockPos pos, BlockState state) {
        super(PHTileEntityRegistry.WAYWARD_LANTERN, pos, state);
    }

    /**
     * 灯笼被破坏或随区块卸载时，释放本灯笼强载的区块——FORCED 票据随存档持久化，
     * 不在这里释放会永久残留（对齐 1.20 侧的 setRemoved 清理）。
     */
    @Override
    public void setRemoved() {
        if (this.level instanceof ServerLevel serverLevel) {
            for (WorkingRequest working : this.workingRequests) {
                ServerLevel targetLevel = getTargetLevel(serverLevel, working.request);
                if (targetLevel != null) {
                    loadChunksAround(targetLevel, working.request.getChunkPosition(), false);
                }
            }
        }
        this.workingRequests.clear();
        super.setRemoved();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WaywardLanternBlockEntity te) {
        if (!te.finishedRequests.isEmpty()) {
            te.workingRequests.removeIf(working -> te.finishedRequests.contains(working.request.getPetUUID()));
            PHWorldData data = PHWorldData.get(level);
            if (data != null) {
                for (UUID uuid : te.finishedRequests) {
                    data.removeMatchingLanternRequests(uuid);
                }
            }
            te.finishedRequests.clear();
        }
        if (te.workingRequests.isEmpty()) {
            if (te.checkAgainIn > 0) {
                te.checkAgainIn--;
            } else {
                te.checkAgainIn = 200 + level.getRandom().nextInt(400);
                PHWorldData data = PHWorldData.get(level);
                if (data != null) {
                    for (Player player : getPlayers(level, pos)) {
                        for (LanternRequest request : data.getLanternRequestsFor(player.getUUID())) {
                            if (!te.abandonedRequests.contains(request.getPetUUID())) {
                                te.workingRequests.add(new WorkingRequest(request));
                            }
                        }
                    }
                }
            }
        } else if (level instanceof ServerLevel serverLevel) {
            Iterator<WorkingRequest> iterator = te.workingRequests.iterator();
            while (iterator.hasNext()) {
                WorkingRequest working = iterator.next();
                LanternRequest request = working.request;
                // 请求带维度：到宠物实际所在维度找实体，而不是在灯笼维度里空找（跨维度卸载的宠物之前永远找不到）
                ServerLevel targetLevel = getTargetLevel(serverLevel, request);
                if (targetLevel == null) {
                    iterator.remove();
                    continue;
                }
                loadChunksAround(targetLevel, request.getChunkPosition(), true);
                Entity entityFromChunk = targetLevel.getEntity(request.getPetUUID());
                working.ticksWaiting++;
                //takes a while to load in entities from the forced chunk, be patient...
                if (entityFromChunk != null || working.ticksWaiting > ENTITY_LOAD_TIMEOUT) {
                    working.ticksWaiting = 0;
                    if (entityFromChunk != null) {
                        BlockPos putAt = getPlaceFor(entityFromChunk, serverLevel, pos, level.getRandom());
                        // 传送前清零下落距离（跨维度路径 restoreFrom 会复制旧值，放后面就无效了）
                        entityFromChunk.fallDistance = 0.0F;
                        if (targetLevel == serverLevel) {
                            entityFromChunk.teleportTo(putAt.getX() + 0.5F, putAt.getY(), putAt.getZ() + 0.5F);
                        } else {
                            // 跨维度：原版 teleportTo(ServerLevel, ...) 内部走 TeleportTransition 重建实体
                            entityFromChunk.teleportTo(serverLevel, putAt.getX() + 0.5F, putAt.getY(), putAt.getZ() + 0.5F, Set.of(), entityFromChunk.getYRot(), entityFromChunk.getXRot(), false);
                        }
                        // 用 owner UUID 在灯笼所在维度解析主人（跨维度传送后实体已重建，不能再用旧实例的 owner 引用）
                        UUID ownerUUID = TameableUtils.getOwnerUUIDOf(entityFromChunk);
                        Entity owner = ownerUUID != null ? serverLevel.getPlayerByUUID(ownerUUID) : null;
                        if (owner instanceof Player) {
                            ((Player) owner).sendSystemMessage(Component.translatable("message.pet_home.wayward_lantern_return", entityFromChunk.getName()));
                        }
                        te.finishedRequests.add(request.getPetUUID());
                        te.abandonedRequests.remove(request.getPetUUID());
                    } else {
                        working.attempts++;
                        if (working.attempts >= MAX_ATTEMPTS) {
                            // 连续几轮找不到（宠物在别的维度未加载 / 已死等待复活 / 已被其他途径召回），本轮放弃
                            te.abandonedRequests.add(request.getPetUUID());
                            iterator.remove();
                        }
                    }
                    loadChunksAround(targetLevel, request.getChunkPosition(), false);
                }
            }
        }
    }

    @Nullable
    private static ServerLevel getTargetLevel(ServerLevel anyLevel, LanternRequest request) {
        return anyLevel.getServer().getLevel(request.getDimensionKey());
    }

    private static void loadChunksAround(ServerLevel serverLevel, BlockPos center, boolean load) {
        // 26.1: ChunkPos 是 record（无 BlockPos 构造器、x/z 私有），直接用方块坐标算区块坐标
        int centerChunkX = center.getX() >> 4;
        int centerChunkZ = center.getZ() >> 4;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                ChunkPos at = new ChunkPos(centerChunkX + i, centerChunkZ + j);
                if (load) {
                    ChunkLoader.forceLoadChunk(serverLevel, at);
                } else {
                    ChunkLoader.unloadChunk(serverLevel, at);
                }
            }
        }
    }

    private static List<Player> getPlayers(Level level, BlockPos pos) {
        double dist = 64 * 64;
        List<Player> withinDist = new ArrayList<>();
        for (Player player : level.players()) {
            if (player.distanceToSqr(Vec3.atCenterOf(pos)) < dist) {
                withinDist.add(player);
            }
        }
        return withinDist;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.checkAgainIn = input.getIntOr("CheckAgainIn", this.checkAgainIn);
    }


    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);


        output.putInt("CheckAgainIn", this.checkAgainIn);
    }

    /** targetLevel 是落点所在维度：碰撞检测必须查灯笼维度，而不是宠物来源维度 */
    private static BlockPos getPlaceFor(Entity entity, Level targetLevel, BlockPos lanternPos, RandomSource random) {
        int maxDist = (int) Math.max(entity.getBbWidth() + 1, 10);
        for (int i = 0; i < 10; i++) {
            BlockPos at = lanternPos.offset(random.nextInt(maxDist) - maxDist / 2, 1, random.nextInt(maxDist) - maxDist / 2);
            while (targetLevel.getBlockState(at).isAir() && at.getY() > targetLevel.getMinY() && targetLevel.noCollision(entity.getType().getSpawnAABB(at.getX() + 0.5F, at.getY() - 1, at.getZ() + 0.5F))) {
                at = at.below();
            }
            if (targetLevel.noCollision(entity.getType().getSpawnAABB(at.getX() + 0.5F, at.getY(), at.getZ() + 0.5F))) {
                return at;
            }
            if (entity.isInWall()) {
                return lanternPos.above();
            }
        }
        return lanternPos.above();
    }
}
