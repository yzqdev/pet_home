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

    /** 以 center 所在区块为中心强载 3x3 区块（FORCED 票据，owner 为本 mod，ticket 用调用方给定的 UUID）。 */
    public static void forceLoadAround(ServerLevel level, BlockPos center, UUID ticket) {
        ChunkPos chunkPos = new ChunkPos(center);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                ForgeChunkManager.forceChunk(level, PetHomeMod.MODID, ticket, chunkPos.x + i, chunkPos.z + j, true, true);
            }
        }
    }

    /** 释放 forceLoadAround 加的同 3x3 范围票据（须与加载成对调用，否则区块被永久强载）。 */
    public static void unloadAround(ServerLevel level, BlockPos center, UUID ticket) {
        ChunkPos chunkPos = new ChunkPos(center);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                ForgeChunkManager.forceChunk(level, PetHomeMod.MODID, ticket, chunkPos.x + i, chunkPos.z + j, false, true);
            }
        }
    }
}
