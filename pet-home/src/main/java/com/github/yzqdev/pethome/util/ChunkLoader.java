package com.github.yzqdev.pethome.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * 原版 FORCED 区块票据的薄封装：迷途灯笼（找宠物）与罗盘召回队列（取出未加载宠物）用。
 * FORCED 票据随存档持久化，调用方必须保证成对调用 load/unload。
 */
public class ChunkLoader {

    /** 给 {@code chunkPos} 打上 FORCED 票据（仅服务端生效），区块立即加载。 */
    public static void forceLoadChunk(Level level, ChunkPos chunkPos) {
        if (level instanceof ServerLevel serverLevel) {

            serverLevel.getChunkSource().updateChunkForced(chunkPos, true);
        }
    }

    /** 释放 FORCED 票据（必须与 {@link #forceLoadChunk} 配对，否则票据永久残留导致区块常驻强载）。 */
    public static void unloadChunk(Level level, ChunkPos chunkPos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().updateChunkForced(chunkPos, false);
        }
    }
}