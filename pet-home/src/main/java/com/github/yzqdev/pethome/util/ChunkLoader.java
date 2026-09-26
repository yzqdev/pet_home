package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Comparator;

public class ChunkLoader {

    private static final TicketType<ChunkPos> PET_HOME_TICKET =
            TicketType.create(PetHomeMod.MODID, Comparator.comparingLong(ChunkPos::toLong));

    // Request a chunk ticket (e.g., in your block entity or item)
    public static void forceLoadChunk(Level level, ChunkPos chunkPos) {
        ServerLevel serverLevel = (ServerLevel) level;

        // Request a ticket with infinite duration
        var ticketManager = serverLevel.getChunkSource().chunkMap.getDistanceManager();
        ticketManager.addRegionTicket(PET_HOME_TICKET, chunkPos, 0, chunkPos);
    }

    // Release the ticket when done
    public static void unloadChunk(Level level, ChunkPos chunkPos) {
        ServerLevel serverLevel = (ServerLevel) level;

        var ticketManager = serverLevel.getChunkSource().chunkMap.getDistanceManager();
        ticketManager.removeRegionTicket(PET_HOME_TICKET, chunkPos, 0, chunkPos);
    }
}