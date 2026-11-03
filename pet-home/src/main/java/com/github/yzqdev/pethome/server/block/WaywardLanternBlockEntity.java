package com.github.yzqdev.pethome.server.block;

import com.github.yzqdev.pethome.server.NbtKeys;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import com.github.yzqdev.pethome.server.misc.PHWorldData;
import com.github.yzqdev.pethome.server.misc.LanternRequest;
import net.minecraft.core.BlockPos;
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
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 迷途灯笼方块实体：召回走失宠物。空闲 200-600 tick 后拉取 64 格内玩家主人的 LanternRequest 快照；
 * 工作期间每 tick 按请求最后位置临时强载 3x3 区块找实体（ForgeChunkManager 票据），找到则
 * {@link #getPlaceFor} 选落点传送、通知主人并标记完成（下 tick 从世界数据清除）。
 * 超过 200 tick 等不到实体就卸载票据复位计数，请求留在工作表中继续尝试。
 */
public class WaywardLanternBlockEntity extends BlockEntity {

    private int checkAgainIn = 100;
    private int entityLoadTimeout = 0;
    private List<LanternRequest> workingRequests = new ArrayList<>();
    private List<UUID> finishedRequests = new ArrayList<>();
    public WaywardLanternBlockEntity(BlockPos pos, BlockState state) {
        super(PHTileEntityRegistry.WAYWARD_LANTERN.get(), pos, state);
    }

    /** 灯笼主循环（服务端）：清完成请求 → 空闲期倒计时并按 64 格内玩家拉取请求快照 → 工作期逐请求强载/找实体/传送。 */
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
                    // 强载区块内的实体要等几个 tick 才加载完，需多等几拍再取
                    if(entityFromChunk != null || te.entityLoadTimeout > 200){
                        te.entityLoadTimeout = 0;
                        if(entityFromChunk != null){
                            BlockPos putAt = getPlaceFor(entityFromChunk, pos, level.random);
                            entityFromChunk.teleportTo(putAt.getX() + 0.5F, putAt.getY(), putAt.getZ() + 0.5F);
                            Entity owner = TameableUtils.getOwnerOf(entityFromChunk);
                            if(owner instanceof Player){
                                ((Player)owner).displayClientMessage(Component.translatable("message.pet_home.wayward_lantern_return", entityFromChunk.getName()), false);
                            }
                            te.finishedRequests.add(request.getPetUUID());
                        }
                        loadChunksAround(serverLevel, request.getPetUUID(), request.getChunkPosition(), false);
                    }
                }
            }
        }
    }

    /** 以请求的最后位置为中心对 3x3 区块加/卸 FORCED 票据（owner 为本 mod，ticket 用宠物 UUID）。 */
    private static void loadChunksAround(ServerLevel serverLevel, UUID ticket, BlockPos center, boolean load){
        ChunkPos chunkPos = new ChunkPos(center);
        for(int i = -1; i <= 1; i++){
            for(int j = -1; j <= 1; j++){
                ForgeChunkManager.forceChunk(serverLevel, PetHomeMod.MODID, ticket, chunkPos.x + i, chunkPos.z + j, load, true);
            }
        }
    }

    /** 灯笼 64 格内的玩家列表（其主人的灯笼请求才会被本灯笼处理）。 */
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

    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(NbtKeys.CHECK_AGAIN_IN)) {
            this.checkAgainIn = tag.getInt(NbtKeys.CHECK_AGAIN_IN);
        }

    }

    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(NbtKeys.CHECK_AGAIN_IN, this.checkAgainIn);
    }

    /** 灯笼旁选落点：最多试 10 次在 maxDist 范围随机点向下探到无碰撞位置；全部失败或卡墙则退回灯笼上方。 */
    private static BlockPos getPlaceFor(Entity entity, BlockPos lanternPos, RandomSource random){
        int maxDist = (int)Math.max(entity.getBbWidth() + 1, 10);
        for(int i = 0; i < 10; i++){
            BlockPos at = lanternPos.offset(random.nextInt(maxDist) - maxDist/2, 1, random.nextInt(maxDist) - maxDist/2);
            while(entity.level().getBlockState(at).isAir() && at.getY() > entity.level().getMinBuildHeight() && entity.level().noCollision(entity.getType().getAABB(at.getX() + 0.5F, at.getY() - 1, at.getZ() + 0.5F))){
                at = at.below();
            }
            if(entity.level().noCollision(entity.getType().getAABB(at.getX() + 0.5F, at.getY(), at.getZ() + 0.5F))){
                return at;
            }
            if(entity.isInWall()){
                return lanternPos.above();
            }
        }
        return lanternPos.above();
    }
}
