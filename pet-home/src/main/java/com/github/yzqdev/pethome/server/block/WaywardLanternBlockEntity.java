package com.github.yzqdev.pethome.server.block;

import com.github.yzqdev.pethome.server.misc.LanternRequest;
import com.github.yzqdev.pethome.server.misc.PHWorldData;
import com.github.yzqdev.pethome.util.ChunkLoader;
import com.github.yzqdev.pethome.util.TameableUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 迷途灯笼方块实体：灯笼收容流的执行端。方块本身无交互，全靠 tick 自动处理—— 周期性（200~600 tick 随机间隔）收集 64 格内玩家名下的 {@code
 * LanternRequest}， 对每条请求临时强载宠物最后所在区块的 3×3 邻域，等宠物实体加载进来后传送回灯笼旁并通知主人， 随后立即释放强载并把请求从世界数据移除；200 tick
 * 找不到实体则放弃本轮（请求保留，下轮再试）。 仅服务端逻辑；1.21 版不做跨维度查找（按记录坐标在本维度处理）。
 */
public class WaywardLanternBlockEntity extends BlockEntity {

  private int checkAgainIn = 100;
  private int entityLoadTimeout = 0;
  private List<LanternRequest> workingRequests = new ArrayList<>();
  private List<UUID> finishedRequests = new ArrayList<>();

  public WaywardLanternBlockEntity(BlockPos pos, BlockState state) {
    super(PHTileEntityRegistry.WAYWARD_LANTERN, pos, state);
  }

  /** 方块实体 tick：结算已完成请求 → 空闲时随机间隔扫描新请求 → 处理中的请求（强载/等待/传送）。 */
  public static void tick(
      Level level, BlockPos pos, BlockState state, WaywardLanternBlockEntity te) {
    if (!te.finishedRequests.isEmpty()) {
      PHWorldData data = PHWorldData.get(level);
      te.workingRequests.removeIf(
          lanternRequest -> te.finishedRequests.contains(lanternRequest.getPetUUID()));
      for (UUID uuid : te.finishedRequests) {
        data.removeMatchingLanternRequests(uuid);
      }
      te.finishedRequests.clear();
    }
    if (te.workingRequests.isEmpty()) {
      if (te.checkAgainIn > 0) {
        te.checkAgainIn--;
      } else {
        te.checkAgainIn = 200 + level.random.nextInt(400);
        PHWorldData data = PHWorldData.get(level);
        for (Player player : getPlayers(level, pos)) {
          te.workingRequests.addAll(data.getLanternRequestsFor(player.getUUID()));
        }
      }
    } else {
      if (level instanceof ServerLevel serverLevel) {
        for (LanternRequest request : te.workingRequests) {
          loadChunksAround(serverLevel, request.getChunkPosition(), true);
          Entity entityFromChunk = serverLevel.getEntity(request.getPetUUID());
          te.entityLoadTimeout++;
          // takes a while to load in entities from the forced chunk, be patient...
          if (entityFromChunk != null || te.entityLoadTimeout > 200) {
            te.entityLoadTimeout = 0;
            if (entityFromChunk != null) {
              BlockPos putAt = getPlaceFor(entityFromChunk, pos, level.random);
              entityFromChunk.teleportTo(putAt.getX() + 0.5F, putAt.getY(), putAt.getZ() + 0.5F);
              Entity owner = TameableUtils.getOwnerOf(entityFromChunk);
              if (owner instanceof Player) {
                ((Player) owner)
                    .displayClientMessage(
                        Component.translatable(
                            "message.pet_home.wayward_lantern_return", entityFromChunk.getName()),
                        false);
              }
              te.finishedRequests.add(request.getPetUUID());
            }
            loadChunksAround(serverLevel, request.getChunkPosition(), false);
          }
        }
      }
    }
  }

  /** 以请求记录坐标为中心对 3×3 区块邻域批量加/释放强载 ticket（{@code ChunkLoader}，必须配对释放避免残留强载）。 */
  private static void loadChunksAround(ServerLevel serverLevel, BlockPos center, boolean load) {
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

  /** 取灯笼 64 格内的全部玩家（其名下的灯笼请求才会被本灯笼处理）。 */
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
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    if (tag.contains("CheckAgainIn")) {
      this.checkAgainIn = tag.getInt("CheckAgainIn");
    }
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);

    tag.putInt("CheckAgainIn", this.checkAgainIn);
  }

  /** 给召回宠物选落点：在灯笼周围随机取点向下探到可站立的非碰撞位；10 次未果则退回灯笼正上方。 */
  private static BlockPos getPlaceFor(Entity entity, BlockPos lanternPos, RandomSource random) {
    int maxDist = (int) Math.max(entity.getBbWidth() + 1, 10);
    for (int i = 0; i < 10; i++) {
      BlockPos at =
          lanternPos.offset(
              random.nextInt(maxDist) - maxDist / 2, 1, random.nextInt(maxDist) - maxDist / 2);
      while (entity.level().getBlockState(at).isAir()
          && at.getY() > entity.level().getMinBuildHeight()
          && entity
              .level()
              .noCollision(
                  entity
                      .getType()
                      .getSpawnAABB(at.getX() + 0.5F, at.getY() - 1, at.getZ() + 0.5F))) {
        at = at.below();
      }
      if (entity
          .level()
          .noCollision(
              entity.getType().getSpawnAABB(at.getX() + 0.5F, at.getY(), at.getZ() + 0.5F))) {
        return at;
      }
      if (entity.isInWall()) {
        return lanternPos.above();
      }
    }
    return lanternPos.above();
  }
}
