package com.github.yzqdev.pethome.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ChunkLoader {

    public static void forceLoadChunk(Level level, ChunkPos chunkPos) {
        if (level instanceof ServerLevel serverLevel) {

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