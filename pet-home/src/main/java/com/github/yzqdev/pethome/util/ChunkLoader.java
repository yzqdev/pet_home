package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.PetHomeMod;
import java.util.Comparator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * 区块强加载工具：用模组自定义 {@code TicketType}（{@code pet_home}，1.21 允许自定义实例）按 {@code ChunkPos}
 * 记账。往生灯笼找宠物时用它在宠物所在区块加临时 ticket，处理完必须 {@link #unloadChunk} 释放，否则会残留持久性强加载。
 */
public class ChunkLoader {

  private static final TicketType<ChunkPos> PET_HOME_TICKET =
      TicketType.create(PetHomeMod.MODID, Comparator.comparingLong(ChunkPos::toLong));

  // Request a chunk ticket (e.g., in your block entity or item)
  /** 给目标区块加永久时长（手动释放前一直有效）的强加载 ticket，半径 0（仅该区块本身）。 */
  public static void forceLoadChunk(Level level, ChunkPos chunkPos) {
    ServerLevel serverLevel = (ServerLevel) level;

    // Request a ticket with infinite duration
    var ticketManager = serverLevel.getChunkSource().chunkMap.getDistanceManager();
    ticketManager.addRegionTicket(PET_HOME_TICKET, chunkPos, 0, chunkPos);
  }

  // Release the ticket when done
  /** 释放 {@link #forceLoadChunk} 加的 ticket；必须与加票严格配对，否则区块被永久强载。 */
  public static void unloadChunk(Level level, ChunkPos chunkPos) {
    ServerLevel serverLevel = (ServerLevel) level;

    var ticketManager = serverLevel.getChunkSource().chunkMap.getDistanceManager();
    ticketManager.removeRegionTicket(PET_HOME_TICKET, chunkPos, 0, chunkPos);
  }
}
