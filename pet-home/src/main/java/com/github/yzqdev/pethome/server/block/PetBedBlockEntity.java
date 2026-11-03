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

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * 宠物床方块实体：复活流的执行端。每天第一 tick（{@code dayTime % 24000 == 1}）从
 * {@link PHWorldData} 取绑定到本床、且维度匹配的 {@link RespawnRequest}，按存档 NBT 重建宠物。
 */
public class PetBedBlockEntity extends BlockEntity {

    public PetBedBlockEntity(BlockPos pos, BlockState state) {
        super(PHTileEntityRegistry.PET_BED, pos, state);
    }

    /** 方块实体 tick（由 PetBedBlock.getTicker 提供服务端接线）：次日清晨逐条复活并移除请求。 */
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

    /** 清空绑定到本床的全部复活请求（主人拆除床时调用），逐条向玩家提示「宠物走失」。 */
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

    /** 单条复活：按 NBT 重建实体（满血、朝向床朝向、默认停留指令），通知主人，成功返回 true。 */
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

    /** 重置 10×5×10 范围内绑定到本床的宠物（床被破坏时由 {@code ServerEvent.onBlockBreak} 调用），并通知各主人。 */
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
