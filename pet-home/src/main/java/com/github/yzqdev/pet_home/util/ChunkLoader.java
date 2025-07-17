package com.github.yzqdev.pet_home.util;
import com.github.yzqdev.pet_home.PetHomeMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Comparator;

public class ChunkLoader {
    // Request a chunk ticket (e.g., in your block entity or item)
    public static void forceLoadChunk(Level level, ChunkPos chunkPos) {
        ServerLevel serverLevel = (ServerLevel) level;
        TicketType<ChunkPos> ticketType = TicketType.create(PetHomeMod.MODID, Comparator.comparingLong(ChunkPos::toLong));
        
        // Request a ticket with infinite duration
        var ticketManager = serverLevel.getChunkSource().chunkMap.getDistanceManager();
        ticketManager.addRegionTicket(ticketType, chunkPos, 0, chunkPos);
    }

    // Release the ticket when done
    public static void unloadChunk(Level level, ChunkPos chunkPos) {
        ServerLevel serverLevel = (ServerLevel) level;
        TicketType<ChunkPos> ticketType = TicketType.create(PetHomeMod.MODID, Comparator.comparingLong(ChunkPos::toLong));
        
        var ticketManager = serverLevel.getChunkSource().chunkMap.getDistanceManager();
        ticketManager.removeRegionTicket(ticketType, chunkPos, 0, chunkPos);
    }
}