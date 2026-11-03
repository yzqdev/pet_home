package com.github.yzqdev.pethome.server.block;


import com.github.yzqdev.pethome.server.entity.IComandableMob;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import com.github.yzqdev.pethome.server.misc.PHWorldData;
import com.github.yzqdev.pethome.server.misc.RespawnRequest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class PetBedBlockEntity extends BlockEntity {

    public PetBedBlockEntity(BlockPos pos, BlockState state) {
        super(PHTileEntityRegistry.PET_BED.get(), pos, state);
    }

    /** 方块实体 tick（服务端与客户端都进，白天第一 tick——dayTime%24000==1 时才动）：取出绑定本床的复活请求逐个复活。 */
    public static void tick(Level level, BlockPos pos, BlockState state, PetBedBlockEntity blockEntity) {
        long time = level.dayTime() % 24000L;
        if(time == 1){
            PHWorldData data = PHWorldData.get(level);
            if(data != null){
               List<RespawnRequest> requestList = data.getRespawnRequestsFor(level, pos);
               for(RespawnRequest request : requestList){
                    if(addAndRemoveEntity(level, pos, state.getValue(PetBedBlock.FACING), request)){
                        data.removeRespawnRequest(request);
                    }
               }
            }
        }
    }

    /** 床被拆/放生时清空本床全部复活请求（有玩家时逐个发告别消息）。 */
    public void removeAllRequestsFor(@Nullable Player message){
        PHWorldData data = PHWorldData.get(level);
        if(data != null){
            List<RespawnRequest> requestList = data.getRespawnRequestsFor(level, this.getBlockPos());
            for(RespawnRequest request : requestList){
                data.removeRespawnRequest(request);
                if(message != null){
                    message.displayClientMessage(Component.translatable("message.pet_home.goodbye", request.getNametag()), false);
                }
            }
        }
    }

    /** 复活单个请求：create + readAdditionalSaveData（复用原实体 NBT，UUID/PetId/附魔随之恢复）→ 满血 → 按床朝向放置 → 置坐下 → 通知主人。
     * @return true 表示复活成功（调用方据此移除请求）；类型失效或 petBedRespawns 配置关闭返回 false。 */
    private static boolean addAndRemoveEntity(Level level, BlockPos pos, Direction dir, RespawnRequest request) {
        EntityType type = request.getEntityType();
        if(type != null && PetHomeMod.CONFIG.petBedRespawns.get()){
            Entity entity = type.create(level);
            if(entity instanceof LivingEntity living){
                living.readAdditionalSaveData(request.getEntityData());
                living.setPos(Vec3.upFromBottomCenterOf(pos, 0.8F));
                living.setHealth(living.getMaxHealth());
                if(!request.getNametag().isEmpty()){
                    living.setCustomName(Component.translatable(request.getNametag()));
                }
                switch (dir){
                    case NORTH:
                        living.setYRot(180);
                        break;
                    case EAST:
                        living.setYRot(-90);
                        break;
                    case SOUTH:
                        living.setYRot(0);
                        break;
                    case WEST:
                        living.setYRot(90);
                        break;
                }
                if(living instanceof IComandableMob){
                    ((IComandableMob) living).setCommand(1);
                }

                if(living instanceof TamableAnimal tamableAnimal){
                    tamableAnimal.setOrderedToSit(true);

                }
                level.addFreshEntity(living);
                Entity owner = TameableUtils.getOwnerOf(entity);
                if(owner instanceof Player){
                    ((Player)owner).displayClientMessage(Component.translatable("message.pet_home.respawn", entity.getName()), false);
                }
                return true;
            }
        }
        return false;
    }

    /** 解绑 10x5x10 范围内绑定到本床的宠物（清宠物侧床坐标并通知主人），床被拆时调用。 */
    public void resetBedsForNearbyPets() {
        Predicate<Entity> pet = (animal) -> TameableUtils.isTamed(animal) && TameableUtils.getPetBedPos((LivingEntity)animal) != null && TameableUtils.getPetBedPos((LivingEntity)animal).equals(this.getBlockPos());
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, new AABB(this.getBlockPos().offset(-10, -5, -10), this.getBlockPos().offset(10, 5, 10)), EntitySelector.NO_SPECTATORS.and(pet));
        for (LivingEntity entity : list){
            Entity owner = TameableUtils.getOwnerOf(entity);
            if(owner instanceof Player){
                ((Player)owner).displayClientMessage(Component.translatable("message.pet_home.remove_respawn", entity.getName()), false);
                TameableUtils.removePetBedPos(entity);
            }
        }
    }
}
