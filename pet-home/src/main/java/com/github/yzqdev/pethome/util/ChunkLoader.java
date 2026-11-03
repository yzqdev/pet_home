package com.github.yzqdev.pethome.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * 区块强加载工具。26.1 的 {@code TicketType} 无法再自定义携带实例，
 * 改用原版 {@code FORCED} 语义（等价 {@code setChunkForced}，含实体 tick）。
 * 调用方必须保证 {@link #forceLoadChunk} 与 {@link #unloadChunk} 成对出现
 * （灯笼实体在 {@code setRemoved} 里做兜底释放，防止持久化强加载残留）。
 */
public class ChunkLoader {
    /** 强制加载该区块（仅 {@code ServerLevel} 有效；FORCED ticket 会同时加载实体并保持活跃）。 */
    public static void forceLoadChunk(Level level, ChunkPos chunkPos) {
        if (level instanceof ServerLevel serverLevel) {
            // 26.1: TicketType 无法再自定义实例，改用原版 FORCED 语义强加载区块（含实体tick）
            serverLevel.getChunkSource().updateChunkForced(chunkPos, true);
        }
    }

    /** 释放 FORCED 强加载（与 {@link #forceLoadChunk} 配对；多余调用无副作用）。 */
    public static void unloadChunk(Level level, ChunkPos chunkPos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().updateChunkForced(chunkPos, false);
        }
    }
}