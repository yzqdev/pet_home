package com.github.yzqdev.pet_home.server.block;


import com.github.yzqdev.pet_home.server.misc.PHWorldData;
import com.github.yzqdev.pet_home.server.misc.LanternRequest;
import com.github.yzqdev.pet_home.util.ChunkLoader;
import com.github.yzqdev.pet_home.util.TameableUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WaywardLanternBlockEntity extends BlockEntity {

    private int checkAgainIn = 100;
    private int entityLoadTimeout = 0;
    private List<LanternRequest> workingRequests = new ArrayList<>();
    private List<UUID> finishedRequests = new ArrayList<>();
    public WaywardLanternBlockEntity(BlockPos pos, BlockState state) {
        super(PHTileEntityRegistry.WAYWARD_LANTERN.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WaywardLanternBlockEntity te) {
        if(!te.finishedRequests.isEmpty()){
            PHWorldData data = PHWorldData.get(level);
            te.workingRequests.removeIf(lanternRequest -> te.finishedRequests.contains(lanternRequest.getPetUUID()));
            for(UUID uuid: te.finishedRequests){
                data.removeMatchingLanternRequests(uuid);
            }
            te.finishedRequests.clear();
        }
        if(te.workingRequests.isEmpty()){
            if(te.checkAgainIn > 0){
                te.checkAgainIn--;
            }else{
                te.checkAgainIn = 200 + level.random.nextInt(400);
                PHWorldData data = PHWorldData.get(level);
                for (Player player : getPlayers(level, pos)) {
                    te.workingRequests.addAll(data.getLanternRequestsFor(player.getUUID()));
                }
            }
        }else{
            if(level instanceof ServerLevel serverLevel) {
                for (LanternRequest request : te.workingRequests) {
                    loadChunksAround(serverLevel, request.getPetUUID(), request.getChunkPosition(), true);
                    Entity entityFromChunk = serverLevel.getEntity(request.getPetUUID());
                    te.entityLoadTimeout++;
                    //takes a while to load in entities from the forced chunk, be patient...
                    if(entityFromChunk != null || te.entityLoadTimeout > 200){
                        te.entityLoadTimeout = 0;
                        if(entityFromChunk != null){
                            BlockPos putAt = getPlaceFor(entityFromChunk, pos, level.random);
                            entityFromChunk.teleportTo(putAt.getX() + 0.5F, putAt.getY(), putAt.getZ() + 0.5F);
                            Entity owner = TameableUtils.getOwnerOf(entityFromChunk);
                            if(owner instanceof Player){
                                ((Player)owner).displayClientMessage(Component.translatable("message.domesticationinnovation.wayward_lantern_return", entityFromChunk.getName()), false);
                            }
                            te.finishedRequests.add(request.getPetUUID());
                        }
                        loadChunksAround(serverLevel, request.getPetUUID(), request.getChunkPosition(), false);
                    }
                }
            }
        }
    }

    private static void loadChunksAround(ServerLevel serverLevel, UUID ticket, BlockPos center, boolean load){
        ChunkPos chunkPos = new ChunkPos(center);

        for(int i = -1; i <= 1; i++){
            for(int j = -1; j <= 1; j++){
               if (load){
                   ChunkLoader.forceLoadChunk(serverLevel,chunkPos);
               }else{
                   ChunkLoader.unloadChunk(serverLevel,chunkPos);
               }
//                ForcedChunkManager.writeModForcedChunks();
//                ForgeChunkManager.forceChunk(serverLevel, PetHomeMod.MODID, ticket, chunkPos.x + i, chunkPos.z + j, load, true);
            }
        }
    }

    private static List<Player> getPlayers(Level level, BlockPos pos) {
        double dist = 64 * 64;
        List<Player> withinDist = new ArrayList<>();
        for (Player player : level.players()) {
            if (player.distanceToSqr(Vec3.atCenterOf(pos)) < dist) {
                withinDist.add(player);
            }
        }
        return withinDist;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("CheckAgainIn")) {
            this.checkAgainIn = tag.getInt("CheckAgainIn");
        }
    }



    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);


        tag.putInt("CheckAgainIn", this.checkAgainIn);
    }

    private static BlockPos getPlaceFor(Entity entity, BlockPos lanternPos, RandomSource random){
        int maxDist = (int)Math.max(entity.getBbWidth() + 1, 10);
        for(int i = 0; i < 10; i++){
            BlockPos at = lanternPos.offset(random.nextInt(maxDist) - maxDist/2, 1, random.nextInt(maxDist) - maxDist/2);
            while(entity.level().getBlockState(at).isAir() && at.getY() > entity.level().getMinBuildHeight() && entity.level().noCollision(entity.getType().getSpawnAABB(at.getX() + 0.5F, at.getY() - 1, at.getZ() + 0.5F))){
                at = at.below();
            }
            if(entity.level().noCollision(entity.getType().getSpawnAABB(at.getX() + 0.5F, at.getY(), at.getZ() + 0.5F))){
                return at;
            }
            if(entity.isInWall()){
                return lanternPos.above();
            }
        }
        return lanternPos.above();
    }
}
