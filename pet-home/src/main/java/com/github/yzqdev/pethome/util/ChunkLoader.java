package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Comparator;

public class ChunkLoader {
    /**
     * 单一静态票据类型：1.21 的 {@link TicketType} 没有重写 equals/hashCode，DistanceManager
     * 按引用比对票据，每次调用 {@code create} 都会产生新实例，导致 add/remove 永远配不上对，
     * 强加载票据只增不减（区块无限累积 → 主线程卡死 → Watchdog 崩溃）。
     */
    private static final TicketType<ChunkPos> PET_HOME_TICKET =
            TicketType.create(PetHomeMod.MODID, Comparator.comparingLong(ChunkPos::toLong));

    /**
     * 对指定区块请求 FORCED 语义的 region ticket（level 0，随存档持久化）。
     * 每次强载必须与 {@link #unloadChunk} 严格配对（灯笼/召回队列的成对调用点），
     * 否则票据残留导致区块永久加载。
     */
    public static void forceLoadChunk(Level level, ChunkPos chunkPos) {
        ServerLevel serverLevel = (ServerLevel) level;

        // 以无限时长请求 region ticket
        var ticketManager = serverLevel.getChunkSource().chunkMap.getDistanceManager();
        ticketManager.addRegionTicket(PET_HOME_TICKET, chunkPos, 0, chunkPos);
    }

    /** 移除 {@link #forceLoadChunk} 添加的票据（实体传送完成/请求放弃/灯笼移除时） */
    public static void unloadChunk(Level level, ChunkPos chunkPos) {
        ServerLevel serverLevel = (ServerLevel) level;

        var ticketManager = serverLevel.getChunkSource().chunkMap.getDistanceManager();
        ticketManager.removeRegionTicket(PET_HOME_TICKET, chunkPos, 0, chunkPos);
    }
}