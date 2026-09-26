package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.UUID;

/**
 * 宠物罗盘召回用的临时强载助手（照 WaywardLanternBlockEntity.loadChunksAround 的 ForgeChunkManager 模式）。
 * 只做一次性临时强载：召回应答后必须调用 unloadAround 释放，绝不永久加载区块。
 */
public class ChunkLoaderHelper {

    public static void forceLoadAround(ServerLevel level, BlockPos center, UUID ticket) {
        ChunkPos chunkPos = new ChunkPos(center);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                ForgeChunkManager.forceChunk(level, PetHomeMod.MODID, ticket, chunkPos.x + i, chunkPos.z + j, true, true);
            }
        }
    }

    public static void unloadAround(ServerLevel level, BlockPos center, UUID ticket) {
        ChunkPos chunkPos = new ChunkPos(center);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                ForgeChunkManager.forceChunk(level, PetHomeMod.MODID, ticket, chunkPos.x + i, chunkPos.z + j, false, true);
            }
        }
    }
}
