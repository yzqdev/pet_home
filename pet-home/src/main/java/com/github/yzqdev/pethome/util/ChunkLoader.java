package com.github.yzqdev.pethome.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ChunkLoader {
    // Request a chunk ticket (e.g., in your block entity or item)
    public static void forceLoadChunk(Level level, ChunkPos chunkPos) {
        if (level instanceof ServerLevel serverLevel) {
            // 26.1: TicketType 无法再自定义实例，改用原版 FORCED 语义强加载区块（含实体tick）
            serverLevel.getChunkSource().updateChunkForced(chunkPos, true);
        }
    }

    // Release the ticket when done
    public static void unloadChunk(Level level, ChunkPos chunkPos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().updateChunkForced(chunkPos, false);
        }
    }
}